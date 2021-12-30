plugins {
    kotlin("jvm")
    idea
}

idea {
    module{
        inheritOutputDirs = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":coredb"))
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.3")
    testImplementation("junit:junit:4.13.2")
}

tasks.register<Copy>("copyTestResources") {
    from("${projectDir}/src/test/resources")
    into("${buildDir}/classes/kotlin/test")
}

tasks.withType<ProcessResources>(){
    dependsOn("copyTestResources")
}