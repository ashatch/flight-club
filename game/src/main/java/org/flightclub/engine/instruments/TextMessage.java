package org.flightclub.engine.instruments;

public class TextMessage {
  private String message;

  public TextMessage(String initialMessage) {
    setMessage(initialMessage);
  }

  public String getMessage(boolean isPaused) {
    return isPaused ? message + " [ paused ]" : message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
