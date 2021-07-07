/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.math;

public final class Vector3d {
  public float posX;
  public float posY;
  public float posZ;

  public Vector3d() {
    set(0, 0, 0);
  }

  public Vector3d(float posX, float posY, float posZ) {
    set(posX, posY, posZ);
  }

  public Vector3d(double posX, double posY, double posZ) {
    set((float) posX, (float) posY, (float) posZ);
  }

  public Vector3d(Vector3d other) {
    set(other);
  }

  public float length() {
    return (float) Math.hypot(Math.hypot(posX, posY), posZ);
  }

  public Vector3d set(float x, float y, float z) {
    this.posX = x;
    this.posY = y;
    this.posZ = z;
    return this;
  }

  public Vector3d set(Vector3d other) {
    set(other.posX, other.posY, other.posZ);
    return this;
  }

  public Vector3d add(Vector3d other) {
    posX += other.posX;
    posY += other.posY;
    posZ += other.posZ;
    return this;
  }

  /** Returns a copy of this instance plus by the other instance. */
  public Vector3d plus(Vector3d other) {
    return new Vector3d(this).add(other);
  }

  public Vector3d subtract(Vector3d other) {
    posX -= other.posX;
    posY -= other.posY;
    posZ -= other.posZ;
    return this;
  }

  /** Returns a copy of this instance subtracted by the other instance. */
  public Vector3d minus(Vector3d other) {
    return new Vector3d(this).subtract(other);
  }

  public Vector3d scaleBy(float factor) {
    posX *= factor;
    posY *= factor;
    posZ *= factor;
    return this;
  }

  public Vector3d scaleToLength(float length) {
    scaleBy(length / this.length());
    return this;
  }

  public Vector3d makeUnit() {
    scaleToLength(1);
    return this;
  }

  public float dot(Vector3d other) {
    return posX * other.posX + posY * other.posY + posZ * other.posZ;
  }

  public Vector3d cross(Vector3d other) {
    set(posY * other.posZ - posZ * other.posY,
        -posX * other.posZ + posZ * other.posX,
        posX * other.posY - posY * other.posX);
    return this;
  }

  /** Returns a copy of this instance cross multiplied with the other instance. */
  public Vector3d crossed(Vector3d other) {
    return new Vector3d(this).cross(other);
  }
}
