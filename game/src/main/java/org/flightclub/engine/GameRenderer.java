package org.flightclub.engine;

import org.flightclub.engine.core.Graphics;

public interface GameRenderer {
  void setGameGraphics(Graphics graphics);

  void render();
}
