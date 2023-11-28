plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.0" // Check for the latest version
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.myapp.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

    // Specify the namespace here
    namespace = "com.myapp.myapplication"
}

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
        credentials {
            username = findProperty("authToken") as String? ?: ""
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")
    implementation("com.opencsv:opencsv:5.9")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.google.android.mms:pdu_alt:1.0")

    // Use the latest version available on Maven Central
    implementation("com.github.mikaelhg:nondroid-mms:0ac06a5")

    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
