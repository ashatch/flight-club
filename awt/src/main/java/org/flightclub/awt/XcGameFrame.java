/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt;

import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.flightclub.engine.GameEnvironment;
import org.flightclub.engine.GameMode;
import org.flightclub.engine.GameModelHolder;
import org.flightclub.engine.IntPair;
import org.flightclub.engine.XcGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.awt.AwtKeyEventMapper.toEngineKeyEvent;

public class XcGameFrame extends Frame {
  private static final Logger LOG = LoggerFactory.getLogger(XcGameFrame.class);

  public XcGameFrame(
      final XcGame game,
      final String title,
      final IntPair windowSize
  ) {
    super(title);

    final ModelCanvas modelCanvas = new ModelCanvas(game);
    add(modelCanvas, "Center");
    setSize(windowSize.x(), windowSize.y());
    setVisible(true);
    modelCanvas.init();

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });

    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        game.eventManager.addEvent(toEngineKeyEvent(e));
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        game.eventManager.addEvent(toEngineKeyEvent(e));
      }
    });
  }

  public static void main(final String ...args) {
    LOG.info("Flight Club");

    final IntPair windowSize = new IntPair(1000, 600);
    final XcGame game = new XcGame(
        new GameModelHolder(GameMode.DEMO),
        new GameEnvironment(windowSize, new JavaxAudioPlayer())
    );

    new XcGameFrame(game, "Flight Club", windowSize);

    game.start();
  }
}
