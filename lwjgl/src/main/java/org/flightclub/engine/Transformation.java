package org.flightclub.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

  private final Matrix4f projectionMatrix;

  private final Matrix4f modelViewMatrix;

  private final Matrix4f viewMatrix;

  public Transformation() {
    projectionMatrix = new Matrix4f();
    modelViewMatrix = new Matrix4f();
    viewMatrix = new Matrix4f();
  }

  public final Matrix4f getProjectionMatrix(
      final float fov,
      final float width,
      final float height,
      final float zNear,
      final float zFar
  ) {
    return projectionMatrix.setPerspective(fov, width / height, zNear, zFar);
  }

  public Matrix4f getViewMatrix(final Camera camera) {
    final Vector3f cameraPos = camera.getPosition();
    final Vector3f rotation = camera.getRotation();

    viewMatrix.identity();
    viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
        .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
    viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

    return viewMatrix;
  }

  public Matrix4f getModelViewMatrix(
      final GameItem gameItem,
      final Matrix4f viewMatrix
  ) {
    final Vector3f rotation = gameItem.getRotation();
    modelViewMatrix.identity().translate(gameItem.getPosition()).
        rotateX((float)Math.toRadians(-rotation.x)).
        rotateY((float)Math.toRadians(-rotation.y)).
        rotateZ((float)Math.toRadians(-rotation.z)).
        scale(gameItem.getScale());
    final Matrix4f viewCurr = new Matrix4f(viewMatrix);
    return viewCurr.mul(modelViewMatrix);
  }
}