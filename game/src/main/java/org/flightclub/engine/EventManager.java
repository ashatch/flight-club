/*
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
  public void subscribe(final KeyEventHandler handler) {
    subscribers.add(handler);
  }

  /*
   * add event to queue
   */
  public boolean addEvent(final KeyEvent e) {
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
    final KeyEvent keyEvent = events.poll();

    if (keyEvent == null) {
      return;
    }

    subscribers.forEach(subscriber -> {
      if (keyEvent.type() == KeyEvent.TYPE_KEY_RELEASED) {
        subscriber.keyReleased(keyEvent);
      } else if (keyEvent.type() == KeyEvent.TYPE_KEY_PRESSED) {
        subscriber.keyPressed(keyEvent);
      }
    });
  }
}
