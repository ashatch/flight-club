/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;

public class XcGame implements KeyEventHandler, Clock.Observer {
  public static final int FRAME_RATE = 25;
  public static final float TIME_PER_FRAME = (float) (1.0 / FRAME_RATE) / 2;

  private final Font font = new Font("SansSerif", Font.PLAIN, 10);

  public final Clock clock = new Clock(1000 / FRAME_RATE);
  public final EventManager eventManager = new EventManager();
  public final Obj3dManager obj3dManager = new Obj3dManager();
  public final CameraMan cameraMan;

  private float time = 0.0f;
  public Landscape landscape;
  public Sky sky;
  public GameEnvironment envGameEnvironment;
  public float timeMultiplier = 1.0f;
  public GameMode gameMode;
  public String textMessage;

  protected float timePerFrame = TIME_PER_FRAME;

  private Vector<Glider> gaggle;
  private GliderUser gliderUser;
  private JetTrail jet1;
  private JetTrail jet2;

  private boolean fastForward = true;
  private Compass compass = null;
  private DataSlider slider = null;
  private Variometer vario;

  public XcGame(
      GameEnvironment envGameEnvironment
  ) {
    this.envGameEnvironment = envGameEnvironment;
    clock.addObserver(this);

    cameraMan = new CameraMan(this, envGameEnvironment.windowSize());

    eventManager.subscribe(this);

    sky = new Sky();
    landscape = new Landscape(this);

    gliderUser = new GliderUser(this, new Vector3d(0, 0, 0));
    gliderUser.landed();
    cameraMan.subject1 = gliderUser;

    vario = new Variometer(this, gliderUser);

    jet1 = new JetTrail(this, -JetTrail.TURN_RADIUS, -JetTrail.TURN_RADIUS);
    jet2 = new JetTrail(this, 0, JetTrail.TURN_RADIUS);
    jet2.makeFlyX();

    gaggle = new Vector<>();
    for (int i = 0; i < 10; i++) {
      Glider glider;
      if (i != 3 && i != 7) {
        glider = new Glider(this, new Vector3d());
      } else {
        //pink ones
        glider = new Glider(this, new Vector3d(), false, true);
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
    gameMode = GameMode.DEMO;
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
    clock.paused = !clock.paused;
  }

  public void start() {
    clock.start();
  }

  public void stop() {
    clock.stop();
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

    if (clock.paused) {
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
      compass = new Compass(25, gameWidth - 30, gameHeight - 15);
    }
    if (slider == null) {
      float vmax = -2 * Glider.SINK_RATE;
      slider = new DataSlider(
          -vmax,
          vmax,
          30,
          gameWidth - 60,
          gameHeight - 15
      );
      slider.label = "vario";
    }
  }

  @Override
  public void tick(final float delta) {
    time += delta * timeMultiplier / 2.0f;

    eventManager.processEvent();
    cameraMan.tick();

    updateCompass();
    updateSlider(delta);
    updateVario(delta);
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

      final String msg = clock.paused ? textMessage + " [ paused ]" : textMessage;
      g.drawString(msg, 15, height - 15);
    }
  }

  public float getTime() {
    return time;
  }
}

