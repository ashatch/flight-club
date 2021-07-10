/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt;

import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.Font;
import org.flightclub.engine.core.Graphics;

public class AwtGraphics implements Graphics {

  private final java.awt.Graphics gfx;

  public AwtGraphics(java.awt.Graphics gfx) {
    this.gfx = gfx;
  }

  @Override
  public void setColor(Color color) {
    gfx.setColor(new java.awt.Color(color.r(), color.g(), color.b()));
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2) {
    gfx.drawLine(x1, y1, x2, y2);
  }

  @Override
  public void setFont(Font font) {
    gfx.setFont(toAwtFont(font));
  }

  @Override
  public void drawString(String str, int x, int y) {
    gfx.drawString(str, x, y);
  }

  @Override
  public void fillPolygon(int[] pointsX, int[] pointsY, int pointsN) {
    gfx.fillPolygon(pointsX, pointsY, pointsN);
  }

  @Override
  public void fillCircle(int x, int y, int diameter) {
    gfx.fillOval(x, y, diameter, diameter);
  }

  private java.awt.Font toAwtFont(Font f) {
    return new java.awt.Font(f.name(), f.style(), f.size());
  }
}
