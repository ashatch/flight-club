package org.flightclub;

import imgui.ImGui;
import imgui.app.Configuration;
import java.util.function.Supplier;
import org.flightclub.engine.GameItem;
import org.flightclub.engine.Mesh;
import org.flightclub.engine.ShaderProgram;
import org.flightclub.engine.Transformation;
import org.flightclub.meshes.GliderMesh;
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
  private GameItem gliderGameItem;

  private final float[] gliderAngle = new float[3];
  private final float[] lightPosition = new float[]{-1, 1, 0};

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
      this.updateLightPosition();

      final Mesh gliderMesh = new GliderMesh();
      this.gliderGameItem = new GameItem(gliderMesh);
      gliderGameItem.setPosition(0, 0, -5);
      gliderGameItem.setRotation(2, 2, 2);
      gliderGameItem.setScale(1.0f);
      gameItems.add(gliderGameItem);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateLightPosition() {
    shaderProgram.setUniform3f("lightPos", this.lightPosition);
  }

  private void updateGliderRotation() {
    this.gliderGameItem.setRotation(this.gliderAngle[0], this.gliderAngle[1], this.gliderAngle[2]);
    //    this.gliderGameItem.getRotation().add(
    //        30f * deltaTimeSeconds,
    //        30f * deltaTimeSeconds,
    //        30f * deltaTimeSeconds
    //    );
  }

  @Override
  protected void updateState(final float deltaTimeSeconds) {
    this.updateGliderRotation();
    this.updateLightPosition();
  }

  @Override
  public void render() {
    this.render(this.shaderProgram);
  }

  public void render(
      final ShaderProgram shaderProgram
  ) {
    shaderProgram.bind();

    final Matrix4f projectionMatrix = transformation.getProjectionMatrix(
        FOV,
        this.windowSize.x,
        this.windowSize.y,
        Z_NEAR,
        Z_FAR);

    shaderProgram.setUniform("projectionMatrix", projectionMatrix);
    final Matrix4f viewMatrix = transformation.getViewMatrix(camera);

    shaderProgram.setUniform("projectionMatrix", projectionMatrix);

    gameItems.forEach(gameItem -> {
      final Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
      shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
      gameItem.getMesh().render();
    });

    shaderProgram.unbind();
  }

  @Override
  public void renderGUI() {
    //ImGui.showDemoWindow();
    ImGui.begin("Scene");
    ImGui.sliderFloat3("Glider angle", gliderAngle, 0f, 360f);
    ImGui.sliderFloat3("Light position", lightPosition, -10f, 10f);
    ImGui.end();
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
