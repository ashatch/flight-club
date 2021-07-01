package org.flightclub.compat;

public class AwtGraphics implements Graphics {

  private final java.awt.Graphics gfx;

  public AwtGraphics(java.awt.Graphics gfx) {
    this.gfx = gfx;
  }

  @Override
  public void setColor(Color color) {
    gfx.setColor(color.getColor());
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2) {
    gfx.drawLine(x1, y1, x2, y2);
  }

  @Override
  public void setFont(Font font) {
    gfx.setFont(font.getFont());
  }

  @Override
  public void drawString(String str, int x, int y) {
    gfx.drawString(str, x, y);
  }

  @Override
  public void fillPolygon(int[] pointsX, int[] pointsY, int pointsN) {
    gfx.fillPolygon(pointsX, pointsY, pointsN);
  }

  @Override
  public void fillCircle(int x, int y, int diameter) {
    gfx.fillOval(x, y, diameter, diameter);
  }
}
