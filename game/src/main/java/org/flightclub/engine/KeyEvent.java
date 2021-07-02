package org.flightclub.engine;

public record KeyEvent(int type, int code) {
  public static final int TYPE_KEY_RELEASED = 100;
  public static final int TYPE_KEY_PRESSED = 101;

  public static final int VK_A = 0x41;
  public static final int VK_UP = 0x26;
  public static final int VK_DOWN = 0x28;
  public static final int VK_LEFT = 0x25;
  public static final int VK_RIGHT = 0x27;
  public static final int VK_SPACE = 0x20;
  public static final int VK_D = 0x44;
  public static final int VK_G = 0x47;
  public static final int VK_H = 0x48;
  public static final int VK_K = 0x4B;
  public static final int VK_L = 0x4C;
  public static final int VK_M = 0x4D;
  public static final int VK_N = 0x4E;
  public static final int VK_P = 0x50;
  public static final int VK_Q = 0x51;
  public static final int VK_S = 0x53;
  public static final int VK_W = 0x57;
  public static final int VK_Y = 0x59;
  public static final int VK_1 = 0x31;
  public static final int VK_2 = 0x32;
  public static final int VK_3 = 0x33;
  public static final int VK_4 = 0x34;
}
