package org.flightclub.engine.keyboard;

import org.flightclub.engine.XcGame;
import org.flightclub.engine.events.KeyEvent;
import org.flightclub.engine.events.KeyEventHandler;

public class GameControl implements KeyEventHandler {
  private final XcGame game;

  public GameControl(final XcGame game) {
    this.game = game;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int key = e.code();
    switch (key) {
      case KeyEvent.VK_P:
        game.togglePause();
        break;

      case KeyEvent.VK_Y:
        game.startPlay();
        break;

      case KeyEvent.VK_Q:
        game.toggleFastForward();
        break;

      default:
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {

  }
}
