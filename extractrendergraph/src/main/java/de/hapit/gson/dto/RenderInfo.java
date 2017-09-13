package de.hapit.gson.dto;

import lombok.Data;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Data
public class RenderInfo {
    String filename = "";
    int totalFrames = 0;
    int startFrame = 0;
    int endFrame = 0;
    Duration totalRenderTime = Duration.ZERO;
    private long totalRenderTimeMillis = 0L;
    List<String> instanceTypes = Collections.emptyList();
    List<RenderFrameInfo> frameInfos = Collections.emptyList();
}
