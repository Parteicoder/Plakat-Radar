plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "de.bsw.plakatradar"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.bsw.plakatradar"
        minSdk = 26
        targetSdk = 35
        versionCode = 23
        versionName = "0.10.13-authority-export-without-qr"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

tasks.register("normalizeKeyboardCallbacks") {
    doLast {
        val mainActivity = file("src/main/java/de/bsw/plakatradar/MainActivity.kt")
        if (mainActivity.exists()) {
            val oldText = "fun close" + "Keyboard() { focusManager.clearFocus(force = true) }"
            val newText = "val close" + "Keyboard: () -> Unit = { focusManager.clearFocus(force = true) }"
            val current = mainActivity.readText()
            val fixed = current.replace(oldText, newText)
            if (fixed != current) {
                mainActivity.writeText(fixed)
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn("normalizeKeyboardCallbacks")
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.2")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-nearby:19.3.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.zxing:core:3.5.3")
}
