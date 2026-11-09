plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.legacyKapt)
}

android {
    namespace = "com.fixly.tp9comp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.fixly.tp9comp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activityKtx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.coreKtx)
    implementation(libs.material)

    // Room
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    kapt(libs.roomCompiler)

    // WorkManager
    implementation(libs.workRuntimeKtx)

    // Play Services Location
    implementation(libs.playServicesLocation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.androidxJunit)
}
