plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "2.5.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.5.10"
}

group = "io.github.pushpagarwal"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":db-interface"))
    api(project(":memdb"))
    api(project(":sql-parser"))
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.3")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("junit:junit:4.13.2")

}

tasks.register<Copy>("copyTestResources") {
    from("${projectDir}/src/test/resources")
    into("${buildDir}/classes/kotlin/test")
}

tasks.withType<ProcessResources>(){
    dependsOn("copyTestResources")
}
