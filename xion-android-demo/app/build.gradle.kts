plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("kapt")
}

android {
    namespace = "com.burnt.xiondemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.burnt.xiondemo"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField("String", "XION_RPC_URL", "\"https://rpc.xion-testnet-2.burnt.com:443\"")
        buildConfigField("String", "XION_REST_URL", "\"https://api.xion-testnet-2.burnt.com/\"")
        buildConfigField("String", "XION_CHAIN_ID", "\"xion-testnet-2\"")
        buildConfigField("String", "XION_TREASURY_ADDRESS", "\"xion1sm3qp7kdqkkqgq5sdze6fjvk02a9psqqht2s575kdw06y4prlqcqhqa0mj\"")
        buildConfigField("String", "XION_OAUTH_CLIENT_ID", "\"\"")
        buildConfigField("String", "XION_OAUTH_AUTHORIZATION_ENDPOINT", "\"https://auth.testnet.burnt.com/\"")
    }

    buildTypes {
        debug {
            // No mock toggle — always uses real mob
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity & Lifecycle
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Retrofit + Serialization
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JNA (AAR for Android)
    implementation("net.java.dev.jna:jna:5.14.0@aar")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Browser (Chrome Custom Tabs)
    implementation("androidx.browser:browser:1.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
}

kapt {
    correctErrorTypes = true
}
