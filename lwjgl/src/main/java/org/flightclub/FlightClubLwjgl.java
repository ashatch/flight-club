package org.flightclub;

import imgui.ImGui;
import imgui.app.Configuration;
import java.util.function.Supplier;
import org.flightclub.engine.GameItem;
import org.flightclub.engine.Mesh;
import org.flightclub.engine.ShaderProgram;
import org.flightclub.engine.Transformation;
import org.flightclub.meshes.CubeMesh;
import org.flightclub.shaders.StandardShader;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightClubLwjgl extends Window {
  private static final float FOV = (float) Math.toRadians(60.0f);

  private static final float Z_NEAR = 0.01f;

  private static final float Z_FAR = 1000.f;

  protected ShaderProgram shaderProgram;
  private Transformation transformation;

  private static final Logger LOG = LoggerFactory.getLogger(FlightClubLwjgl.class);
  private GameItem cubeGameItem;

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
    transformation = new Transformation();

    try {
      shaderProgram = new StandardShader();

      final Mesh cube = new CubeMesh();
      this.cubeGameItem = new GameItem(cube);
      cubeGameItem.setPosition(0, 0, -5);
      cubeGameItem.setRotation(2, 2, 2);
      cubeGameItem.setScale(1.0f);
      gameItems.add(cubeGameItem);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void updateState(final float deltaTimeSeconds) {
    this.cubeGameItem.getRotation().add(
        30f * deltaTimeSeconds,
        30f * deltaTimeSeconds,
        30f * deltaTimeSeconds
    );
  }

  @Override
  public void render() {
    this.render(this.shaderProgram);
  }

  public void render(
      final ShaderProgram shaderProgram
  ) {
    shaderProgram.bind();

    final Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(
        FOV,
        windowSize.x,
        windowSize.y,
        Z_NEAR,
        Z_FAR);

    shaderProgram.setUniform("projectionMatrix", projectionMatrix);

    gameItems.forEach(gameItem -> {
      final Matrix4f worldMatrix = transformation.getWorldMatrix(
          gameItem.getPosition(),
          gameItem.getRotation(),
          gameItem.getScale()
      );
      shaderProgram.setUniform("worldMatrix", worldMatrix);
      gameItem.getMesh().render();
    });

    shaderProgram.unbind();
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
