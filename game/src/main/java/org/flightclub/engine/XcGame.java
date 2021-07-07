/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;

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
  public float timeMultiplier = 1.0f;
  public GameMode gameMode;
  public String textMessage;

  protected float timePerFrame = TIME_PER_FRAME;

  private final Vector<Glider> gaggle;
  private final GliderUser gliderUser;
  private final JetTrail jet1;
  private final JetTrail jet2;

  private boolean fastForward = true;
  private Compass compass = null;
  private DataSlider slider = null;
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

    gliderUser = new GliderUser(this, this.sky, new Vector3d(0, 0, 0));
    gliderUser.landed();
    cameraMan.subject1 = gliderUser;

    vario = new Variometer(this, gliderUser);

    jet1 = new JetTrail(this, sky, -JetTrail.TURN_RADIUS, -JetTrail.TURN_RADIUS);
    jet2 = new JetTrail(this, sky, 0, JetTrail.TURN_RADIUS);
    jet2.makeFlyX();

    gaggle = new Vector<>();
    for (int i = 0; i < 10; i++) {
      Glider glider;
      if (i != 3 && i != 7) {
        glider = new Glider(this, this.sky, new Vector3d());
      } else {
        //pink ones
        glider = new Glider(this, this.sky, new Vector3d(), false, true);
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

    cameraMan.setEye(Landscape.TILE_WIDTH / 2, -Landscape.TILE_WIDTH / 4, 6);
    cameraMan.setFocus(0, 0, 0);

    launchGaggle();
    cameraMan.setMode(CameraMode.GAGGLE);
    textMessage = "Demo mode";
    gameModelHolder.setMode(GameMode.DEMO);
    toggleFastForward();
  }

  void launchGaggle() {
    for (int i = 0; i < gaggle.size(); i++) {
      Glider glider = gaggle.elementAt(i);
      glider.takeOff(new Vector3d(4 - i, 4 - i, (float) 1.5));
    }
  }

  void launchUser() {
    gliderUser.takeOff(new Vector3d(4 - 4 - 1, 4 - 6, (float) 1.8));
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
          c.update(new UpdateContext(delta, this.obj3dManager));
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

    gliderUser.triggerLoading = true;
    Glider glider = gaggle.elementAt(5);
    glider.triggerLoading = false;

    launchUser();
    launchGaggle();

    cameraMan.setEye(Landscape.TILE_WIDTH / 2, -Landscape.TILE_WIDTH / 4, 6);
    cameraMan.setFocus(0, 0, 0);

    cameraMan.setMode(CameraMode.SELF);
    createInstruments();

    jet1.buzzThis = gliderUser;
    jet2.buzzThis = gliderUser;

    if (paused) {
      togglePause();
    }
    if (fastForward) {
      toggleFastForward();
    }
  }

  void createInstruments() {
    final int gameWidth = envGameEnvironment.windowSize().x();
    final int gameHeight = envGameEnvironment.windowSize().y();

    if (compass == null) {
      compass = new Compass(25, gameWidth - 30, gameHeight - 35);
    }
    if (slider == null) {
      float vmax = -2 * Glider.SINK_RATE;
      slider = new DataSlider(
          "vario",
          -vmax,
          vmax,
          30,
          gameWidth - 60,
          gameHeight - 35
      );
    }
  }

  @Override
  public void update(final UpdateContext context) {
    time += context.deltaTime() * timeMultiplier / 2.0f;

    eventManager.processEvent();
    cameraMan.tick();

    updateCompass();
    updateSlider(context.deltaTime());
    updateVario(context.deltaTime());
  }

  private void updateVario(float delta) {
    vario.tick(delta);
  }

  private void updateSlider(float delta) {
    if (slider != null) {
      slider.setValue(2.0f * gliderUser.vector.posZ / (delta * timeMultiplier));
    }
  }

  private void updateCompass() {
    if (compass != null) {
      compass.setArrow(gliderUser.vector.posX, gliderUser.vector.posY);
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

      final String msg = paused ? textMessage + " [ paused ]" : textMessage;
      g.drawString(msg, 15, height - 15);
    }
  }

  public float getTime() {
    return time;
  }
}
