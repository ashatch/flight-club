package org.flightclub.engine;

import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.events.MouseTracker;

public class MouseOrbitCamera implements UpdatableGameObject {
  private static final float DEFAULT_SENSITIVITY = 20f;

  private final Camera camera;
  private final MouseTracker mouseTracker;
  private final float speedZeroToOne;

  private float sensitivity = DEFAULT_SENSITIVITY;
  private boolean limitZ = false;

  public MouseOrbitCamera(
      final Camera camera,
      final MouseTracker mouseTracker,
      float speedZeroToOne
  ) {
    this.camera = camera;
    this.mouseTracker = mouseTracker;
    this.speedZeroToOne = speedZeroToOne;
  }

  @Override
  public void update(final UpdateContext context) {
    if (this.mouseTracker.isDragging()) {
      final float unitStep = context.deltaTime() * speedZeroToOne;

      float theta = 0;
      float translateZ = 0;

      if (this.mouseTracker.getDeltaX() > this.sensitivity) {
        theta = (float) Math.PI * -unitStep;
      }

      if (this.mouseTracker.getDeltaX() < -this.sensitivity) {
        theta = (float) Math.PI * unitStep;
      }

      if (this.mouseTracker.getDeltaY() > this.sensitivity) {
        translateZ = unitStep;
      }

      if (this.mouseTracker.getDeltaY() < -this.sensitivity) {
        translateZ = -unitStep;
      }

      camera.rotateEyeAboutFocus(-theta);
      camera.translateZ(-translateZ, limitZ);
    }
  }

  public MouseOrbitCamera withSensitivity(float sensitivity) {
    this.sensitivity = sensitivity;
    return this;
  }

  public MouseOrbitCamera withLimitZ(boolean limitZ) {
    this.limitZ = limitZ;
    return this;
  }
}
