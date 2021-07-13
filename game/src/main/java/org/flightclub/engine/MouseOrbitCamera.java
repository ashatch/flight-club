package org.flightclub.engine;

import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.events.MouseTracker;

public class MouseOrbitCamera implements UpdatableGameObject {
  private final Camera camera;
  private final MouseTracker mouseTracker;

  public MouseOrbitCamera(final Camera camera, final MouseTracker mouseTracker) {
    this.camera = camera;
    this.mouseTracker = mouseTracker;
  }

  @Override
  public void update(final UpdateContext context) {
    if (mouseTracker.isDragging()) {
      //float dtheta = (float) dx/width;
      float dtheta = 0;
      float dz = 0;
      float unitStep = (float) Math.PI * context.deltaTime() / 8; //4 seconds to 90 - sloow!

      if (mouseTracker.getDeltaX() > 20) {
        dtheta = -unitStep;
      }

      if (mouseTracker.getDeltaX() < -20) {
        dtheta = unitStep;
      }

      if (mouseTracker.getDeltaY() > 20) {
        dz = context.deltaTime() / 4;
      }

      if (mouseTracker.getDeltaY() < -20) {
        dz = -context.deltaTime() / 4;
      }

      camera.rotateEyeAboutFocus(-dtheta);
      camera.translateZ(-dz);
    }
  }
}
