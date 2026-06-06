plugins {
    kotlin("multiplatform")
}

kotlin {
    // JVM target (for Compose Multiplatform desktop)
    jvm()

    // Native targets
    mingwX64()
    macosArm64()

    // JS target (browser + Node.js)
    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains:markdown:0.7.3")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
