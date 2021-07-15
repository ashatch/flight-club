/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 */

package org.flightclub.awt;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.events.MouseTracker;
import org.flightclub.engine.keyboard.KeyboardState;

import static org.flightclub.awt.AwtKeyEventMapper.toEngineKeyEvent;

public class AwtEventAdapter {
    public static void registerEventListeners(
        final JFrame frame,
        final KeyboardState keyboardState,
        final EventManager eventManager,
        final MouseTracker mouseTracker
    ) {
      frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(final WindowEvent e) {
          System.exit(0);
        }
      });

      frame.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(final KeyEvent e) {
          org.flightclub.engine.events.KeyEvent keyEvent = toEngineKeyEvent(e);
          keyboardState.keyDown(keyEvent.code());
          eventManager.addEvent(keyEvent);
        }

        @Override
        public void keyReleased(final KeyEvent e) {
          org.flightclub.engine.events.KeyEvent keyEvent = toEngineKeyEvent(e);
          keyboardState.keyUp(keyEvent.code());
          eventManager.addEvent(toEngineKeyEvent(e));
        }
      });

      frame.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(final MouseEvent e) {
          mouseTracker.pressed(e.getX(), e.getY());
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
          mouseTracker.released();
        }
      });

      frame.addMouseMotionListener(new MouseMotionAdapter() {
        public void mouseDragged(final MouseEvent e) {
          mouseTracker.dragged(e.getX(), e.getY());
        }
      });
    }
}
