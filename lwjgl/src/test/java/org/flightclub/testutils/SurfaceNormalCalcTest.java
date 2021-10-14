package org.flightclub.testutils;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SurfaceNormalCalcTest {
  @Test
  void spike() {
    final float chordY = 0.2f;
    final float noseZ = chordY * 0.3f;
    final float anhedral = 0.15f;
    final float sweep = 0.4f;

    final float[] mesh = new float[]{
        0, chordY, noseZ,
        1, chordY - sweep, noseZ + anhedral,
        1, -sweep, anhedral,
        0, 0, 0,
        -1, -sweep, anhedral,
        -1, chordY - sweep, noseZ + anhedral
    };

    final int[] indices = new int[]{
        0, 1, 2,
        0, 2, 3,
        3, 4, 5,
        3, 5, 0
    };

    final Vector3f t1p1 = vectorAtIndex(mesh, indices[0]);
    final Vector3f t1p2 = vectorAtIndex(mesh, indices[1]);
    final Vector3f t1p3 = vectorAtIndex(mesh, indices[2]);

    final Vector3f t2p1 = vectorAtIndex(mesh, indices[6]);
    final Vector3f t2p2 = vectorAtIndex(mesh, indices[7]);
    final Vector3f t2p3 = vectorAtIndex(mesh, indices[8]);


    final Vector3f u = new Vector3f();
    final Vector3f v = new Vector3f();
    final Vector3f normal = new Vector3f();

    t1p2.sub(t1p1, u);
    t1p3.sub(t1p1, v);

    u.cross(v, normal).normalize();

    assertVectorEquality(normal, new Vector3f(0.25037593f, 0.27819547f, -0.92731827f));

    t2p2.sub(t2p1, u);
    t2p3.sub(t2p1, v);

    u.cross(v, normal).normalize();

    assertVectorEquality(normal, new Vector3f(-0.25037593f, 0.27819547f, -0.92731822f));
  }

  private Vector3f vectorAtIndex(final float[] mesh, final int index) {
    return new Vector3f(
        mesh[index * 3],
        mesh[index * 3 + 1],
        mesh[index * 3 + 2]);
  }

  private void assertVectorEquality(Vector3f a, Vector3f b) {
    final Vector3f diff = new Vector3f();
    a.sub(b, diff);

    // System.out.printf("%.8f %.8f %.8f\n", a.x, a.y, a.z);
    // System.out.printf("%.8f %.8f %.8f\n", diff.x, diff.y, diff.z);
    assertThat(Math.abs(diff.x)).isLessThan(0.000001f);
    assertThat(Math.abs(diff.y)).isLessThan(0.000001f);
    assertThat(Math.abs(diff.z)).isLessThan(0.000001f);
  }
}
