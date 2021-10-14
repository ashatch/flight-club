package org.flightclub.meshes;

import org.flightclub.engine.Mesh;

/**
 * Derived from GliderShape
 */
public class GliderMesh extends Mesh {
  private static final float chordY = 0.2f;
  private static final float noseZ = chordY * 0.3f;
  private static final float anhedral = 0.15f;
  private static final float sweep = 0.4f;
  private static final float wingLen = 1;

  public GliderMesh() {
    super(
        new float[]{
            // right wing, clockwise from nose
            0, chordY, noseZ,
            wingLen, chordY - sweep, noseZ + anhedral,
            wingLen, -sweep, anhedral,
            0, 0, 0,

            // left wing, clockwise from rear
            0, 0, 0,
            -wingLen, -sweep, anhedral,
            -wingLen, chordY - sweep, noseZ + anhedral,
            0, chordY, noseZ,
        }, new float[]{
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
            0.3f, 0.4f, 0.3f,
        },
        new float[]{
            0.25037593f, 0.27819547f, 0.92731827f,
            0.25037593f, 0.27819547f, 0.92731827f,
            0.25037593f, 0.27819547f, 0.92731827f,
            0.25037593f, 0.27819547f, 0.92731827f,

            -0.25037593f, 0.27819547f, 0.92731822f,
            -0.25037593f, 0.27819547f, 0.92731822f,
            -0.25037593f, 0.27819547f, 0.92731822f,
            -0.25037593f, 0.27819547f, 0.92731822f,
        },
        new int[]{
            0, 1, 2,
            0, 2, 3,
            7, 4, 5,
            7, 5, 6,
        }
    );
  }
}
