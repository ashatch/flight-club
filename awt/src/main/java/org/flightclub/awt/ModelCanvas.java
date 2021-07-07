/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import org.flightclub.engine.MouseTracker;
import org.flightclub.engine.UpdatableGameObject;
import org.flightclub.engine.XcGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.awt.AwtKeyEventMapper.toEngineKeyEvent;

/*
 * canvas manager - draws world, dragging on canvas moves camera
 *
 * This class is based on the framework outlined in a book called
 * 'Java Games Programming' by Niel Bartlett
 */
public class ModelCanvas extends Canvas implements UpdatableGameObject {
  private final Logger LOG = LoggerFactory.getLogger(ModelCanvas.class);

  private final Color backColor = Color.white;
  private final MouseTracker mouseTracker = new MouseTracker();

  protected final XcGame game;
  private Image imgBuffer;
  private Graphics graphicsBuffer;
  private org.flightclub.engine.Graphics gameGraphics;

  public ModelCanvas(final XcGame game) {
    this.game = game;
    this.game.addGameObject(this);
  }

  public void init() {
    imgBuffer = createImage(getWidth(), getHeight());
    graphicsBuffer = imgBuffer.getGraphics();

    //event handlers
    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        mouseTracker.pressed(e.getX(), e.getY());
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        mouseTracker.released();
      }
    });

    this.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        mouseTracker.dragged(e.getX(), e.getY());
      }
    });

    // Add key listeners for XCGameApplet implementation
    // (XCGameFrame is listening by itself)
    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (!game.eventManager.addEvent(toEngineKeyEvent(e))) {
          LOG.warn("Did not register keyPressed " + e);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        if (!game.eventManager.addEvent(toEngineKeyEvent(e))) {
          LOG.warn("Did not register keyReleased " + e);
        }
      }
    });
  }

  @Override
  public void update(final float delta) {
    if (mouseTracker.isDragging()) {
      //float dtheta = (float) dx/width;
      float dtheta = 0;
      float dz = 0;
      float unitStep = (float) Math.PI * delta / 8; //4 seconds to 90 - sloow!

      if (mouseTracker.getDeltaX() > 20) {
        dtheta = -unitStep;
      }

      if (mouseTracker.getDeltaX() < -20) {
        dtheta = unitStep;
      }

      if (mouseTracker.getDeltaY() > 20) {
        dz = delta / 4;
      }

      if (mouseTracker.getDeltaY() < -20) {
        dz = -delta / 4;
      }

      game.cameraMan.rotateEyeAboutFocus(-dtheta);
      game.cameraMan.translateZ(-dz);
    }

    repaint();
  }

  @Override
  public void paint(final Graphics g) {
    if (this.imgBuffer == null) {
      return;
    }

    if (this.gameGraphics == null) {
      this.gameGraphics = new AwtGraphics(this.graphicsBuffer);
    }

    updateImgBuffer(this.graphicsBuffer);
    g.drawImage(this.imgBuffer, 0, 0, this);
  }

  @Override
  public void update(final Graphics g) {
    paint(g);
  }

  public void updateImgBuffer(final Graphics g) {
    g.setColor(backColor);
    g.fillRect(0, 0, getWidth(), getHeight());

    this.game.draw(gameGraphics, getWidth(), getHeight());
  }
}
