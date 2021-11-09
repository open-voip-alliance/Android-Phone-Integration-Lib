plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("com.kezong.fat-aar")
    id("com.palantir.git-version") version "0.12.2"
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 26
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        createVersionInformation(this)
    }
}

dependencies {
    api("com.google.firebase:firebase-messaging-ktx:23.0.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.github.blainepwnz:AndroidContacts:1.14.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.media:media:1.4.3")
    implementation("io.insert-koin:koin-android:3.1.3")
    embed("org.linphone.minimal:linphone-sdk-android:5.0.49")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    androidTestImplementation("org.mockito:mockito-android:4.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
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
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    version = versionDetails().version
    val lastTag = versionDetails().lastTag
    val gitHash = versionDetails().gitHash

    defaultConfig.resValue("string", "pil_build_info_version", version.toString())
    defaultConfig.resValue("string", "pil_build_info_tag", lastTag)
    defaultConfig.resValue("string", "pil_build_info_hash", gitHash)
}