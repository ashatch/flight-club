/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.core.geometry.Object3dWithShadow;
import org.flightclub.engine.math.Vector3d;

public class FlyingBody extends FlyingDot {
  private Object3dWithShadow body0;
  private Object3dWithShadow body1;
  protected float bodyHeight;

  public FlyingBody(
      final XcGame theApp,
      final Sky sky,
      final float speed,
      final float inTurnRadius,
      final boolean isUser
  ) {
    super(theApp, sky, speed, inTurnRadius, isUser);
  }

  public void init(
      final Object3dWithShadow inBody,
      final Vector3d inP
  ) {
    body0 = inBody;    //the base object should not be registered
    body1 = new Object3dWithShadow();
    app.renderManager.add(body1);
    Object3dWithShadow.clone(body0, body1);

    super.init(inP);
    rotateBody();
    translateBody();
  }

  @Override
  public void update(final UpdateContext context) {
    super.update(context);
    rotateBody();
    translateBody();
    body1.updateShadow(app.landscape);
  }

  public void rotateBody() {
    for (int i = 0; i < body0.points.size(); i++) {
      Vector3d p1 = body1.points.elementAt(i);
      Vector3d p0 = body0.points.elementAt(i);

      Vector3d xp1 = new Vector3d(axisX).scaleBy(p0.posX);
      Vector3d yp1 = new Vector3d(axisY).scaleBy(p0.posY);
      Vector3d zp1 = new Vector3d(axisZ).scaleBy(p0.posZ);

      p1.posX = xp1.posX + yp1.posX + zp1.posX;
      p1.posY = xp1.posY + yp1.posY + zp1.posY;
      p1.posZ = xp1.posZ + yp1.posZ + zp1.posZ;
    }
  }

  public void translateBody() {
    for (Vector3d point : body1.points) {
      point.add(vectorP);
    }
  }

  float getBodyHeight() {
    return bodyHeight;
  }
}
