package org.terasology.config;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com/>
 * @author Immortius
 */
public class SystemConfig {
    public enum DebugRenderingStages {
        DEBUG_STAGE_OPAQUE_COLOR,
        DEBUG_STAGE_TRANSPARENT_COLOR,
        DEBUG_STAGE_OPAQUE_NORMALS,
        DEBUG_STAGE_OPAQUE_DEPTH,
        DEBUG_STAGE_OPAQUE_SUNLIGHT,
        DEBUG_STAGE_BAKED_OCCLUSION,
        DEBUG_STAGE_SSAO,
        DEBUG_STAGE_OPAQUE_LIGHT_BUFFER,
        DEBUG_STAGE_SHADOW_MAP,
        DEBUG_STAGE_SOBEL,
        DEBUG_STAGE_HIGH_PASS,
        DEBUG_STAGE_BLOOM,
        DEBUG_STAGE_SKY_BAND,
        DEBUG_STAGE_LIGHT_SHAFTS,
        DEBUG_STAGE_RECONSTRUCTED_POSITION,
    }

    private long dayNightLengthInMs = 1800000;
    private int maxThreads = 2;
    private int verticalChunkMeshSegments = 1;

    private boolean debugEnabled;
    private boolean debugRenderChunkBoundingBoxes;
    private boolean debugRenderingEnabled;
    private int debugRenderingStage;
    private boolean debugFirstPersonElementsHidden;
    private boolean debugRenderWireframe;
    private boolean debugHudHidden;

    public boolean isDebugFirstPersonElementsHidden() {
        return debugFirstPersonElementsHidden;
    }

    public void setDebugFirstPersonElementsHidden(boolean debugFirstPersonElementsHidden) {
        this.debugFirstPersonElementsHidden = debugFirstPersonElementsHidden;
    }

    public long getDayNightLengthInMs() {
        return dayNightLengthInMs;
    }

    public void setDayNightLengthInMs(long dayNightLengthInMs) {
        this.dayNightLengthInMs = dayNightLengthInMs;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getVerticalChunkMeshSegments() {
        return verticalChunkMeshSegments;
    }

    public void setVerticalChunkMeshSegments(int verticalChunkMeshSegments) {
        this.verticalChunkMeshSegments = verticalChunkMeshSegments;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public boolean isDebugRenderChunkBoundingBoxes() {
        return debugRenderChunkBoundingBoxes;
    }

    public void setDebugRenderChunkBoundingBoxes(boolean debugRenderChunkBoundingBoxes) {
        this.debugRenderChunkBoundingBoxes = debugRenderChunkBoundingBoxes;
    }

    public boolean isDebugRenderingEnabled() {
        return debugRenderingEnabled;
    }

    public void setDebugRenderingEnabled(boolean debugRenderingEnabled) {
        this.debugRenderingEnabled = debugRenderingEnabled;
    }

    public void cycleDebugRenderingStage() {
        this.debugRenderingStage = (this.debugRenderingStage + 1) % DebugRenderingStages.values().length;
    }

    public int getDebugRenderingStage() {
        return debugRenderingStage;
    }

    public boolean isDebugRenderWireframe() {
        return debugRenderWireframe;
    }

    public void setDebugRenderWireframe(boolean debugRenderWireframe) {
        this.debugRenderWireframe = debugRenderWireframe;
    }

    public boolean isDebugHudHidden() {
        return debugHudHidden;
    }

    public void setDebugHudHidden(boolean debugHudHidden) {
        this.debugHudHidden = debugHudHidden;
    }
}
