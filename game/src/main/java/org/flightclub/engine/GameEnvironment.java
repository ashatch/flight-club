/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.awt.Dimension;
import org.flightclub.awt.AudioPlayer;

public record GameEnvironment(
    Dimension windowSize,
    AudioPlayer audioPlayer
) {}
