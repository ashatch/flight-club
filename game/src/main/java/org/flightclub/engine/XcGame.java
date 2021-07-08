/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;
import org.flightclub.engine.camera.CameraMan;
import org.flightclub.engine.camera.CameraMode;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.events.KeyEvent;
import org.flightclub.engine.events.KeyEventHandler;
import org.flightclub.engine.instruments.Compass;
import org.flightclub.engine.instruments.DataSlider;
import org.flightclub.engine.instruments.TextMessage;
import org.flightclub.engine.instruments.Variometer;
import org.flightclub.engine.math.Vector3d;

import static org.flightclub.engine.Glider.regularNpcGlider;
import static org.flightclub.engine.Glider.rigidNpcGlider;
import static org.flightclub.engine.Glider.userGlider;

public class XcGame implements KeyEventHandler, UpdatableGameObject {
  public static final int FRAME_RATE = 25;
  public static final float TIME_PER_FRAME = (float) (1.0 / FRAME_RATE) / 2;

  private final Font font = new Font("SansSerif", Font.PLAIN, 10);

  public final EventManager eventManager = new EventManager();
  public final Obj3dManager obj3dManager;
  public final CameraMan cameraMan;

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
  private Compass compass = null;
  private DataSlider slider = null;
  private TextMessage textMessage;
  private final Variometer vario;

  final Vector<UpdatableGameObject> gameObjects = new Vector<>();
  final int sleepTime = 1000 / FRAME_RATE;
  public long last = 0;
  boolean paused = false;


