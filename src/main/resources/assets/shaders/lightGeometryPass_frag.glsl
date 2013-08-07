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

varying vec4 vertexProjPos;
varying vec3 eyeVec;

uniform vec3 lightViewPos;

uniform sampler2D texSceneOpaqueDepth;
uniform sampler2D texSceneOpaqueNormals;

uniform vec3 lightColorDiffuse = vec3(1.0, 0.0, 0.0);
uniform vec3 lightColorAmbient = vec3(1.0, 0.0, 0.0);

uniform vec4 lightProperties;
#define lightDiffuseIntensity lightProperties.y
#define lightAmbientIntensity lightProperties.x
#define lightSpecularIntensity lightProperties.z
#define lightSpecularPower lightProperties.w

uniform vec4 lightExtendedProperties;
#define lightAttenuationRange lightExtendedProperties.x
#define lightAttenuationFalloff lightExtendedProperties.y

uniform mat4 invProjMatrix;

#if defined (DYNAMIC_SHADOWS)
# if defined (CLOUD_SHADOWS)
uniform sampler2D texSceneClouds;
# endif

#define SHADOW_MAP_BIAS 0.01

uniform sampler2D texSceneShadowMap;
uniform mat4 lightViewProjMatrix;

uniform vec3 activeCameraToLightSpace;

uniform mat4 invViewProjMatrix;
#endif

void main() {

#if defined (FEATURE_LIGHT_POINT)
    vec2 projectedPos = projectVertexToTexCoord(vertexProjPos);
#elif defined (FEATURE_LIGHT_DIRECTIONAL)
    vec2 projectedPos = gl_TexCoord[0].xy;
#else
    vec2 projectedPos = vec2(0.0);
#endif

    vec4 normalBuffer = texture2D(texSceneOpaqueNormals, projectedPos.xy).rgba;
    vec3 normal = normalize(normalBuffer.xyz * 2.0 - 1.0);
    float depth = texture2D(texSceneOpaqueDepth, projectedPos.xy).r * 2.0 - 1.0;

#if defined (DYNAMIC_SHADOWS) && defined (FEATURE_LIGHT_DIRECTIONAL)
    // TODO: Uhhh... Doing this twice here :/ Frustum ray would be better!
    vec3 worldPosition = reconstructViewPos(depth, gl_TexCoord[0].xy, invViewProjMatrix);
    vec3 lightWorldPosition = worldPosition.xyz + activeCameraToLightSpace;

    vec4 lightProjPos = lightViewProjMatrix * vec4(lightWorldPosition.x, lightWorldPosition.y, lightWorldPosition.z, 1.0);

    vec3 lightPosClipSpace = lightProjPos.xyz / lightProjPos.w;
    vec2 shadowMapTexPos = lightPosClipSpace.xy * vec2(0.5) + vec2(0.5);

    float shadowTerm = 1.0;

    if (!epsilonEqualsOne(depth)) {
# if defined (DYNAMIC_SHADOWS_PCF)
        shadowTerm = calcPcfShadowTerm(texSceneShadowMap, lightPosClipSpace.z, shadowMapTexPos, 0.0, SHADOW_MAP_BIAS);
# else
        float shadowMapDepth = texture2D(texSceneShadowMap, shadowMapTexPos).x;
        if (shadowMapDepth + SHADOW_MAP_BIAS < lightPosClipSpace.z) {
            shadowTerm = 0.0;
        }
# endif

# if defined (CLOUD_SHADOWS)
        // TODO: Not so nice that this is all hardcoded
        float cloudOcclusion = clamp(1.0 - texture2D(texSceneClouds, (worldPosition.xz + cameraPosition.xz) * 0.005 + timeToTick(time, 0.004)).r * 5.0, 0.0, 1.0);
        shadowTerm *= clamp(1.0 - cloudOcclusion + 0.25, 0.0, 1.0);
#  endif
   }
#endif

    // TODO: Costly - would be nice to use Crytek's view frustum ray method at this point
    vec3 viewSpacePos = reconstructViewPos(depth, projectedPos, invProjMatrix);

    vec3 lightDir = lightViewPos.xyz - viewSpacePos;
    float lightDist = length(lightDir);
    vec3 lightDirNorm = lightDir / lightDist;

    float ambTerm = lightAmbientIntensity;
    float lambTerm = calcLambLight(normal, lightDirNorm);
    float specTerm  = calcSpecLight(normal, lightDirNorm, eyeVec, lightSpecularPower);

#if defined (DYNAMIC_SHADOWS) && defined (FEATURE_LIGHT_DIRECTIONAL)
    lambTerm *= shadowTerm;
    ambTerm *= clamp(shadowTerm, 0.25, 1.0);
#endif

    float specular = lightSpecularIntensity * specTerm;

#if defined (FEATURE_LIGHT_POINT)
    vec3 color = ambTerm * lightColorAmbient;
    color += lightColorDiffuse * lightDiffuseIntensity * lambTerm;
#elif defined (FEATURE_LIGHT_DIRECTIONAL)
    vec3 color = calcSunlightColorDeferred(normalBuffer.a, lambTerm, ambTerm, lightDiffuseIntensity, lightColorAmbient, lightColorDiffuse);
#else
    vec3 color = vec3(0.0);
#endif

#if defined (FEATURE_LIGHT_POINT)
    //float attenuation = clamp (1.0 - ((lightDist * lightDist) / (lightAttenuationRange * lightAttenuationRange)), 0.0, 1.0);
    float attenuation = clamp(1.0 - (pow (lightDist, lightAttenuationFalloff) / lightAttenuationRange), 0.0, 1.0);

    specular *= attenuation;
    color *= attenuation;
#endif

    gl_FragData[0].rgba = vec4(color.r, color.g, color.b, specular);
}
