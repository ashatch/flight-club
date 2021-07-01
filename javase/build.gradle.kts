repositories {
    mavenCentral()
}

plugins {
    application
    java
    checkstyle
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

application {
    mainClass.set("org.flightclub.XcGameFrame")
}

dependencies {
    implementation(project(":core"))
}
