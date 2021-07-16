package org.flightclub.engine.camera;

import org.flightclub.engine.Landscape;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.geometry.Tools3d;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class Camera {
  private static final float AMBIENT_LIGHT = (float) 0.3;

  public static final int BACKGROUND_R = 255;
  public static final int BACKGROUND_G = 255;
  public static final int BACKGROUND_B = 255;
  public static final Color BACKGROUND = new Color(BACKGROUND_R, BACKGROUND_G, BACKGROUND_B);
  public static final float DEPTH_OF_VISION = Landscape.TILE_WIDTH * (float) 2.5;

  private final int screenWidth;
  private final int screenHeight;
  private final float theScale;

  private float distance = 0;
  private float[][] matrix;

  private Vector3f lightRay;
  private Vector3f eye;
  private Vector3f focus;

  public Camera(
      final Vector2i windowSize
  ) {
    screenWidth = windowSize.x;
    screenHeight = windowSize.y;
    theScale = screenHeight * (float) 1.1; //defines lens angle - smaller num -> wider angle
    //starting position and light
    eye = new Vector3f(3, 0, 0);
    focus = new Vector3f(0, 0, 0);

    withLightRay(1, 1, -3);
  }

  public Camera withLightRay(float x, float y, float z) {
    lightRay = new Vector3f(x, y, z);
    lightRay.normalize();
    return this;
  }

  public Vector3f getEye() {
    return eye;
  }

  public void setEye(float x, float y, float z) {
    eye.set(x, y, z);
  }

  public Vector3f getFocus() {
    return focus;
  }

  public void setFocus(float x, float y, float z) {
    focus.set(x, y, z);
  }

  public float getDistance() {
    return distance;
  }

  public float[][] getMatrix() {
    return matrix;
  }

  /*
   * rotate eye about z axis by xy radians and up/down by z
   */
  public void rotateEyeAboutFocus(float dtheta) {
    Vector3f ray = new Vector3f(eye).sub(focus);

    //transform ray
    float[][] m = Tools3d.rotateX(new Vector3f(1, dtheta, 0));
    Tools3d.applyTo(m, ray, ray);

    //reposition eye
    eye.set(focus).add(ray);
  }

  public void translateZ(float dz, boolean limitZ) {
    Vector3f ray = new Vector3f(eye).sub(focus);

    ray.z += distance * dz;

    eye.set(focus).add(ray);

    if (limitZ && eye.z < 0) {
      eye.z = 0;
    }
  }

  /*
   * move focus, maintaining angle of view
   */
  void moveFocus(Vector3f f) {
    Vector3f ray = new Vector3f(eye).sub(focus);
    focus.set(f);
    eye.set(ray).add(focus);
  }

  /*
   * rotation such that eye is looking down +x axis at origin
   */
  public void setMatrix() {
    Vector3f ray = new Vector3f(eye).sub(focus);
    matrix = Tools3d.rotateX(ray);
    distance = ray.length();
  }

  /*
   * scale the y and z co-ords so a 1 by 1 square
   * fills the screen when viewed from a distance of ??
   *
   * origin appears center screen.
   * nb flip z as screen coords have origin at top left !
   *
   * 1/10 try double scale (ie. half camera angle)
   */
  public void scaleToScreen(Vector3f vec) {
    vec.y *= theScale;    //preserve aspect ratio ? screenWidth;
    vec.y += screenWidth / 2;

    vec.z *= -theScale;
    vec.z += screenHeight / 2;
  }

  public void move(float dx, float dy) {
    eye.x += dx;
    eye.y += dy;
    focus.x += dx;
    focus.y += dy;
  }

  public void setFocus(Vector3f focus) {
    this.focus = focus;
  }

  public void focusOffset(int x, int y) {
    this.focus.x += x;
    this.focus.y += y;
  }

  public void setEye(Vector3f eye) {
    this.eye = eye;
  }

  /*
   * how much light falls on a surface with this normal - take dot product
   */
  public float surfaceLight(Vector3f inNormal) {
    float dot = lightRay.dot(inNormal);
    dot = (-dot + 1) / 2;

    //fri 1 mar 2002 - some under lighting for clouds
    if (inNormal.z < -0.99) {
      dot += 0.3;
    }

    return dot * (1 - AMBIENT_LIGHT) + AMBIENT_LIGHT;
  }
}
