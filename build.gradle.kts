import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}
group = "gmail.sung2vision"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
dependencies {
    implementation("commons-cli:commons-cli:1.4")
    implementation("commons-io:commons-io:2.6")
    implementation("org.ini4j:ini4j:0.5.4")
    implementation("org.json:json:20200518")
    implementation("org.apache.httpcomponents:httpclient:4.5.12")
    testImplementation(kotlin("test-junit"))
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
application {
    mainClassName = "MainKt"
}
