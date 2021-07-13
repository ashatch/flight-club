package org.flightclub.engine;

public class GameLoop {
  private final GameLoopTarget target;
  private final int sleepTime;

  public GameLoop(
      final GameLoopTarget target,
      final float framesPerSecond
  ) {
    this.target = target;
    this.sleepTime = (int)(1000.0f / framesPerSecond);
  }

  public void gameLoop() {
    long last = 0;
    do {
      long now = System.currentTimeMillis();
      float delta = (now - last) / 1000.0f;
      last = now;

      target.updateGameState(delta);
      frameSleep(now);
    } while (true);
  }

  private void frameSleep(final long now) {
    long timeLeft = sleepTime + now - System.currentTimeMillis();
    if (timeLeft > 0) {
      try {
        Thread.sleep(timeLeft);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
