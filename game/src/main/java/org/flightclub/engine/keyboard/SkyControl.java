package org.flightclub.engine.keyboard;

import org.flightclub.engine.Sky;
import org.flightclub.engine.events.KeyEvent;
import org.flightclub.engine.events.KeyEventHandler;

public class SkyControl implements KeyEventHandler {
  private final Sky sky;

  public SkyControl(final Sky sky) {
    this.sky = sky;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int key = e.code();
    switch (key) {
      case KeyEvent.VK_H:
        sky.setHigh();
        break;

      case KeyEvent.VK_G:
        sky.setLow();
        break;

      default:
    }
  }
  @Override
  public void keyReleased(KeyEvent e) {

  }
}
