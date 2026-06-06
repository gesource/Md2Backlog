import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project(":core"))
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.md2backlog.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Pkg, TargetFormat.Msi)
            packageName = "Md2Backlog"
            packageVersion = "1.0.0"
            description = "Markdown to Backlog converter"
            vendor = "Sprix"

            macOS {
                bundleID = "io.github.md2backlog.desktop"
                iconFile.set(project.file("icons/AppIcon.icns"))
            }

            windows {
                iconFile.set(project.file("icons/AppIcon.ico"))
                upgradeUuid = "D7B4F1E2-3C5A-4B8D-9F0E-1A2B3C4D5E6F"
            }
        }
    }
}
