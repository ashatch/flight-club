package org.flightclub.engine;

public record Color(int r, int g, int b) {
  /**
   * The color white.  In the default sRGB space.
   */
  public static final Color WHITE = new Color(255, 255, 255);

  /**
   * The color light gray.  In the default sRGB space.
   */
  public static final Color LIGHT_GRAY = new Color(192, 192, 192);

  /**
   * The color gray.  In the default sRGB space.
   */
  public static final Color GRAY = new Color(128, 128, 128);

  /**
   * The color dark gray.  In the default sRGB space.
   */
  public static final Color DARK_GRAY = new Color(64, 64, 64);

  /**
   * The color black.  In the default sRGB space.
   */
  public static final Color BLACK = new Color(0, 0, 0);

  /**
   * The color red.  In the default sRGB space.
   */
  public static final Color RED = new Color(255, 0, 0);

  /**
   * The color pink.  In the default sRGB space.
   */
  public static final Color PINK = new Color(255, 175, 175);

  /**
   * The color orange.  In the default sRGB space.
   */
  public static final Color ORANGE = new Color(255, 200, 0);

  /**
   * The color yellow.  In the default sRGB space.
   */
  public static final Color YELLOW = new Color(255, 255, 0);

  /**
   * The color green.  In the default sRGB space.
   */
  public static final Color GREEN = new Color(0, 255, 0);

  /**
   * The color magenta.  In the default sRGB space.
   */
  public static final Color MAGENTA = new Color(255, 0, 255);

  /**
   * The color cyan.  In the default sRGB space.
   */
  public static final Color CYAN = new Color(0, 255, 255);

  /**
   * The color blue.  In the default sRGB space.
   */
  public static final Color BLUE = new Color(0, 0, 255);

  public Color mul(final float scale) {
    return new Color((int)(r * scale), (int)(g * scale), (int)(b * scale));
  }
}
