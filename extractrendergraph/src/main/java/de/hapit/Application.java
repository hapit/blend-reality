package de.hapit;

import de.hapit.gson.JsonService;
import de.hapit.gson.dto.RenderFrameInfo;
import de.hapit.gson.dto.RenderInfo;
import de.hapit.instanceextraction.InstanceExtractorService;
import de.hapit.instanceextraction.model.Instance;
import de.hapit.instanceextraction.model.InstanceInfo;
import de.hapit.metadata.MetadataService;
import de.hapit.templating.TemplateService;

import java.awt.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    public static final String TARGET_D3GRAPH_HTML = "target/d3graph.html";
    public static final String TARGET_JSON_FILE = "target/renderFrameInfos.json";
    public static final String TARGET_INSTANCE_TYPES_FILE = "target/instanceTypes.json";
    private static final double STANDARD_ECU = 62.0;


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setWebEnvironment(false);
        app.setHeadless(false);
        app.run(args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MetadataService metadataService,
                                               JsonService jsonService,
                                               TemplateService templateService,
                                               InstanceExtractorService instanceExtractorService,
                                               @Value("${imagesPath}") String imagesPath,
                                               @Value("${generate}") boolean generate) {
        return args -> {
            LOGGER.info("Application started...");

            InstanceInfo instanceInfo = instanceExtractorService.extractInstanceInfoFromAWS(TARGET_INSTANCE_TYPES_FILE);

            Path p = Paths.get(TARGET_JSON_FILE);
            String renderInfoJsonString;
            if (generate || !p.toFile().exists()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Generate json from image files in: " + imagesPath);
                }
                List<RenderFrameInfo> renderFrameInfos = metadataService.extractMetadata(imagesPath);

                calculateNormalization(instanceInfo, renderFrameInfos);

                RenderInfo renderInfo = jsonService.extractRenderInfo(renderFrameInfos);

                renderInfoJsonString = jsonService.convert2String(renderInfo);
                jsonService.write2File(renderInfo, TARGET_JSON_FILE);

            } else {
                renderInfoJsonString = new String (Files.readAllBytes(Paths.get(TARGET_JSON_FILE)), Charset.forName("UTF-8"));
            }

            templateService.renderD3page(renderInfoJsonString, TARGET_D3GRAPH_HTML);

            openWebpage(TARGET_D3GRAPH_HTML);

            LOGGER.info("Application stopped...");
        };
    }

    private void calculateNormalization(InstanceInfo instanceInfo, List<RenderFrameInfo> renderFrameInfos) {

        Map<String, Instance> instanceMap = createInstanceMap(instanceInfo);

        for (RenderFrameInfo frameInfo : renderFrameInfos) {
            Instance instance = instanceMap.get(frameInfo.getInstanceType());
            double ecu = instance.getEcu();
            frameInfo.setEcu(ecu);

            double normalizedMillis = frameInfo.getRenderTimeMillis() * ecu / STANDARD_ECU;

            frameInfo.setNormalizedRenderTimeMillis((long) normalizedMillis);
        }

    }

    private Map<String, Instance> createInstanceMap(InstanceInfo instanceInfo) {
        Map<String, Instance> instanceMap = new HashMap<>();

        for (Instance instance : instanceInfo.getInstanceTypes()) {
            instanceMap.put(instance.getName(), instance);
        }

        return instanceMap;
    }

    public void openWebpage(String uriString) {
        Path path = Paths.get(uriString);
        URI uri = path.toUri();
        LOGGER.info("Open uri in browser: " + uri.toString());
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                LOGGER.error("failed to open browser", e);
            }
        }
    }
}
