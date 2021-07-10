package org.flightclub.engine.instruments;

import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.Font;
import org.flightclub.engine.core.Graphics;
import org.flightclub.engine.core.RenderContext;
import org.flightclub.engine.core.Renderable;

public class TextMessage implements Renderable {
  private final Font font;
  private String message;

  public TextMessage(
      final String initialMessage,
      final Font font
  ) {
    setMessage(initialMessage);
    this.font = font;
  }

  public String getMessage(final boolean isPaused) {
    return isPaused ? message + " [ paused ]" : message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public void render(RenderContext context) {
    final Graphics graphics = context.graphics();
    final int height = context.screenSize().y();

    graphics.setFont(font);
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.drawString(getMessage(context.isPaused()), 15, height - 35);
  }
}
