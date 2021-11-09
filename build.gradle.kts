// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.github.kezong:fat-aar:1.3.6")
    }
}

allprojects {
    repositories {
        google()
        maven {
            url = uri("https://linphone.org/maven_repository/")
        }
        mavenCentral()
        maven("https://jitpack.io")
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}
