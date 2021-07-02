package org.flightclub;

import imgui.ImGui;
import imgui.app.Configuration;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.Supplier;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwMaximizeWindow;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

public abstract class Window {
  protected final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
  protected final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
//  protected final FrameRate fps = new FrameRate();
//  protected MouseInput mouseInput;
//  protected KeyboardInput keyboardInput;
  protected final float[] colorBg = {.5f, .5f, .5f, 1};
  protected final Vector2i windowSize = new Vector2i();
  protected String glslVersion = null;
  protected double lastFrameStartTime;
  protected long handle;

  private boolean resized = false;

  protected final void init(final Configuration config) {
    windowSize.set(config.getWidth(), config.getHeight());
    initWindow(config);
//    mouseInput.init(this.handle);
    initImGui();
    imGuiGlfw.init(handle, true);
    imGuiGl3.init(glslVersion);
  }

  public void launch(final Supplier<Configuration> configurationSupplier) {
    init(configurationSupplier.get());
    setup();
    run();
    cleanup();
    dispose();
  }

  protected abstract void cleanup();

  protected abstract void setup();

  protected final void dispose() {
    imGuiGl3.dispose();
    imGuiGlfw.dispose();
    disposeImGui();
    disposeWindow();
  }

  protected void initWindow(final Configuration config) {
    GLFWErrorCallback.createPrint(System.err).set();

    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    decideGlGlslVersions();

    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    handle = glfwCreateWindow(config.getWidth(), config.getHeight(), config.getTitle(), MemoryUtil.NULL, MemoryUtil.NULL);

    if (handle == MemoryUtil.NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    glfwSetFramebufferSizeCallback(handle, this::windowResizedCallback);

//    this.keyboardInput = new KeyboardInput();
//    this.keyboardInput.init(this.handle);
//
//    this.mouseInput = new MouseInput();
//    this.mouseInput.init(this.handle);

    try (final MemoryStack stack = MemoryStack.stackPush()) {
      final IntBuffer pWidth = stack.mallocInt(1); // int*
      final IntBuffer pHeight = stack.mallocInt(1); // int*

      glfwGetWindowSize(handle, pWidth, pHeight);
      final GLFWVidMode vidmode = Objects.requireNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));
      glfwSetWindowPos(handle, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
    }

    glfwMakeContextCurrent(handle);
    glfwSwapInterval(GLFW_TRUE);
    glfwShowWindow(handle);

    if (config.isFullScreen()) {
      glfwMaximizeWindow(handle);
    }

    createCapabilities();
    glEnable(GL_DEPTH_TEST);
  }

  private void windowResizedCallback(
      final long window,
      final int width,
      final int height
  ) {
    this.windowSize.set(width, height);
    this.resized = true;
  }

  private void decideGlGlslVersions() {
    final boolean isMac = System.getProperty("os.name")
        .toLowerCase()
        .contains("mac");

    if (isMac) {
      glslVersion = "#version 150";
      glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
      glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
      glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);  // 3.2+ only
      glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);          // Required on Mac
    } else {
      glslVersion = "#version 130";
      glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
      glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
    }
  }

  protected void initImGui() {
    ImGui.createContext();
  }

  protected void updateState(float deltaTimeSeconds) {
  }

  protected final void run() {
    double frameStartTime = glfwGetTime();
    double deltaTime;

    while (!glfwWindowShouldClose(handle)) {
      startFrame();
      handleResizes();
      deltaTime = glfwGetTime() - frameStartTime;
      frameStartTime = glfwGetTime();
//      mouseInput.input();
      updateState((float) deltaTime);
      render();
      renderGUI();
      endFrame();
    }
  }

  private void handleResizes() {
    if (this.resized) {
      glViewport(0, 0, windowSize.x, windowSize.y);
      resized = false;
    }
  }

  public abstract void render();
  public abstract void renderGUI();

  protected void startFrame() {
    glClearColor(colorBg[0], colorBg[1], colorBg[2], colorBg[3]);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    imGuiGlfw.newFrame();
    ImGui.newFrame();
  }

  protected void endFrame() {
    ImGui.render();
    imGuiGl3.renderDrawData(ImGui.getDrawData());

    if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
      final long backupWindowPtr = glfwGetCurrentContext();
      ImGui.updatePlatformWindows();
      ImGui.renderPlatformWindowsDefault();
      glfwMakeContextCurrent(backupWindowPtr);
    }

    glfwSwapBuffers(handle);
    glfwPollEvents();

    updateFrameRateData();
  }

  private void updateFrameRateData() {
    final double elapsedTime = glfwGetTime() - lastFrameStartTime;
    if (elapsedTime > 0.1) {
//      fps.updateFrameRate(elapsedTime);
      lastFrameStartTime = glfwGetTime();
    }
//    fps.frameTick();
  }

  protected void disposeImGui() {
    ImGui.destroyContext();
  }

  protected void disposeWindow() {
    glfwFreeCallbacks(handle);
    glfwDestroyWindow(handle);
    glfwTerminate();
    Objects.requireNonNull(glfwSetErrorCallback(null)).free();
  }
}
