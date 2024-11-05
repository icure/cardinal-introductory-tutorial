plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.cardinal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://maven.taktik.be/content/groups/public") }
}

dependencies {
    implementation(group = "com.icure", name = "cardinal-sdk", version="1.0.0-RC.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}