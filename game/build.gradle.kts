repositories {
    mavenCentral()
}

plugins {
    checkstyle
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.joml:joml:1.10.0")
}
