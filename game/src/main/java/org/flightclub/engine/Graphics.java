/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

public interface Graphics {
  void setColor(Color color);

  void setFont(Font font);

  void drawLine(int x1, int y1, int x2, int y2);

  void drawString(String str, int x, int y);

  void fillCircle(int x, int y, int diameter);

  void fillPolygon(int[] pointsX, int[] pointsY, int pointsN);
}
