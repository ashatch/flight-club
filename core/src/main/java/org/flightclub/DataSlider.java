/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub;

import org.flightclub.compat.Color;
import org.flightclub.compat.Font;
import org.flightclub.compat.Graphics;

/*
 * a dot on a line - use for eg vario
 * minimal design - cf toshiba scan of fred
 */
public class DataSlider {
  /* length of slider in pixels */
  final int size;

  // screen coords of center point of slider
  final int x0;
  final int y0;

  // value to display
  private float displayValue;

  // screen coords of v (v_min = 0, v_max = size)
  int screenCoords;

  final float minValue;
  final float maxValue;

  String label = null;

  // default radius of 10
  static final int SIZE_DEFAULT = 20;

  // pixel space for label at bottom
  static final int dx = 2;
  static final int dy = 10;

  public DataSlider(float inMin, float inMax, int inSize, int inX0, int inY0) {
    minValue = inMin;
    maxValue = inMax;
    size = inSize;
    x0 = inX0;
    y0 = inY0;
    setValue((minValue + maxValue) / 2);
  }

  public DataSlider() {
    this(-1, 1, SIZE_DEFAULT, 50, 42);
  }

  void setValue(float inV) {
    // clamp value and convert to 'screen' coords
    displayValue = inV;
    if (displayValue <= minValue) {
      displayValue = minValue;
    }
    if (inV >= maxValue) {
      displayValue = maxValue;
    }

    displayValue = inV;
    screenCoords = (int) (((displayValue - minValue) / (maxValue - minValue)) * size);
  }

  public void draw(Graphics g) {

    g.setColor(Color.LIGHT_GRAY);
    g.drawLine(x0 - dx, y0 - size - dy, x0 + dx, y0 - size - dy);
    g.drawLine(x0 - dx, y0 - dy, x0 + dx, y0 - dy);
    g.drawLine(x0, y0 - dy, x0, y0 - size - dy);

    if (label != null) {
      Font font = new Font("SansSerif", Font.PLAIN, 10);
      g.setFont(font);
      g.drawString(label, x0 - 10, y0);
    }

    g.setColor(Color.GRAY);
    g.fillCircle(x0 - 1, y0 - screenCoords - dy - 1, 3);
  }
}