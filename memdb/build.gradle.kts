plugins {
    kotlin("jvm")
}

group= "io.github.pushpagarwal"
version= "1.0-SNAPSHOT"


dependencies {
    api(project(":coredb"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
}