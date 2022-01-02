plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

allprojects {
    group = "io.github.pushpagarwal"
    version = "1.0-SNAPSHOT"

    repositories {

        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")

    }

}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
tasks.register<Copy>("copyTestResources") {
    from("${projectDir}/src/test/resources")
    into("${buildDir}/classes/test")
}

tasks.withType<ProcessResources>(){
    dependsOn("copyTestResources")
}

