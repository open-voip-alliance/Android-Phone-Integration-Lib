plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
    id("com.jfrog.bintray")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api("org.openvoipalliance:AndroidPhoneLib:0.6.25")
    api("com.google.firebase:firebase-messaging-ktx:21.0.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")
    implementation("com.takwolf.android:foreback:0.1.1")
    implementation("com.tomash:androidcontacts:1.14.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("org.koin:koin-android:2.2.0")

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}