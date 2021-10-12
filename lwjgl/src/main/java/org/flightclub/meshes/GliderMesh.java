package org.flightclub.meshes;

import org.flightclub.engine.Mesh;

/**
  Derived from GliderShape
 */
public class GliderMesh extends Mesh {
  private static final float chordY = 0.2f;
  private static final float noseZ = chordY * 0.3f;
  private static final float anhedral = 0.15f;
  private static final float sweep = 0.4f;

  public GliderMesh() {
    super(
        new float[]{
            0, chordY, noseZ,
            1, chordY - sweep, noseZ + anhedral,
            1, -sweep, anhedral,
            0, 0, 0,
            -1, -sweep, anhedral,
            -1, chordY - sweep, noseZ + anhedral
        }, new float[]{
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
        },
        new int[]{
            0, 1, 2,
            0, 2, 3,
            3, 4, 5,
            3, 5, 0
        }
    );
  }
}
