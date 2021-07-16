/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;
import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.camera.CameraMan;
import org.flightclub.engine.camera.CameraMode;
import org.flightclub.engine.core.Font;
import org.flightclub.engine.core.GameEnvironment;
import org.flightclub.engine.core.GameMode;
import org.flightclub.engine.core.GameModeHolder;
import org.flightclub.engine.core.Graphics;
import org.flightclub.engine.core.RenderContext;
import org.flightclub.engine.core.RenderManager;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.instruments.Compass;
import org.flightclub.engine.instruments.DataSlider;
import org.flightclub.engine.instruments.TextMessage;
import org.flightclub.engine.instruments.Variometer;
import org.flightclub.engine.keyboard.CameraControl;
import org.flightclub.engine.keyboard.GameControl;
import org.flightclub.engine.keyboard.KeyboardState;
import org.flightclub.engine.keyboard.SkyControl;
import org.flightclub.engine.keyboard.UserGliderController;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.engine.Glider.regularNpcGlider;
import static org.flightclub.engine.Glider.rigidNpcGlider;
import static org.flightclub.engine.Glider.userGlider;

public class XcGame implements GameLoopTarget, GameRenderer {
  private static final Logger LOG = LoggerFactory.getLogger(XcGame.class);

  public static final int FRAME_RATE = 25;
  public static final float TIME_PER_FRAME = (float) (1.0 / FRAME_RATE) / 2;

  public final EventManager eventManager;
  public final RenderManager renderManager;
  public final CameraMan cameraMan;
  private final Camera camera;

  private float time = 0.0f;
  public Landscape landscape;
  public Sky sky;
  public GameEnvironment envGameEnvironment;
  private float timeMultiplier = 1.0f;
  public GameMode gameMode;

  protected float timePerFrame = TIME_PER_FRAME;

  private final Vector<Glider> gaggle;
  private final Glider userGlider;
  private final UserGliderController userGliderController;
  private final JetTrail jet1;
  private final JetTrail jet2;

  private boolean fastForward = true;
  private Compass compass;
  private DataSlider slider;
  private TextMessage textMessage;
  private final Variometer vario;

  final Vector<UpdatableGameObject> gameObjects = new Vector<>();
  boolean paused = false;

  private RenderContext renderContext;

  public XcGame(
      final RenderManager renderManager,
      final Camera camera,
      final EventManager eventManager,
      final KeyboardState keyboardState,
      final Sky sky,
      final GameModeHolder gameModeHolder,
      final GameEnvironment envGameEnvironment
  ) {
    this.eventManager = eventManager;
    this.renderManager = renderManager;
    this.envGameEnvironment = envGameEnvironment;
    this.camera = camera;

    this.sky = sky;
    landscape = new Landscape(this, this.sky);
    cameraMan = new CameraMan(camera, gameModeHolder, landscape);

    this.userGlider = userGlider(this, sky);
    userGlider.landed();

    userGliderController = new UserGliderController(userGlider, keyboardState);
    addGameObject(userGliderController);
    this.eventManager.subscribe(userGliderController);
    cameraMan.setSubject1(userGlider);

    textMessage = new TextMessage("Demo mode", new Font("SansSerif", Font.PLAIN, 10));
    renderManager.addRenderable(textMessage);

    compass = new Compass(25, envGameEnvironment.windowSize().x() - 30, envGameEnvironment.windowSize().y() - 35);
    renderManager.addRenderable(compass);

    float vmax = -2 * Glider.SINK_RATE;
    slider = new DataSlider(
        "vario",
        -vmax,
        vmax,
        30,
        envGameEnvironment.windowSize().x() - 60,
        envGameEnvironment.windowSize().y() - 35
    );
    renderManager.addRenderable(slider);

    vario = new Variometer(this, userGlider);

    jet1 = new JetTrail(this, sky, -JetTrail.TURN_RADIUS, -JetTrail.TURN_RADIUS);
    jet2 = new JetTrail(this, sky, 0, JetTrail.TURN_RADIUS);
    jet2.makeFlyX();

    gaggle = new Vector<>();
    for (int i = 0; i < 10; i++) {
      Glider glider;
      if (i != 3 && i != 7) {
        glider = regularNpcGlider(this, sky);
      } else {
        glider = rigidNpcGlider(this, sky);
      }
      gaggle.addElement(glider);
      if (i == 5) {
        cameraMan.setSubject2(glider);
        glider.triggerLoading = true;
        jet1.buzzThis = glider;
        jet2.buzzThis = glider;
      }
      //glider.triggerLoading = true;//????
    }

    camera.setEye(Landscape.TILE_WIDTH / 2f, -Landscape.TILE_WIDTH / 4f, 6);
    camera.setFocus(0, 0, 0);

    launchGaggle();
    cameraMan.setMode(CameraMode.GAGGLE);
    gameModeHolder.setMode(GameMode.DEMO);
    toggleFastForward();

    this.eventManager.subscribe(new CameraControl(cameraMan, camera));
    this.eventManager.subscribe(new SkyControl(sky));
    this.eventManager.subscribe(new GameControl(this));
  }

