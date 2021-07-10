/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.core;

public record Font(String name, int style, int size) {
  /**
   * The plain style constant.
   */
  public static final int PLAIN = 0;

  /**
   * The bold style constant.  This can be combined with the other style
   * constants (except PLAIN) for mixed styles.
   */
  public static final int BOLD = 1;

  /**
   * The italicized style constant.  This can be combined with the other
   * style constants (except PLAIN) for mixed styles.
   */
  public static final int ITALIC = 2;
}
