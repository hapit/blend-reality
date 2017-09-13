package de.hapit.gson.dto;

import de.hapit.gson.GsonSkip;
import lombok.Data;

import java.time.Duration;

@Data
public class RenderFrameInfo {
    private int frame = 0;
    @GsonSkip
    private String filename = "";
    private Duration renderTime = Duration.ZERO;
    private long renderTimeMillis = 0L;
    private long normalizedRenderTimeMillis = 0L;
    private String renderTimeDisplay = "";
    private String instanceType = "";
    private double ecu = 1;
}
