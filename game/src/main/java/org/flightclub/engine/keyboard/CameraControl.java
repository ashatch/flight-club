package org.flightclub.engine.keyboard;

import org.flightclub.engine.camera.CameraMan;
import org.flightclub.engine.camera.CameraMode;
import org.flightclub.engine.events.KeyEvent;
import org.flightclub.engine.events.KeyEventHandler;

public class CameraControl implements KeyEventHandler {

  private final CameraMan cameraMan;

  public CameraControl(CameraMan cameraMan) {
    this.cameraMan = cameraMan;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int key = e.code();
    switch (key) {
      case KeyEvent.VK_K:
        cameraMan.move(-CameraMan.CAMERA_MOVEMENT_DELTA, 0);
        return;
      case KeyEvent.VK_L:
        cameraMan.move(CameraMan.CAMERA_MOVEMENT_DELTA, 0);
        return;
      case KeyEvent.VK_M:
        cameraMan.move(0, CameraMan.CAMERA_MOVEMENT_DELTA);
        return;
      case KeyEvent.VK_N:
        cameraMan.move(0, -CameraMan.CAMERA_MOVEMENT_DELTA);
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
