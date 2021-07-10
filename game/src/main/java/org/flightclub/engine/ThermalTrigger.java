/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;

public class ThermalTrigger implements UpdatableGameObject {
  final XcGame app;
  final int positionX;
  final int positionY;
  private final Sky sky;
  float time;

  /* 0, 1 or 2 - stop clouds overlapping */
  int nextCloud;

  final int cycleLength;
  final int cloudDuration;
  final int cloudStrength;
  int dummyClick = 0;
  final Vector<Cloud> clouds;

  /* how many thermals per cycle (1 or 2) */
  final int bubbles;

  static final float SPREAD = (float) 1.2; // 0.5
  static final int CYCLE_LENGTH = 20; // 30

  /* life span of cloud in seconds */
  static final int CLOUD_DURATION = 10;
  static final int MAX_WAIT = 7;

  public ThermalTrigger(
      XcGame theApp,
      Sky sky,
      int inX,
      int inY,
      int inCloudStrenth,
      float inCycleLength,
      float inCloudDuration
  ) {
    app = theApp;
    this.sky = sky;
    theApp.addGameObject(this);
    positionX = inX;
    positionY = inY;

    cloudStrength = inCloudStrenth;
    cycleLength = (int) (inCycleLength * CYCLE_LENGTH);
    cloudDuration = (int) (inCloudDuration * CLOUD_DURATION);

    time = (int) Tools3d.rnd(0, cycleLength - 1);
    nextCloud = (int) Tools3d.rnd(0, 2);
    clouds = new Vector<>();

    //more cloud on center tiles
    if (positionX < Landscape.TILE_WIDTH / 2
        && positionX > -Landscape.TILE_WIDTH / 2) {
      bubbles = 1; //was 2, trying bruce's hexagon
    } else {
      bubbles = 1;
    }

    if (time < cycleLength - MAX_WAIT) {
      makeCloud();
    }

    Landscape.crossHair(positionX, positionY);
  }

  @Override
  public void update(final UpdateContext context) {

    if (time == 0) {
      makeCloud();
    }
    //if (bubbles > 1) if (t == 7) makeCloud();
    //if (bubbles > 2) if (t == 14) makeCloud();

    time += context.deltaTime() * context.timeMultiplier() / 2.0f;
    if (time > cycleLength) {
      time = 0;
    }

    //System.out.println("avg secs: " + c.getAvgSleep());
  }

  void makeCloud() {
    float dy;

    switch (nextCloud) {
      case 0:
        dy = SPREAD;
        break;
      case 1:
        dy = 0;
        break;
      case 2:
        dy = -SPREAD;
        break;
      default:
        dy = 0;
    }

    nextCloud++;
    if (nextCloud == 3) {
      nextCloud = 0;
    }

    float dx = (float) Tools3d.rnd(-SPREAD, SPREAD);
    Cloud cloud = new Cloud(app, this.sky,positionX + dx, positionY + dy, cloudDuration, cloudStrength);
    clouds.addElement(cloud);
    cloud.trigger = this;
  }

  void destroyMe() {
    app.removeGameObject(this);

    // hurry up clouds
    for (int i = 0; i < clouds.size(); i++) {
      Cloud cloud = clouds.elementAt(i);
      if (cloud.age < cloud.nose + cloud.mature) {
        if (cloud.age > cloud.nose) {
          cloud.mature = (int) cloud.age - cloud.nose;
        } else {
          cloud.mature = 0;
        }
      }
    }

  }

  void destroyMe(boolean really) {
    destroyMe();
    if (!really) {
      return;
    }

    //24/10 - kill those clouds, but beware that bug (see comment below)
    //hence kill them gradually
    //nb this is wanted for thermalling across tile boundary anyway
    for (int i = 0; i < clouds.size(); i++) {
      Cloud cloud = clouds.elementAt(i);
      //cloud.destroyMe();
      //found bug - do not modify contents of a vector while looping thru it!
      cloud.age = cloud.nose + cloud.mature + cloud.tail;
    }
  }
}
