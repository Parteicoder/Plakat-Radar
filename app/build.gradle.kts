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
        versionCode = 24
        versionName = "0.10.14-excel-friendly-authority-csv"
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
        if (mainActivity.isFile) {
            val original = mainActivity.readText()
            val fixed = original.replace(
                "fun close" + "Keyboard() { focusManager.clearFocus(force = true) }",
                "val close" + "Keyboard: () -> Unit = { focusManager.clearFocus(force = true) }"
            )
            if (fixed != original) mainActivity.writeText(fixed)
        }
    }
}

tasks.named("preBuild") {
    dependsOn("normalizeKeyboardCallbacks")
}
