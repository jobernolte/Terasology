/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;
import org.terasology.rendering.assets.Texture;

import javax.vecmath.Vector4f;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Chunk shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersChunk extends ShaderParametersBase {
    Property waveIntens = new Property("waveIntens", 1.5f, 0.0f, 2.0f);
    Property waveIntensFalloff = new Property("waveIntensFalloff", 0.85f, 0.0f, 2.0f);
    Property waveSize = new Property("waveSize", 0.1f, 0.0f, 2.0f);
    Property waveSizeFalloff = new Property("waveSizeFalloff", 1.25f, 0.0f, 2.0f);
    Property waveSpeed = new Property("waveSpeed", 0.1f, 0.0f, 2.0f);
    Property waveSpeedFalloff = new Property("waveSpeedFalloff", 0.95f, 0.0f, 2.0f);

    Property waveOverallScale = new Property("waveOverallScale", 1.0f, 0.0f, 2.0f);

    Property waterRefraction = new Property("waterRefraction", 0.04f, 0.0f, 1.0f);
    Property waterFresnelBias = new Property("waterFresnelBias", 0.01f, 0.01f, 0.1f);
    Property waterFresnelPow = new Property("waterFresnelPow", 2.5f, 0.0f, 10.0f);
    Property waterNormalBias = new Property("waterNormalBias", 25.0f, 1.0f, 100.0f);
    Property waterTint = new Property("waterTint", 0.24f, 0.0f, 1.0f);

    Property waterOffsetY = new Property("waterOffsetY", 0.0f, 0.0f, 5.0f);

    Property waterSpecExp = new Property("waterSpecExp", 512.0f, 0.0f, 1024.0f);

    Property parallaxBias = new Property("parallaxBias", 0.05f, 0.0f, 0.5f);
    Property parallaxScale = new Property("parallaxScale", 0.05f, 0.0f, 0.5f);

    public void applyParameters(GLSLShaderProgramInstance program) {
        super.applyParameters(program);

        Texture terrain = Assets.getTexture("engine:terrain");
        Texture terrainNormal = Assets.getTexture("engine:terrainNormal");
        Texture terrainHeight = Assets.getTexture("engine:terrainHeight");

        Texture water = Assets.getTexture("engine:waterStill");
        Texture lava = Assets.getTexture("engine:lavaStill");
        Texture waterNormal = Assets.getTexture("engine:waterNormal");
        Texture waterNormalAlt = Assets.getTexture("engine:waterNormalAlt");
        Texture effects = Assets.getTexture("engine:effects");

        if (terrain == null || water == null || lava == null || waterNormal == null || effects == null) {
            return;
        }

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, terrain.getId());
        program.setInt("textureAtlas", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, water.getId());
        program.setInt("textureWater", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, lava.getId());
        program.setInt("textureLava", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, waterNormal.getId());
        program.setInt("textureWaterNormal", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, waterNormalAlt.getId());
        program.setInt("textureWaterNormalAlt", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, effects.getId());
        program.setInt("textureEffects", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneReflected");
        program.setInt("textureWaterReflection", texId++);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
        program.setInt("texSceneOpaque", texId++);

        if (CoreRegistry.get(Config.class).getRendering().isNormalMapping()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL11.GL_TEXTURE_2D, terrainNormal.getId());
            program.setInt("textureAtlasNormal", texId++);

            if (CoreRegistry.get(Config.class).getRendering().isParallaxMapping()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, terrainHeight.getId());
                program.setInt("textureAtlasHeight", texId++);
            }
        }

        Vector4f lightingSettingsFrag = new Vector4f();
        lightingSettingsFrag.z = (Float) waterSpecExp.getValue();
        program.setFloat4("lightingSettingsFrag", lightingSettingsFrag);

        Vector4f waterSettingsFrag = new Vector4f();
        waterSettingsFrag.x = (Float) waterNormalBias.getValue();
        waterSettingsFrag.y = (Float) waterRefraction.getValue();
        waterSettingsFrag.z = (Float) waterFresnelBias.getValue();
        waterSettingsFrag.w = (Float) waterFresnelPow.getValue();
        program.setFloat4("waterSettingsFrag", waterSettingsFrag);

        Vector4f alternativeWaterSettingsFrag = new Vector4f();
        alternativeWaterSettingsFrag.x = (Float) waterTint.getValue();
        program.setFloat4("alternativeWaterSettingsFrag", alternativeWaterSettingsFrag);

        if (CoreRegistry.get(Config.class).getRendering().isAnimateWater()) {
            program.setFloat("waveIntensFalloff", (Float) waveIntensFalloff.getValue());
            program.setFloat("waveSizeFalloff", (Float) waveSizeFalloff.getValue());
            program.setFloat("waveSize", (Float) waveSize.getValue());
            program.setFloat("waveSpeedFalloff", (Float) waveSpeedFalloff.getValue());
            program.setFloat("waveSpeed", (Float) waveSpeed.getValue());
            program.setFloat("waveIntens", (Float) waveIntens.getValue());
            program.setFloat("waterOffsetY", (Float) waterOffsetY.getValue());
            program.setFloat("waveOverallScale", (Float) waveOverallScale.getValue());
        }

        if (CoreRegistry.get(Config.class).getRendering().isParallaxMapping()
                && CoreRegistry.get(Config.class).getRendering().isNormalMapping()) {
            program.setFloat4("parallaxProperties", (Float) parallaxBias.getValue(), (Float) parallaxScale.getValue(), 0.0f, 0.0f);
        }
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(waveIntens);
        properties.add(waveIntensFalloff);
        properties.add(waveSize);
        properties.add(waveSizeFalloff);
        properties.add(waveSpeed);
        properties.add(waveSpeedFalloff);
        properties.add(waterSpecExp);
        properties.add(waterNormalBias);
        properties.add(waterFresnelBias);
        properties.add(waterFresnelPow);
        properties.add(waterRefraction);
        properties.add(waterOffsetY);
        properties.add(waveOverallScale);
        properties.add(waterTint);
        properties.add(parallaxBias);
        properties.add(parallaxScale);
    }
}
