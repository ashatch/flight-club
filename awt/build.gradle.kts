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
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation(project(":game"))
}
