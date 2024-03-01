import java.net.URI

plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
    id("com.jfrog.bintray")
    id("com.palantir.git-version") version "0.12.3"
    id("com.kezong.fat-aar")
    id("kotlin-android")
}

version = "0.1.128"

android {
    compileSdkVersion(31)
    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(31)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        createVersionInformation(this)
    }
}

dependencies {
    api("com.google.firebase:firebase-messaging-ktx:21.0.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.media:media:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("io.insert-koin:koin-android:2.2.2")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("androidx.lifecycle:lifecycle-process:2.5.0")

    testImplementation("junit:junit:4.+")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    androidTestImplementation("org.mockito:mockito-android:+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    embed("org.linphone.minimal:linphone-sdk-android:5.2.110")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/open-voip-alliance/Android-Phone-Integration-Lib")
            group = "org.openvoipalliance"
            credentials {
                username = System.getenv("ACTOR_GITHUB")
                password = System.getenv("TOKEN_GITHUB")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            from(components.findByName("release"))
        }
    }
}

task("generateDummyGoogleServicesJsonFile") {
        val file = File("${project.projectDir}/../app/google-services.json")

        if (file.exists()) return@task

        val exampleFile = File("${project.projectDir}/../app/google-services.example")

        if (!exampleFile.exists()) {
            print("Example file does not exist, unable to make google-services.json file. Please add a ${exampleFile.path}.")
            return@task
        }

        val newFile = exampleFile.copyTo(file, overwrite = true)

        if (newFile.exists()) {
            print("Created dummy google-services.json file.")
        }
}


fun createVersionInformation(defaultConfig: com.android.build.api.dsl.DefaultConfig) {
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    version = versionDetails().version
    val lastTag = versionDetails().lastTag
    val gitHash = versionDetails().gitHash

    defaultConfig.resValue("string", "pil_build_info_version", version.toString())
    defaultConfig.resValue("string", "pil_build_info_tag", lastTag)
    defaultConfig.resValue("string", "pil_build_info_hash", gitHash)
}