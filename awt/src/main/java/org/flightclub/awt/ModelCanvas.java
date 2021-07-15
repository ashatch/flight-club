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
import javax.swing.JPanel;
import org.flightclub.engine.GameRenderer;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;

public class ModelCanvas extends JPanel implements UpdatableGameObject {
  private final Color backColor = Color.white;

  protected final GameRenderer gameRenderer;
  private Image imgBuffer;
  private Graphics graphicsBuffer;
  private org.flightclub.engine.core.Graphics gameGraphics;

  public ModelCanvas(
      final GameRenderer gameRenderer
  ) {
    this.gameRenderer = gameRenderer;
  }

  public void init() {
    imgBuffer = createImage(getWidth(), getHeight());
    graphicsBuffer = imgBuffer.getGraphics();
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
