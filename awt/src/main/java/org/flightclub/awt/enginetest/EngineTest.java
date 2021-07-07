/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt.enginetest;

import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.flightclub.awt.JavaxAudioPlayer;
import org.flightclub.awt.ModelCanvas;
import org.flightclub.engine.GameEnvironment;
import org.flightclub.engine.GameMode;
import org.flightclub.engine.GameModelHolder;
import org.flightclub.engine.IntPair;
import org.flightclub.engine.XcGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.awt.AwtKeyEventMapper.toEngineKeyEvent;

public class EngineTest extends Frame {
  private static final Logger LOG = LoggerFactory.getLogger(EngineTest.class);

  public EngineTest(
      final String title,
      final IntPair windowSize
  ) {
    super(title);

    final XcGame app = new XcGame(
        new GameModelHolder(GameMode.DEMO),
        new GameEnvironment(windowSize, new JavaxAudioPlayer())
    );

//    final ModelCanvas modelCanvas = new ModelCanvas(app);
//    add(modelCanvas, "Center");
    setSize(windowSize.x(), windowSize.y());
    setVisible(true);
//    modelCanvas.init();
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
        app.eventManager.addEvent(toEngineKeyEvent(e));
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        app.eventManager.addEvent(toEngineKeyEvent(e));
      }
    });
  }



  public static void main(final String ...args) {
    LOG.info("Flight Club Engine Test");
    final IntPair windowSize =  new IntPair(1000, 600);
    new EngineTest("Flight Club Engine Test", windowSize);
  }
}
