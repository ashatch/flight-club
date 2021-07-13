/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;
import org.flightclub.engine.GameRenderer;
import org.flightclub.engine.XcGame;
import org.flightclub.engine.core.RenderContext;
import org.flightclub.engine.core.Renderable;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.events.MouseTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.awt.AwtKeyEventMapper.toEngineKeyEvent;

/*
 * canvas manager - draws world, dragging on canvas moves camera
 *
 * This class is based on the framework outlined in a book called
 * 'Java Games Programming' by Niel Bartlett
 */
public class ModelCanvas extends JPanel implements UpdatableGameObject {
  private final Logger LOG = LoggerFactory.getLogger(ModelCanvas.class);

  private final Color backColor = Color.white;

  private final EventManager eventManager;
  private final MouseTracker mouseTracker;
  protected final GameRenderer gameRenderer;
  private Image imgBuffer;
  private Graphics graphicsBuffer;
  private org.flightclub.engine.core.Graphics gameGraphics;

  public ModelCanvas(
      final EventManager eventManager,
      final MouseTracker mouseTracker,
      final GameRenderer gameRenderer
  ) {
    this.eventManager = eventManager;
    this.mouseTracker = mouseTracker;
    this.gameRenderer = gameRenderer;
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

    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        boolean eventAdded = eventManager.addEvent(toEngineKeyEvent(e));
        if (!eventAdded) {
          LOG.warn("Did not register keyPressed " + e);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        boolean eventAdded = eventManager.addEvent(toEngineKeyEvent(e));
        if (!eventAdded) {
          LOG.warn("Did not register keyReleased " + e);
        }
      }
    });
  }

  @Override
  public void paint(final Graphics g) {
    if (this.imgBuffer == null) {
      return;
    }

    if (this.gameGraphics == null) {
      this.gameGraphics = new AwtGraphics(this.graphicsBuffer);
      this.gameRenderer.setGameGraphics(gameGraphics);
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

    this.gameRenderer.render();
  }

  @Override
  public void update(UpdateContext context) {
    repaint();
  }
}
