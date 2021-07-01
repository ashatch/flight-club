/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.game;

import org.flightclub.engine.ModelCanvas;
import org.flightclub.engine.XcGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class XcGameFrame extends Frame {
  private static final Logger LOG = LoggerFactory.getLogger(XcGameFrame.class);

  public XcGameFrame(
      final String title,
      final Dimension windowSize
  ) {
    super(title);

    final XcGame app = new XcGame(
        new DesktopEnvironmentImpl(windowSize, new AudioPlayerImpl())
    );

    final ModelCanvas modelCanvas = new ModelCanvas(app);
    add(modelCanvas, "Center");
    setSize(windowSize);
    setVisible(true);
    modelCanvas.init();


    app.start();

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });

    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        app.eventManager.addEvent(e);
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        app.eventManager.addEvent(e);
      }
    });
  }

  public static void main(final String ...args) {
    LOG.info("Flight Club");
    final Dimension windowSize =  new Dimension(1000, 600);
    new XcGameFrame("Flight Club", windowSize);
  }
}
