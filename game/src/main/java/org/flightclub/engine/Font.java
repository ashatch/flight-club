package org.flightclub.engine;

public record Font(String name, int style, int size) {
  /**
   * The plain style constant.
   */
  public static final int PLAIN = 0;

  /**
   * The bold style constant.  This can be combined with the other style
   * constants (except PLAIN) for mixed styles.
   */
  public static final int BOLD = 1;

  /**
   * The italicized style constant.  This can be combined with the other
   * style constants (except PLAIN) for mixed styles.
   */
  public static final int ITALIC = 2;
}
