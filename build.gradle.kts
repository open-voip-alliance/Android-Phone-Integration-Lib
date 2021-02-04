// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    project.extra.set("kotlinVersion",  "1.4.10")

    repositories {
        jcenter()
        google()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("com.google.gms:google-services:4.3.4")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}