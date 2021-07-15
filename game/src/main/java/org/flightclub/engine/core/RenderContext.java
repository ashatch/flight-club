package org.flightclub.engine.core;

import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.math.Pair;

public class RenderContext {
  private final Graphics graphics;
  private final Camera camera;
  private final Pair screenSize;

  private boolean isPaused;

  public RenderContext(
      final Graphics graphics,
      final Camera camera,
      final Pair screenSize,
      final boolean isPaused
  ) {
    this.graphics = graphics;
    this.camera = camera;
    this.screenSize = screenSize;
    this.isPaused = isPaused;
  }

  public Graphics graphics() {
    return graphics;
  }

  public Pair<Integer, Integer> screenSize() {
    return screenSize;
  }

  public boolean isPaused() {
    return isPaused;
  }

  public void setPaused(boolean paused) {
    isPaused = paused;
  }

  public Camera camera() {
    return this.camera;
  }
}
