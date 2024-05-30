// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    project.extra.set("kotlinVersion",  "1.6.20")

    repositories {
        google()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
        classpath("com.google.gms:google-services:4.4.1")
        classpath("com.github.kezong:fat-aar:1.3.6")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven {
            url = uri("https://linphone.org/maven_repository/")
        }
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}