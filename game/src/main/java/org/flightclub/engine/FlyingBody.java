/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.core.geometry.Object3dWithShadow;
import org.joml.Vector3f;

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
      final Vector3f inP
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
      Vector3f p1 = body1.points.elementAt(i);
      Vector3f p0 = body0.points.elementAt(i);

      Vector3f xp1 = new Vector3f(axisX).mul(p0.x);
      Vector3f yp1 = new Vector3f(axisY).mul(p0.y);
      Vector3f zp1 = new Vector3f(axisZ).mul(p0.z);

      p1.x = xp1.x + yp1.x + zp1.x;
      p1.y = xp1.y + yp1.y + zp1.y;
      p1.z = xp1.z + yp1.z + zp1.z;
    }
  }

  public void translateBody() {
    for (Vector3f point : body1.points) {
      point.add(vectorP);
    }
  }

  float getBodyHeight() {
    return bodyHeight;
  }
}