  public TextMessage getTextMessage() {
    return textMessage;
  }

  void launchGaggle() {
    for (int i = 0; i < gaggle.size(); i++) {
      Glider glider = gaggle.elementAt(i);
      glider.takeOff(new Vector3f(4 - i, 4 - i, (float) 1.5));
    }
  }

  void launchUser() {
    userGlider.takeOff(new Vector3f(4 - 4 - 1, 4 - 6, (float) 1.8));
    time = 0;
  }

  public void togglePause() {
    this.paused = !paused;
    this.renderContext.setPaused(this.paused);
  }

  @Override
  public void setGameGraphics(final Graphics gameGraphics) {
    this.renderContext = new RenderContext(
        gameGraphics,
        camera,
        this.envGameEnvironment.windowSize(),
        paused
    );
  }

  @Override
  public void render() {
    if (this.renderContext == null) {
      LOG.warn("Waiting for graphics context");
      return;
    }

    camera.setMatrix();
    renderManager.render(this.renderContext);
  }

  @Override
  public void updateGameState(final float delta) {
    final UpdateContext context = new UpdateContext(delta, this.timeMultiplier, this.renderManager);

    this.update(context);

    if (paused) {
      return;
    }

    for (int i = 0; i < this.gameObjects.size(); i++) {
      this.gameObjects.elementAt(i).update(context);
    }
  }

  public void addGameObject(final UpdatableGameObject observer) {
    gameObjects.addElement(observer);
  }

  public void removeGameObject(final UpdatableGameObject observer) {
    gameObjects.removeElement(observer);
  }

  public void startPlay() {
    gameMode = GameMode.USER;
    landscape.removeAll();

    userGlider.triggerLoading = true;
    Glider glider = gaggle.elementAt(5);
    glider.triggerLoading = false;

    launchUser();
    launchGaggle();

    camera.setEye(Landscape.TILE_WIDTH / 2f, -Landscape.TILE_WIDTH / 4f, 6);
    camera.setFocus(0, 0, 0);

    cameraMan.setMode(CameraMode.SELF);

    jet1.buzzThis = userGlider;
    jet2.buzzThis = userGlider;

    if (paused) {
      togglePause();
    }
    if (fastForward) {
      toggleFastForward();
    }
  }

  public void update(final UpdateContext context) {
    time += context.deltaTime() * timeMultiplier / 2.0f;

    eventManager.processEvent();
    cameraMan.tick();

    updateCompass();
    updateSlider(context.deltaTime());
    vario.tick(context);
  }

  private void updateSlider(final float delta) {
    if (slider != null) {
      slider.setValue(2.0f * userGlider.vector.z / (delta * timeMultiplier));
    }
  }

  private void updateCompass() {
    if (compass != null) {
      compass.setArrow(userGlider.vector.x, userGlider.vector.y);
    }
  }

  /*
   * how much model time passes each second of game play
   */
  public void toggleFastForward() {
    fastForward = !fastForward;
    if (fastForward) {
      //2.5 minutes per second
      timeMultiplier = 5.0f;
    } else {
      //0.5 minutes per second
      timeMultiplier = 1.0f;
    }
    timePerFrame = TIME_PER_FRAME * timeMultiplier;
  }

  public float getTime() {
    return time;
  }
}
