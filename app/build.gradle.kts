plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.facecheckapp"
    compileSdk = 36 // ❗ เปลี่ยนตรงนี้เป็นตัวเลข ไม่ใช่ release(36)

    defaultConfig {
        applicationId = "com.example.facecheckapp"
        minSdk = 24
        targetSdk = 36 // ❗ ตัวเลขเช่นเดียวกัน
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
}

dependencies {
    // ✅ Android base libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ✅ CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // ✅ ML Kit Face Detection
    implementation("com.google.mlkit:face-detection:16.1.5")

    // ✅ Firebase (ใช้ BOM เพื่อให้เวอร์ชันเข้ากันอัตโนมัติ)
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database-ktx")

    // (ถ้าภายหลังอยากเก็บไฟล์ภาพ)
    implementation("com.google.firebase:firebase-storage-ktx")
}
