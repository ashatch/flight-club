package org.flightclub;

import imgui.ImGui;
import imgui.app.Configuration;
import java.util.function.Supplier;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightClubLwjgl extends Window {
  private static final Logger LOG = LoggerFactory.getLogger(FlightClubLwjgl.class);

  public void launch(final Supplier<Configuration> configurationSupplier) {
    init(configurationSupplier.get());
    setup();
    run();
    cleanup();
    dispose();
  }

  @Override
  protected void cleanup() {
    LOG.info("Cleaning up");
  }

  @Override
  protected void setup() {
    LOG.info("Setting up");
  }

  @Override
  public void render() {

  }

  @Override
  public void renderGUI() {
    ImGui.showDemoWindow();
  }

  public static void main(final String[] args) {
    LOG.info("Starting FlightClub");

    new FlightClubLwjgl()
        .launch(
            () -> {
              final Configuration config = new Configuration();
              config.setTitle("FlightClub");
              return config;
            });
  }
}
