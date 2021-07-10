package org.flightclub.engine;

import java.util.Vector;

public class Tile {
  boolean loaded = false;
  Vector<Hill> hills = new Vector<>();
  Vector<ThermalTrigger> triggers = new Vector<>();
}
