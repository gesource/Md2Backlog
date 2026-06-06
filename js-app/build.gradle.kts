plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser()
        nodejs()
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(project(":core"))
        }
    }
}
