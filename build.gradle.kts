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
            }
        }
    }
}