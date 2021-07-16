/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.camera;

import org.joml.Vector3f;

public interface CameraSubject {
  Vector3f getEye();

  Vector3f getFocus();
}
