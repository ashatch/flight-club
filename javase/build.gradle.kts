plugins {
    id("application")
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

application {
    mainClass.set("org.flightclub.XCGameFrame")
}

dependencies {
    implementation(project(":core"))
}
