package org.flightclub;

import imgui.ImGui;
import imgui.app.Configuration;
import java.util.function.Supplier;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightClubLwjgl extends Window {
  private static final float FOV = (float) Math.toRadians(60.0f);

  private static final float Z_NEAR = 0.01f;

  private static final float Z_FAR = 1000.f;

  private Transformation transformation;

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
    transformation = new Transformation();
  }

  @Override
  public void render() {
    this.render(this.shaderProgram);
  }

  public void render(
      final ShaderProgram shaderProgram
//      final Mesh mesh
  ) {
    shaderProgram.bind();

    final Matrix4f projectionMatrix = this.transformation.getProjectionMatrix(FOV, 1024, 768, Z_NEAR, Z_FAR);
    shaderProgram.setUniform("projectionMatrix", projectionMatrix);

    gameItems.forEach(gameItem -> {
      final Matrix4f worldMatrix = transformation.getWorldMatrix(
          gameItem.getPosition(),
          gameItem.getRotation(),
          gameItem.getScale()
//          new Vector3f(0, 0, -5),
//          new Vector3f(2, 10, 0),
//          1.0f
      );
      shaderProgram.setUniform("worldMatrix", worldMatrix);
      gameItem.getMesh().render();
    });

    // Draw the mesh
//    glBindVertexArray(mesh.getVaoId());
//    glEnableVertexAttribArray(0);
//    glEnableVertexAttribArray(1);
//    glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
//
//    // Restore state
//    glDisableVertexAttribArray(0);
//    glBindVertexArray(0);

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
