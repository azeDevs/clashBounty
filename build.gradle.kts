plugins { 
    kotlin("multiplatform").version("1.3.21") 
}

repositories { 
    mavenCentral() 
}

kotlin {
    mingwX64("mingw").compilations["main"].defaultSourceSet {
        dependencies {
            implementation(files("src/libs/kwinhelp.klib"))
            implementation(files("src/libs/libui-windows.klib"))
            implementation(files("src/libs/libui-ktx-windows.klib"))
        }
    }
    mingwX64("mingw") {
        binaries {
            executable("main", listOf(RELEASE)) {
                // Build a binary on the basis of the test compilation.
                compilation = compilations["main"]
                // Base name for the output file.
                baseName = "gearNet"
                // Custom entry point function.
                entryPoint = "classes.main"
                windowsResources("gui.rc")
                linkerOpts("-mwindows")
            }
        }
    }
}

fun org.jetbrains.kotlin.gradle.plugin.mpp.Executable.windowsResources(rcFileName: String) {
    val taskName = linkTaskName.replaceFirst("link", "windres")
    val inFile = compilation.defaultSourceSet.resources.sourceDirectories.singleFile.resolve(rcFileName)
    val outFile = buildDir.resolve("processedResources/$taskName.res")

    val windresTask = tasks.create<Exec>(taskName) {
        val konanUserDir = System.getenv("KONAN_DATA_DIR") ?: "${System.getProperty("user.home")}/.konan"
        val konanLlvmDir = "$konanUserDir/dependencies/msys2-mingw-w64-x86_64-gcc-7.3.0-clang-llvm-lld-6.0.1/bin"

        inputs.file(inFile)
        outputs.file(outFile)
        commandLine("$konanLlvmDir/windres", inFile, "-D_${buildType.name}", "-O", "coff", "-o", outFile)
        environment("PATH", "$konanLlvmDir;${System.getenv("PATH")}")

        dependsOn(compilation.compileKotlinTask)
    }

    linkTask.dependsOn(windresTask)
    linkerOpts(outFile.toString())
}