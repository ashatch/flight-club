package org.flightclub.awt.enginetest;

import java.util.Vector;
import org.flightclub.engine.GameLoopTarget;
import org.flightclub.engine.GameRenderer;
import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.GameEnvironment;
import org.flightclub.engine.core.Graphics;
import org.flightclub.engine.core.RenderContext;
import org.flightclub.engine.core.RenderManager;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.models.GliderShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGame implements GameLoopTarget, GameRenderer {
  private static final Logger LOG = LoggerFactory.getLogger(TestGame.class);

  private final RenderManager renderManager;
  private final EventManager eventManager;
  private final GameEnvironment gameEnvironment;
  private final Camera camera;
  private RenderContext renderContext;

  private final Vector<UpdatableGameObject> gameObjects = new Vector<>();

  public TestGame(
      final Camera camera,
      final RenderManager renderManager,
      final EventManager eventManager,
      final GameEnvironment gameEnvironment
  ) {
    this.camera = camera;
    this.renderManager = renderManager;
    this.eventManager = eventManager;
    this.gameEnvironment = gameEnvironment;

    final GliderShape someGlider = new GliderShape(Color.BLUE);

    this.renderManager.add(someGlider);
    camera.setEye(0, -1, -1);
    camera.setFocus(0, 0, 0);
  }

  public void addGameObject(final UpdatableGameObject gameObject) {
    this.gameObjects.add(gameObject);
  }

  @Override
  public void updateGameState(float delta) {
    final UpdateContext context = new UpdateContext(delta, 1.0f, this.renderManager);
    this.gameObjects.forEach(obj -> obj.update(context));
  }

  @Override
  public void setGameGraphics(Graphics graphics) {
    this.renderContext = new RenderContext(graphics, camera, null, this.gameEnvironment.windowSize(), false);
  }

  @Override
  public void render() {
    if (this.renderContext == null) {
      LOG.warn("No render context");
      return;
    }

    camera.setMatrix();
    this.renderManager.render(this.renderContext);
  }
}
