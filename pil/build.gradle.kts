plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
    id("com.jfrog.bintray")
    id("com.palantir.git-version") version "0.12.3"
}

extra["voipLibVersion"] = "0.1.6"

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        createVersionInformation(this)
    }
}

dependencies {
    val voipLibVersion = project.extra["voipLibVersion"]

    api("com.github.open-voip-alliance:Android-VoIP-Lib:$voipLibVersion")
    api("com.google.firebase:firebase-messaging-ktx:21.0.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.32")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.takwolf.android:foreback:0.1.1")
    implementation("com.tomash:androidcontacts:1.14.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("org.koin:koin-android:2.2.0")

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.findByName("release"))

                groupId = "org.openvoipalliance"
                artifactId = "AndroidPlatformIntegration"

            }
        }
    }
}

task("generateDummyGoogleServicesJsonFile") {
        val file = File("app/google-services.json")

        if (file.exists()) return@task

        val exampleFile = File("app/google-services.example")

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
    val voipLibVersion = project.extra["voipLibVersion"] as String
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    version = versionDetails().version
    val lastTag = versionDetails().lastTag
    val gitHash = versionDetails().gitHash

    defaultConfig.resValue("string", "pil_build_info_version", version.toString())
    defaultConfig.resValue("string", "pil_build_info_tag", lastTag)
    defaultConfig.resValue("string", "pil_build_info_hash", gitHash)
    defaultConfig.resValue("string", "pil_build_info_voip_lib_version", voipLibVersion)
}