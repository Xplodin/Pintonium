package net.irisshaders.iris.shadows;

import net.irisshaders.iris.uniforms.CameraUniforms;
import net.irisshaders.iris.uniforms.CelestialUniforms;
import org.joml.Matrix4f;
import org.joml.Vector3d;

import static org.embeddedt.embeddium.compat.mc.MinecraftVersionShimService.MINECRAFT_SHIM;

/**
 * Matrix construction for the fixed-function shadow camera used by Minecraft 1.12.
 *
 * <p>The operation order intentionally matches Iris' modern {@code ShadowMatrices}
 * implementation. Keeping this in the vintage source set avoids pulling modern
 * PoseStack and Axis classes into the 1.12 build.</p>
 */
public final class VintageShadowMatrices {
    private static final float NEAR = 0.05F;
    private static final float FAR = 256.0F;

    private VintageShadowMatrices() {
    }

    public static Matrix4f createOrthoMatrix(float halfPlaneLength, float nearPlane, float farPlane) {
        return new Matrix4f(
                1.0F / halfPlaneLength, 0.0F, 0.0F, 0.0F,
                0.0F, 1.0F / halfPlaneLength, 0.0F, 0.0F,
                0.0F, 0.0F, 2.0F / (nearPlane - farPlane), 0.0F,
                0.0F, 0.0F, -(farPlane + nearPlane) / (farPlane - nearPlane), 1.0F
        );
    }

    public static Matrix4f createPerspectiveMatrix(float fov) {
        float yScale = (float) (1.0D / Math.tan(Math.toRadians(fov) * 0.5D));
        return new Matrix4f(
                yScale, 0.0F, 0.0F, 0.0F,
                0.0F, yScale, 0.0F, 0.0F,
                0.0F, 0.0F, (FAR + NEAR) / (NEAR - FAR), -1.0F,
                0.0F, 0.0F, 2.0F * FAR * NEAR / (NEAR - FAR), 0.0F
        );
    }

    public static Matrix4f createModelViewMatrix(float sunPathRotation, float intervalSize) {
        Vector3d camera = CameraUniforms.getUnshiftedCameraPosition();
        return createModelViewMatrix(getShadowAngle(), intervalSize, sunPathRotation,
                camera.x, camera.y, camera.z);
    }

    public static Matrix4f createModelViewMatrix(float shadowAngle, float intervalSize,
                                                  float sunPathRotation,
                                                  double cameraX, double cameraY, double cameraZ) {
        float skyAngle = shadowAngle < 0.25F ? shadowAngle + 0.75F : shadowAngle - 0.25F;

        Matrix4f modelView = new Matrix4f()
                .translate(0.0F, 0.0F, -100.0F)
                .rotateX((float) Math.toRadians(90.0F))
                .rotateZ((float) Math.toRadians(skyAngle * -360.0F))
                .rotateX((float) Math.toRadians(sunPathRotation));

        if (Math.abs(intervalSize) != 0.0F) {
            float halfInterval = intervalSize * 0.5F;
            float offsetX = (float) cameraX % intervalSize - halfInterval;
            float offsetY = (float) cameraY % intervalSize - halfInterval;
            float offsetZ = (float) cameraZ % intervalSize - halfInterval;
            modelView.translate(offsetX, offsetY, offsetZ);
        }

        return modelView;
    }

    private static float getShadowAngle() {
        float skyAngle = MINECRAFT_SHIM.getSkyAngle();
        float shadowAngle = skyAngle < 0.75F ? skyAngle + 0.25F : skyAngle - 0.75F;
        if (!CelestialUniforms.isDay()) {
            shadowAngle -= 0.5F;
        }
        return shadowAngle;
    }
}
