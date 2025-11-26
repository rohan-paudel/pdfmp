import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.mp)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.kt)
    alias(libs.plugins.android.lib)
}

kotlin {
    jvmToolchain(21)
    jvm {
        mainRun {
            mainClass = "com.dshatz.pdfmp.MainKt"
        }
    }
    androidTarget()
    val iosTargets = listOf(iosX64(), iosArm64(), iosSimulatorArm64())

    val xcf = XCFramework()
    iosTargets.forEach {
        it.binaries.framework {
            baseName = "pdfmpcompose"
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":pdfmp-compose"))
//            implementation("com.dshatz.pdfmp:pdfmp-compose-jvm:unspecified")
            implementation(compose.runtime)
            implementation(compose.material3)
            implementation(compose.components.resources)
        }
        /*jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.desktop.common)
        }*/
    }
}

android {
    namespace = "com.dshatz.pdfmp.sampleshared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}