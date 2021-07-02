repositories {
    mavenCentral()
}

plugins {
    checkstyle
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

dependencies {
    implementation("io.github.spair:imgui-java-binding:1.80-1.5.0")
    implementation("io.github.spair:imgui-java-lwjgl3:1.80-1.5.0")
    implementation("io.github.spair:imgui-java-app:1.80-1.5.0")
    implementation("org.joml:joml:1.10.0")
    implementation("org.lwjgl:lwjgl:3.2.3")
    implementation("org.lwjgl:lwjgl-glfw:3.2.3")
    implementation("org.lwjgl:lwjgl-opengl:3.2.3")
    implementation("org.lwjgl:lwjgl-assimp:3.2.3")
    implementation("org.lwjgl:lwjgl-stb:3.2.3")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation(project(":game"))
}
