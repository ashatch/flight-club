package org.flightclub.awt;

import java.awt.event.KeyEvent;

public class KeyEventMapper {
  public static org.flightclub.engine.KeyEvent toEngineKeyEvent(final KeyEvent e) {
    int type = e.getID() == KeyEvent.KEY_PRESSED
        ? org.flightclub.engine.KeyEvent.TYPE_KEY_PRESSED
        : org.flightclub.engine.KeyEvent.TYPE_KEY_RELEASED;

    return new org.flightclub.engine.KeyEvent(type, e.getKeyCode());
  }
}
