package org.flightclub.engine.core;

public class GameModeHolder {
  private GameMode mode;

  public GameModeHolder(final GameMode initialMode) {
    this.mode = initialMode;
  }

  public GameMode getMode() {
    return mode;
  }

  public void setMode(final GameMode mode) {
    this.mode = mode;
  }
}
