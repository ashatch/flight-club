/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

/*
 * interface to be implemented by objects (eg actors) that respond
 *to user pressing keys
 */
public interface KeyEventHandler {
  void keyPressed(KeyEvent e);

  void keyReleased(KeyEvent e);
}
