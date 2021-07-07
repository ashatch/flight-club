package org.flightclub.engine;

public class GameModelHolder {
  private GameMode mode;

  public GameModelHolder(final GameMode initialMode) {
    this.mode = initialMode;
  }

  public GameMode getMode() {
    return mode;
  }

  public void setMode(final GameMode mode) {
    this.mode = mode;
  }
}
