/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt;

import java.awt.event.KeyEvent;

public class AwtKeyEventMapper {
  public static org.flightclub.engine.events.KeyEvent toEngineKeyEvent(final KeyEvent e) {
    int type = e.getID() == KeyEvent.KEY_PRESSED
        ? org.flightclub.engine.events.KeyEvent.TYPE_KEY_PRESSED
        : org.flightclub.engine.events.KeyEvent.TYPE_KEY_RELEASED;

    return new org.flightclub.engine.events.KeyEvent(type, e.getKeyCode());
  }
}
