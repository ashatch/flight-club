/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;
import org.joml.Vector3f;

/*
 * Manages clouds and related met data
 * NB Thermal triggers create clouds
 */
public class Sky {
  private static final float BASE_HIGH = 3;
  private static final float BASE_LOW = 2;

  // clouds in order from south to north
  private final Vector<Cloud> clouds = new Vector<>();
  private float cloudBase = BASE_LOW;

  final float RANGE = 8;    //for next /prev - dist per unit height i.e. glide angle

  void addCloud(Cloud cloud) {
    // TODO keep sorted list of clouds
    clouds.addElement(cloud);
  }

  void removeCloud(final Cloud cloud) {
    clouds.removeElement(cloud);
  }

  public void setHigh() {
    cloudBase = BASE_HIGH;
  }

  public void setLow() {
    cloudBase = BASE_LOW;
  }

  /*
   * return first cloud downwind of p within glide
   */
  Cloud nextCloud(final Vector3f p) {
    int j = -1;
    float dyMin = RANGE * p.z;

    for (int i = 0; i < clouds.size(); i++) {
      Cloud cloud = clouds.elementAt(i);
      //if (cloud.getY(p.z) >= p.y && cloud.age < 10) {
      if (cloud.getY(p.z) >= p.y && !cloud.decaying) {
        float dy = cloud.getY(p.z) - p.y;
        if (dy < dyMin) {
          j = i;
          dyMin = dy;
        }
      }
    }
    if (j != -1) {
      return clouds.elementAt(j);
    }

    //System.out.println("Next cloud returning null !");
    return null;
  }

  /*
   * return first cloud upwind of p
   * useful when gaggle get ahead of user
   * and reach end of a tile
   */
  Cloud prevCloud(final Vector3f p) {
    int j = -1;
    float dyMin = RANGE * p.z;

    for (int i = clouds.size() - 1; i >= 0; i--) {
      Cloud cloud = clouds.elementAt(i);
      if (cloud.getY(p.z) <= p.y && cloud.age < 10) {
        float dy = p.y - cloud.getY(p.z);
        if (dy < dyMin) {
          j = i;
          dyMin = dy;
        }
      }
    }

    if (j != -1) {
      return clouds.elementAt(j);
    }

    //System.out.println("Prev cloud returning null");
    return null;
  }

  public Cloud getCloudAt(final Vector3f p) {
    for (Cloud cloud : clouds) {
      if (cloud.isUnder(p)) {
        return cloud;
      }
    }

    return null;
  }

  float getCloudBase() {
    return cloudBase;
  }

  float getWind() {
    // units of unit distance (km) per unit time (minute)
    return (float) 0.3;
  }
}
