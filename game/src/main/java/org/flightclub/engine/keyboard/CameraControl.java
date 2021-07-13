package org.flightclub.engine.keyboard;

import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.camera.CameraMan;
import org.flightclub.engine.camera.CameraMode;
import org.flightclub.engine.events.KeyEvent;
import org.flightclub.engine.events.KeyEventHandler;

public class CameraControl implements KeyEventHandler {
  public static final float CAMERA_MOVEMENT_DELTA = (float) 0.1;

  private final CameraMan cameraMan;
  private final Camera camera;

  public CameraControl(
      final CameraMan cameraMan,
      final Camera camera
  ) {
    this.cameraMan = cameraMan;
    this.camera = camera;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int key = e.code();
    switch (key) {
      case KeyEvent.VK_K:
        camera.move(-CAMERA_MOVEMENT_DELTA, 0);
        return;
      case KeyEvent.VK_L:
        camera.move(CAMERA_MOVEMENT_DELTA, 0);
        return;
      case KeyEvent.VK_M:
        camera.move(0, CAMERA_MOVEMENT_DELTA);
        return;
      case KeyEvent.VK_N:
        camera.move(0, -CAMERA_MOVEMENT_DELTA);
        return;

      case KeyEvent.VK_1:
        cameraMan.setMode(CameraMode.SELF);
        return;
      case KeyEvent.VK_2:
        cameraMan.setMode(CameraMode.GAGGLE);
        return;
      case KeyEvent.VK_3:
        cameraMan.setMode(CameraMode.PLAN);
        return;
      case KeyEvent.VK_4:
        cameraMan.setMode(CameraMode.TILE);
        return;

      default:
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }
}
