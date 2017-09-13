package de.hapit.metadata;

import de.hapit.gson.dto.RenderFrameInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

@Service
public class MetadataService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MetadataService.class);
    private static final List<String> INFO_TAGS = Arrays.asList("RenderTime", "Frame", "InstanceType", "File");

    public List<RenderFrameInfo> extractMetadata(String directoryPath) {

        Path path = Paths.get(directoryPath);
        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .map(this::getInputStream)
                    .filter(Objects::nonNull)
                    .map(this::getMetadata)
                    .filter(Objects::nonNull)
                    .map(this::getRenderInfo)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .sorted(Comparator.comparing(RenderFrameInfo::getFrame))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Error extracting metadata from files", e);
        }
        return Collections.emptyList();
    }

    private Optional<RenderFrameInfo> getRenderInfo(Metadata metadata) {
        List<Directory> dirList = new ArrayList<>();
        for (Directory directory : metadata.getDirectories()) {
            dirList.add(directory);
        }

        return dirList.stream()
                .flatMap(dir -> dir.getTags().stream())
                .filter((Tag tag) -> {
                    for (String infoTag : INFO_TAGS) {
                        if (tag.getDescription().contains(infoTag)) {
                            return true;
                        }
                    }
                    return false;
                })

                .map(this::tag2RenderInfo)
                .reduce((ri1, ri2) -> {
                    RenderFrameInfo riMerged = new RenderFrameInfo();
                    riMerged.setFilename(ri1.getFilename() + ri2.getFilename());
                    riMerged.setInstanceType(ri1.getInstanceType() + ri2.getInstanceType());
                    riMerged.setFrame(ri1.getFrame() + ri2.getFrame());
                    riMerged.setRenderTime(ri1.getRenderTime().plus(ri2.getRenderTime()));
                    riMerged.setRenderTimeMillis(ri1.getRenderTimeMillis() + (ri2.getRenderTimeMillis()));
                    riMerged.setRenderTimeDisplay(ri1.getRenderTimeDisplay() + ri2.getRenderTimeDisplay());
                    return riMerged;
                });
    }

    private Metadata getMetadata(InputStream is) {
        try {
            return ImageMetadataReader.readMetadata(is);
        } catch (ImageProcessingException e) {
            LOGGER.error("Couldn't read metadata from image.", e);
            return null;
        } catch (IOException e) {
            LOGGER.error("Couldn't list files");
            return null;
        }
    }

    private InputStream getInputStream(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            LOGGER.error("couldn't get input stream.", e);
        }
        return null;
    }

    private RenderFrameInfo tag2RenderInfo(Tag tag) {
        RenderFrameInfo renderInfo = new RenderFrameInfo();
        String description = tag.getDescription();
        if (description.contains("RenderTime")) {
            String timeAsString = description.substring("RenderTime: ".length());
            String[] split = timeAsString.split(":");
            Duration renderTime = Duration.ZERO;
            int startIndex = 0;
            if (split.length == 3) {
                renderTime.plusHours(Integer.parseInt(split[0]));
                startIndex++;
            }
            renderTime = renderTime.plusMinutes(Long.parseLong(split[startIndex]));
            String[] secondsPart = split[startIndex + 1].split("\\.");
            renderTime = renderTime.plusSeconds(Integer.parseInt(secondsPart[0]));
            renderTime = renderTime.plusMillis(Integer.parseInt(secondsPart[1]) * 10L);
            renderInfo.setRenderTime(renderTime);
            renderInfo.setRenderTimeMillis(renderTime.toMillis());
            renderInfo.setRenderTimeDisplay(timeAsString);
        } else if (description.contains("Frame")) {
            String frameAsString = description.substring("Frame: ".length());
            int frame = Integer.parseInt(frameAsString);
            renderInfo.setFrame(frame);
        } else if (description.contains("InstanceType")) {
            String start = "InstanceType=";
            String end = "||";
            String instanceType = description.substring(description.indexOf(start) + start.length(), description.indexOf(end));
            renderInfo.setInstanceType(instanceType);
        } else if (description.contains("File")) {
            renderInfo.setFilename(description.substring(description.lastIndexOf('/') + 1));
        }
        return renderInfo;
    }
}
