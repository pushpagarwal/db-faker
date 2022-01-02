plugins {
    kotlin("jvm")
    //id("com.strumenta.antlr-kotlin") version "0.0.7"
}

val antlrKotlinVersion = "0fad6c228a"

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }
    val antlrKotlinVersion = "0fad6c228a"
    dependencies {
        // add the plugin to the classpath
        classpath("com.strumenta.antlr-kotlin:antlr-kotlin-gradle-plugin:$antlrKotlinVersion")
    }
}

tasks.register<com.strumenta.antlrkotlin.gradleplugin.AntlrKotlinTask>("generateKotlinGrammarSource") {
    // the classpath used to run antlr code generation
    antlrClasspath = configurations.detachedConfiguration(
        // antlr itself
        // antlr is transitive added by antlr-kotlin-target,
        // add another dependency if you want to choose another antlr4 version (not recommended)
        // project.dependencies.create("org.antlr:antlr4:$antlrVersion"),

        // antlr target, required to create kotlin code
        project.dependencies.create("com.strumenta.antlr-kotlin:antlr-kotlin-target:$antlrKotlinVersion")
    )
    maxHeapSize = "64m"
    packageName = "dbfaker.parser.antlr"
    arguments = listOf("-visitor", "-no-listener")
    source = project.objects
        .sourceDirectorySet("antlr", "antlr")
        .srcDir("src/main/antlr").apply {
            include("*.g4")
        }
    // outputDirectory is required, put it into the build directory
    // if you do not want to add the generated sources to version control
    outputDirectory = File("build/generated-src/antlr/main")
    // use this settings if you want to add the generated sources to version control
    //outputDirectory = File("src/main/kotlin-antlr")
}

// run generate task before build
// not required if you add the generated sources to version control
// you can call the task manually in this case to update the generated sources
tasks.getByName("compileKotlin").dependsOn("generateKotlinGrammarSource")

// you have to add the generated sources to kotlin compiler source directory list
configure<SourceSetContainer> {
    named("main") {
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            kotlin.srcDir("build/generated-src/antlr/main")
            //kotlin.srcDir("src/main/kotlin-antlr")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.strumenta.antlr-kotlin:antlr-kotlin-runtime-jvm:$antlrKotlinVersion")
    testImplementation("junit:junit:4.13.2")
}