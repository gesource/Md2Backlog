plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64 {
        binaries.executable {
            baseName = "md2backlog"
            entryPoint = "io.github.md2backlog.cli.main"
        }
    }
    macosArm64 {
        binaries.executable {
            baseName = "md2backlog"
            entryPoint = "io.github.md2backlog.cli.main"
        }
    }

    // Kotlin/Native は macOS 向けバイナリに .kexe を自動付与するが、
    // macOS の慣習として実行ファイルに拡張子は不要なため、拡張子なしのコピーを生成する。
    // renameDebugExecutableMacosArm64 / renameReleaseExecutableMacosArm64 タスクを提供する。
    afterEvaluate {
        listOf("Debug", "Release").forEach { buildType ->
            val linkTask = tasks.findByName("link${buildType}ExecutableMacosArm64") ?: return@forEach
            tasks.register("rename${buildType}ExecutableMacosArm64") {
                dependsOn(linkTask)
                doLast {
                    val dir = layout.buildDirectory.dir("bin/macosArm64/${buildType.lowercase()}Executable").get().asFile
                    val kexe = File(dir, "md2backlog.kexe")
                    val out  = File(dir, "md2backlog")
                    if (kexe.exists()) {
                        kexe.copyTo(out, overwrite = true)
                        out.setExecutable(true)
                    }
                }
            }
        }
    }

    sourceSets {
        // nativeMain はデフォルト階層テンプレートで自動生成される
        nativeMain.dependencies {
            implementation(project(":core"))
        }
    }
}
