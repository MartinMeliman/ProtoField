plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP: genera el código de Room
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}
android {
    namespace = "cl.duoc.protofield"
    compileSdk = 36

    defaultConfig {
        applicationId = "cl.duoc.protofield"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //librerias nuevas
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    // Material icons (íconos de cámara, GPS, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    // Room (SQLite) - runtime y extensiones KTX
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    // Compilador de Room vía KSP
    ksp("androidx.room:room-compiler:2.6.1")

    // Coil: mostrar la foto tomada con la cámara
    implementation("io.coil-kt:coil-compose:2.7.0")

    // DataStore: la sesión (id, nombre y rol del usuario)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // GPS: ubicación actual
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Escáner QR de Google
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // ZXing: genera la imagen de la etiqueta QR de cada equipo
    implementation("com.google.zxing:core:3.5.3")
}