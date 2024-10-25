plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.voipgrid.vialer"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        addDefaultAuthValues(this)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    splits {
        abi {
            isEnable = true
            isUniversalApk = false
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.navigation:navigation-fragment:2.3.2")
    implementation("androidx.navigation:navigation-ui:2.3.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.2")
    implementation(project(":pil"))
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
}

fun addDefaultAuthValues(defaultConfig: com.android.build.api.dsl.DefaultConfig) {
    // If you wish to pre-populate the example app with authentication information to
    // make testing quicker, just add these properties (e.g. apl.default.username) to
    // your ~/.gradle/gradle.properties file.
    try {
        defaultConfig.resValue("string", "default_sip_user", project.property("pil.default.username") as String)
        defaultConfig.resValue("string", "default_sip_password", project.property("pil.default.password") as String)
        defaultConfig.resValue("string", "default_sip_domain", project.property("pil.default.domain") as String)
        defaultConfig.resValue("string", "default_sip_port", project.property("pil.default.port") as String)
        defaultConfig.resValue("string", "default_voipgrid_username", project.property("pil.default.voipgrid.username") as String)
        defaultConfig.resValue("string", "default_voipgrid_password", project.property("pil.default.voipgrid.password") as String)
    } catch (e: groovy.lang.MissingPropertyException) {
        defaultConfig.resValue("string", "default_sip_user", "")
        defaultConfig.resValue("string", "default_sip_password", "")
        defaultConfig.resValue("string", "default_sip_domain", "")
        defaultConfig.resValue("string", "default_sip_port", "")
        defaultConfig.resValue("string", "default_voipgrid_username", "")
        defaultConfig.resValue("string", "default_voipgrid_password", "")
    }
}