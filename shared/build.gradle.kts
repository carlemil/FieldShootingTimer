@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // Host-test-only target: lets logic and Compose UI tests run on the dev
    // host (and CI) without an emulator. Nothing ships from jvmMain.
    jvm()

    val xcf = XCFramework("Shared")
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcf.add(this)
        }
    }

    // The custom uiTest source set below adds explicit dependsOn edges, which
    // makes Kotlin skip the default hierarchy template — silently detaching
    // iosTest from the iOS test compilations. Re-apply it explicitly so both
    // the template wiring and the custom edges coexist.
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            api(compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.androidx.datastore.core.okio)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.ktx)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            // Skiko runtime for the current host so runComposeUiTest can render
            // headlessly, plus Dispatchers.Main (Swing) for viewModelScope.
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
        // Compose UI tests (runComposeUiTest) shared between the jvm and iOS
        // test compilations. Deliberately kept off the Android unit-test
        // classpath, which cannot execute them.
        val uiTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                implementation(compose.uiTest)
            }
        }
        jvmTest.get().dependsOn(uiTest)
        iosTest.get().dependsOn(uiTest)
    }
}

android {
    namespace = "se.kjellstrand.fieldshootingtimer.shared"
    compileSdk = 37
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "se.kjellstrand.fieldshootingtimer.resources"
}
