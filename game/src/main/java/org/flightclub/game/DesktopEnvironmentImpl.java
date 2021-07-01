package org.flightclub.game;

import org.flightclub.engine.DesktopEnvironment;

import java.awt.*;

class DesktopEnvironmentImpl implements DesktopEnvironment {
  private final AudioPlayer audioPlayer;
  private final Dimension windowSize;

  public DesktopEnvironmentImpl(
      final Dimension windowSize,
      final AudioPlayer audioPlayer
  ) {
    this.windowSize = windowSize;
    this.audioPlayer = audioPlayer;
  }


  @Override
  public Dimension getWindowSize() {
    return windowSize;
  }

  @Override
  public void playSoundResource(final String soundResource) {
    audioPlayer.play(soundResource);
  }
}