  public XcGame(
      final Obj3dManager obj3dManager,
      final Sky sky,
      final GameModelHolder gameModelHolder,
      final GameEnvironment envGameEnvironment
  ) {
    this.obj3dManager = obj3dManager;
    this.envGameEnvironment = envGameEnvironment;
    this.sky = sky;
    landscape = new Landscape(this, this.sky);
    cameraMan = new CameraMan(gameModelHolder, landscape, envGameEnvironment.windowSize());

    eventManager.subscribe(this);
    addGameObject(this);

    this.userGlider = userGlider(this, sky);
    userGlider.landed();

    userGliderController = new UserGliderController(userGlider);
    this.eventManager.subscribe(userGliderController);
    cameraMan.subject1 = userGlider;

    textMessage = new TextMessage("Demo mode");
    compass = new Compass(25, envGameEnvironment.windowSize().x() - 30, envGameEnvironment.windowSize().y() - 35);
    float vmax = -2 * Glider.SINK_RATE;
    slider = new DataSlider(
        "vario",
        -vmax,
        vmax,
        30,
        envGameEnvironment.windowSize().x() - 60,
        envGameEnvironment.windowSize().y() - 35
    );

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
        cameraMan.subject2 = glider;
        glider.triggerLoading = true;
        jet1.buzzThis = glider;
        jet2.buzzThis = glider;
      }
      //glider.triggerLoading = true;//????
    }

    cameraMan.setEye(Landscape.TILE_WIDTH / 2f, -Landscape.TILE_WIDTH / 4f, 6);
    cameraMan.setFocus(0, 0, 0);

    launchGaggle();
    cameraMan.setMode(CameraMode.GAGGLE);
    gameModelHolder.setMode(GameMode.DEMO);
    toggleFastForward();
  }

  public TextMessage getTextMessage() {
    return textMessage;
  }

  void launchGaggle() {
    for (int i = 0; i < gaggle.size(); i++) {
      Glider glider = gaggle.elementAt(i);
      glider.takeOff(new Vector3d(4 - i, 4 - i, (float) 1.5));
    }
  }

  void launchUser() {
    userGlider.takeOff(new Vector3d(4 - 4 - 1, 4 - 6, (float) 1.8));
    time = 0;
  }

  void togglePause() {
    paused = !paused;
  }

  public void gameLoop() {
    do {
      long now = System.currentTimeMillis();
      float delta = (now - last) / 1000.0f;
      last = now;

      for (int i = 0; i < gameObjects.size(); i++) {
        /* hack - when paused still tick the modelviewer so
            we can change our POV and unpause */
        if (i == 0 || !paused) {
          UpdatableGameObject c = gameObjects.elementAt(i);
          c.update(new UpdateContext(delta, this.timeMultiplier, this.obj3dManager));
        }
      }

      long timeLeft = sleepTime + now - System.currentTimeMillis();
      if (timeLeft > 0) {
        try {
          Thread.sleep(timeLeft);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    } while (true);
  }

  public void addGameObject(UpdatableGameObject observer) {
    gameObjects.addElement(observer);
  }

  public void removeGameObject(UpdatableGameObject observer) {
    gameObjects.removeElement(observer);
  }

  void startPlay() {
    gameMode = GameMode.USER;
    landscape.removeAll();

    userGlider.triggerLoading = true;
    Glider glider = gaggle.elementAt(5);
    glider.triggerLoading = false;

    launchUser();
    launchGaggle();

    cameraMan.setEye(Landscape.TILE_WIDTH / 2f, -Landscape.TILE_WIDTH / 4f, 6);
    cameraMan.setFocus(0, 0, 0);

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


  @Override
  public void update(final UpdateContext context) {
    time += context.deltaTime() * timeMultiplier / 2.0f;

    eventManager.processEvent();
    cameraMan.tick();

    updateCompass();
    updateSlider(context.deltaTime());
    vario.tick(context);
  }

  private void updateSlider(float delta) {
    if (slider != null) {
      slider.setValue(2.0f * userGlider.vector.posZ / (delta * timeMultiplier));
    }
  }

  private void updateCompass() {
    if (compass != null) {
      compass.setArrow(userGlider.vector.posX, userGlider.vector.posY);
    }
  }

  /*
   * how much model time passes each second of game play
   */
  void toggleFastForward() {
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

  @Override
  public void keyPressed(KeyEvent e) {
    int key = e.code();
    switch (key) {
      case KeyEvent.VK_P:
        togglePause();
        break;

      case KeyEvent.VK_Y:
        startPlay();
        break;

      case KeyEvent.VK_Q:
        toggleFastForward();
        break;

      case KeyEvent.VK_H:
        sky.setHigh();
        break;

      case KeyEvent.VK_G:
        sky.setLow();
        break;

      case KeyEvent.VK_K:
        cameraMan.move(-CameraMan.CAMERA_MOVEMENT_DELTA, 0);
        return;
      case KeyEvent.VK_L:
        cameraMan.move(CameraMan.CAMERA_MOVEMENT_DELTA, 0);
        return;
      case KeyEvent.VK_M:
        cameraMan.move(0, CameraMan.CAMERA_MOVEMENT_DELTA);
        return;
      case KeyEvent.VK_N:
        cameraMan.move(0, -CameraMan.CAMERA_MOVEMENT_DELTA);
        return;

      case KeyEvent.VK_1:
        cameraMan.setMode(CameraMode.SELF);
        return;
      case KeyEvent.VK_2:
        cameraMan.setMode(CameraMode.GAGGLE);
        return;
      case KeyEvent.VK_3:
        cameraMan.setMode(CameraMode.PLAN);
        return;
      case KeyEvent.VK_4:
        cameraMan.setMode(CameraMode.TILE);
        return;

      default:
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  public void draw(final Graphics g, final int width, final int height) {
    //TODO optimize - build vector of objs in FOV, need only draw these
    cameraMan.setMatrix();

    obj3dManager.sort()
        .forEach(layer ->
            layer.forEach(obj -> {
              obj.film(cameraMan);
              obj.draw(g, cameraMan);
            })
        );

    renderTextMessage(g, height);
    renderCompass(g);
    renderSlider(g);
  }

  private void renderCompass(final Graphics g) {
    if (compass != null) {
      compass.draw(g);
    }
  }

  private void renderSlider(final Graphics g) {
    if (slider != null) {
      slider.draw(g);
    }
  }

  private void renderTextMessage(final Graphics g, final int height) {
    if (textMessage != null) {
      g.setFont(font);
      g.setColor(Color.LIGHT_GRAY);
      g.drawString(textMessage.getMessage(paused), 15, height - 15);
    }
  }

  public float getTime() {
    return time;
  }
}
