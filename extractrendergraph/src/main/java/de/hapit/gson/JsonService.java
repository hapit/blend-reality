package de.hapit.gson;

import de.hapit.gson.dto.RenderFrameInfo;
import de.hapit.gson.dto.RenderInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class JsonService {

    private static final Logger LOG = LoggerFactory.getLogger(JsonService.class);

    public RenderInfo extractRenderInfo(List<RenderFrameInfo> renderFrameInfos) {
        RenderInfo renderInfo = new RenderInfo();

        renderInfo.setFrameInfos(renderFrameInfos);
        renderInfo.setInstanceTypes(getInstanceTypes(renderFrameInfos));
        renderInfo.setFilename(getFileName(renderFrameInfos));
        renderInfo.setTotalFrames(renderFrameInfos.size());
        Duration totalRenderTime = getDuration(renderFrameInfos);
        renderInfo.setTotalRenderTime(totalRenderTime);
        renderInfo.setTotalRenderTimeMillis(totalRenderTime.toMillis());
        renderInfo.setStartFrame(getStartFrame(renderFrameInfos));
        renderInfo.setEndFrame(getEndFrame(renderFrameInfos));

        return renderInfo;
    }

    private int getEndFrame(List<RenderFrameInfo> renderFrameInfos) {
        return renderFrameInfos.stream().max(Comparator.comparingInt(RenderFrameInfo::getFrame))
                .orElse(new RenderFrameInfo()).getFrame();
    }

    private int getStartFrame(List<RenderFrameInfo> renderFrameInfos) {
        return renderFrameInfos.stream().min(Comparator.comparingInt(RenderFrameInfo::getFrame))
                .orElse(new RenderFrameInfo()).getFrame();
    }

    private Duration getDuration(List<RenderFrameInfo> renderFrameInfos) {
        return renderFrameInfos.stream()
                .map(RenderFrameInfo::getRenderTime)
                .reduce(Duration::plus).orElse(Duration.ZERO);
    }

    private String getFileName(List<RenderFrameInfo> renderFrameInfos) {
        return renderFrameInfos.stream()
                .map(RenderFrameInfo::getFilename)
                .distinct()
                .collect(Collectors.joining("|"));
    }

    private List<String> getInstanceTypes(List<RenderFrameInfo> renderFrameInfos) {
        return renderFrameInfos.stream()
                .map(RenderFrameInfo::getInstanceType)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public String convert2String(RenderInfo renderInfo) {
        try (Writer writer = new StringWriter()) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(renderInfo, writer);
            return writer.toString();
        } catch (IOException e) {
            LOG.error("Error creating json string from renderInfo object.", e);
        }
        return "";
    }

    public void write2File(RenderInfo renderInfo, String targetFile) {
        try (Writer writer = new FileWriter(targetFile)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(renderInfo, writer);
        } catch (IOException e) {
            LOG.error("Error creating json file from renderInfo object.", e);
        }
    }
}
