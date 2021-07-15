package org.flightclub.engine.keyboard;

import java.util.HashMap;
import java.util.Map;

public class KeyboardState {
  private final Map<Integer, Boolean> isPressed;

  public KeyboardState() {
    this.isPressed = new HashMap<>();
  }

  public boolean isKeyDown(int keycode) {
    return this.isPressed.containsKey(keycode);
  }

  public void keyDown(int keycode) {
    this.isPressed.put(keycode, true);
  }

  public void keyUp(int keycode) {
    this.isPressed.remove(keycode);
  }

  public boolean anyKeyDown(int ...codes) {
    for (int i = 0; i < codes.length; i++) {
      if (isKeyDown(codes[i])) {
        return true;
      }
    }
    return false;
  }
}
