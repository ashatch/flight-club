package org.flightclub.shaders;

import org.flightclub.engine.ShaderProgram;

import static org.flightclub.engine.ResourceLoader.loadResource;

public class StandardShader extends ShaderProgram {
  public StandardShader() throws Exception {
    super();

    createVertexShader(loadResource("/vertex.vs"));
    createFragmentShader(loadResource("/fragment.fs"));
    link();

    createUniform("projectionMatrix");
    createUniform("worldMatrix");
  }
}
