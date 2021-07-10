/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */
package org.flightclub.engine.instruments;

import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.Font;
import org.flightclub.engine.core.Graphics;
import org.flightclub.engine.core.RenderContext;
import org.flightclub.engine.core.Renderable;

/*
 * a dot on a line - use for eg vario
 * minimal design - cf toshiba scan of fred
 */
public class DataSlider implements Renderable {
  // pixel space for label at bottom
  static final int dx = 2;
  static final int dy = 10;

  /* length of slider in pixels */
  private final int size;

  // screen coords of center point of slider
  private final int x;
  private final int y;
  private final Font font;

  // screen coords of v (v_min = 0, v_max = size)
  private int sliderValue;

  private final float minValue;
  private final float maxValue;
  private final String label;

  public DataSlider(
      final String label,
      final float minValue,
      final float maxValue,
      final int size,
      final int x,
      final int y
  ) {
    this.label = label;
    this.font = new Font("SansSerif", Font.PLAIN, 10);
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.size = size;
    this.x = x;
    this.y = y;
    setValue((this.minValue + this.maxValue) / 2);
  }

  public void setValue(float value) {
    float displayValue = Math.max(minValue, Math.min(maxValue, value));
    sliderValue = (int) (((displayValue - minValue) / (maxValue - minValue)) * size);
  }

  @Override
  public void render(final RenderContext context) {
    final Graphics graphics = context.graphics();

    graphics.setColor(Color.LIGHT_GRAY);
    graphics.drawLine(x - dx, y - size - dy, x + dx, y - size - dy);
    graphics.drawLine(x - dx, y - dy, x + dx, y - dy);
    graphics.drawLine(x, y - dy, x, y - size - dy);

    if (label != null) {
      graphics.setFont(font);
      graphics.drawString(label, x - 10, y);
    }

    graphics.setColor(Color.GRAY);
    graphics.fillCircle(x - 1, y - sliderValue - dy - 1, 3);
  }
}
