package org.flightclub.engine;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class ResourceLoader {
  public static String loadResource(final String path) {
    return new Scanner(
        Objects.requireNonNull(ResourceLoader.class.getResourceAsStream(path)),
        StandardCharsets.UTF_8
    )
        .useDelimiter("\\A")
        .next();
  }
}
