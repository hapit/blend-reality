package de.hapit.templating;

import de.hapit.gson.dto.RenderInfo;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateService.class);
    public static final String D3_HTML_TEMPLATE_NAME = "d3graph.ftl";
    private final Configuration freeMarkerConfig;

    @Autowired
    public TemplateService (Configuration freeMarkerConfig) {
        this.freeMarkerConfig = freeMarkerConfig;
    }

    public void logConfigInfo() {
        TemplateLoader templateLoader = freeMarkerConfig.getTemplateLoader();
        if(LOG.isInfoEnabled()) {
            LOG.info(templateLoader.toString());
        }
    }

    public void renderD3page(String renderInfo, String targetFile) {
        try (Writer writer = new FileWriter(targetFile)) {
            Template template = freeMarkerConfig.getTemplate(D3_HTML_TEMPLATE_NAME);
            Map<String, Object> model = new HashMap<>(1);
            model.put("jsonData", renderInfo);
            template.process(model, writer);
        } catch (IOException | TemplateException e) {
            LOG.error("Error creating html file from renderInfo object.", e);
        }

    }
}
