/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub;

import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class XcGameFrame extends Frame {
  final XcGame app = new XcGame();

  public XcGameFrame(String title, int w, int h) {
    super(title);

    ModelCanvas panel = new ModelCanvas(app);
    add(panel, "Center");
    setSize(w, h);
    setVisible(true);

    panel.init();
    app.init(new FrameInterface(this, panel));
    app.start();

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        app.eventManager.addEvent(e);
      }

      @Override
      public void keyReleased(KeyEvent e) {
        app.eventManager.addEvent(e);
      }
    });
  }

  public static void main(String s[]) {
    new XcGameFrame("Flight Club", 1000, 600);
  }
}
