package org.flightclub.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AudioPlayerImpl implements AudioPlayer {
  private static final Logger LOG = LoggerFactory.getLogger(AudioPlayerImpl.class);

  private final Map<String, Clip> resourceToClip = new HashMap<>();

  @Override
  public void play(final String resourceName) {
    getClip(resourceName).ifPresent(clip -> {
      LOG.debug("Playing {}", resourceName);
      clip.setFramePosition(0);
      clip.start();
    });
  }

  private Optional<Clip> getClip(final String resourceName) {
    final Clip storedClip = this.resourceToClip.get(resourceName);
    if (storedClip != null) {
      return Optional.of(storedClip);
    }

    loadClip(resourceName).ifPresent(clip -> this.resourceToClip.put(resourceName, clip));
    return Optional.ofNullable(this.resourceToClip.get(resourceName));
  }

  private Optional<Clip> loadClip(
      final String resourceName
  ) {
    try {
      if (resourceName == null || resourceName.isEmpty() || !resourceName.endsWith(".wav")) {
        throw new RuntimeException(String.format("%s is not a .wav file", resourceName));
      }

      final URL url = this.getClass().getClassLoader().getResource(resourceName);
      final AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
      final Clip clip = AudioSystem.getClip();
      clip.open(audioIn);

      LOG.debug("Loaded audio resource {}", resourceName);

      return Optional.of(clip);

    } catch (final Exception ex) {
      LOG.error("Could not load clip for resource", ex);

      return Optional.empty();
    }
  }
}
