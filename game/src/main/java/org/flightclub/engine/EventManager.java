/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

public class EventManager {
  private static final int MAX_QUEUE_LENGTH = 20;

  private final Vector<KeyEventHandler> subscribers = new Vector<>();
  private final Queue<KeyEvent> events = new LinkedList<>();

  /*
   * add an object to the list of objects to be
   * notified when an event happens
   */
  public void subscribe(KeyEventHandler i) {
    subscribers.add(i);
  }

  public void unsubscribe(KeyEventHandler i) {
    subscribers.remove(i);
  }

  /*
   * add event to queue
   */
  public boolean addEvent(KeyEvent e) {
    if (events.size() >= MAX_QUEUE_LENGTH) {
      return false;
    }

    events.add(e);
    return true;
  }

  /*
   * process event at head of the queue
   */
  public void processEvent() {
    KeyEvent e = events.poll();
    if (e == null) {
      return;
    }

    for (KeyEventHandler i : subscribers) {
      if (e.type() == KeyEvent.TYPE_KEY_RELEASED) {
        i.keyReleased(e);
      } else if (e.type() == KeyEvent.TYPE_KEY_PRESSED) {
        i.keyPressed(e);
      }
    }
  }
}
