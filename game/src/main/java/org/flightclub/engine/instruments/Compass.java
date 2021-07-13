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
 * a simple compass
 */
public class Compass implements Renderable {
  // head of arrow
  private final int[] hxs = {0, 2, -2};
  private final int[] hys = {5, 2, 2};

  // tail
  private final int[] txs = {0, 0};
  private final int[] tys = {1, -5};

  // rotate and translate above points
  private final int[] hxsPrime = new int[3];
  private final int[] hysPrime = new int[3];
  private final int[] txsPrime = new int[2];
  private final int[] tysPrime = new int[2];

  private final int theR;
  private final int x;
  private final int y;
  private final Font font;

  // vector determines arrow direction - start pointing north
  private float vx = 0;
  private float vy = 1;

  private final float[][] matrix = new float[2][2];

  private static final int H_NUM = 3;
  private static final int T_NUM = 2;

  // pixel space for label at bottom
  private static final int dy = 10;

  public Compass(int size, int x, int y) {
    theR = size / 2;
    this.x = x;
    this.y = y;
    this.font = new Font("SansSerif", Font.PLAIN, 10);
    init();
  }

  void init() {
    /*
     * scale arrow head and tail to size
     * also flip y coord (screen +y points down)
     */
    float s = (float) (theR - 2) / 5;

    for (int i = 0; i < H_NUM; i++) {
      hxs[i] = (int) (hxs[i] * s);
      hys[i] = (int) (hys[i] * -s);
    }

    for (int i = 0; i < T_NUM; i++) {
      txs[i] = (int) (txs[i] * s);
      tys[i] = (int) (tys[i] * -s);
    }
    updateArrow();
  }

  public void setArrow(float x, float y) {
    // +y is north
    // +x is east
    vx = x;
    vy = y;

    // normalize
    float d = (float) Math.hypot(x, y);
    vx = vx / d;
    vy = vy / d;

    // calc arrow points
    updateArrow();
  }

  void updateArrow() {
    // rotate
    matrix[0][0] = vy;
    matrix[0][1] = -vx;
    matrix[1][0] = -matrix[0][1];
    matrix[1][1] = matrix[0][0];

    // transform
    for (int i = 0; i < H_NUM; i++) {
      hxsPrime[i] = (int) (matrix[0][0] * hxs[i] + matrix[0][1] * hys[i] + x);
      hysPrime[i] = (int) (matrix[1][0] * hxs[i] + matrix[1][1] * hys[i] + y - dy - theR);
    }

    for (int i = 0; i < T_NUM; i++) {
      txsPrime[i] = (int) (matrix[0][0] * txs[i] + matrix[0][1] * tys[i] + x);
      tysPrime[i] = (int) (matrix[1][0] * txs[i] + matrix[1][1] * tys[i] + y - dy - theR);
    }
  }

  @Override
  public void render(final RenderContext context) {
    final Graphics graphics = context.graphics();

    graphics.setColor(Color.LIGHT_GRAY);
    graphics.drawLine(txsPrime[0], tysPrime[0], txsPrime[1], tysPrime[1]);

    graphics.setFont(font);
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.drawString("N", x - 3, y - theR * 2 - dy);
    graphics.drawString("S", x - 2, y);

    graphics.setColor(Color.GRAY);
    graphics.fillPolygon(hxsPrime, hysPrime, hxsPrime.length);
  }
}