import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // 学习用：预留 iOS target。assembleDebug 只编 Android，不会编这些。
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation("androidx.activity:activity-compose:1.9.3")
        }
    }
}

android {
    namespace = "com.haha.kmp"
    compileSdk = (rootProject.extra["compileSdkVersion"] as Int)
    defaultConfig {
        minSdk = (rootProject.extra["minSdkVersion"] as Int)
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileOptions {
        sourceCompatibility = rootProject.extra["sourceCompatibility_javaVersion"] as JavaVersion
        targetCompatibility = rootProject.extra["targetCompatibility_javaVersion"] as JavaVersion
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}
