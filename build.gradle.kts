plugins {
    id("java")
}

group = "arjun.path_tracer_cuda"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.4.1"
val lwjglNatives = "natives-windows"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")

    // 3. LWJGL Natives (Notice the double colon '::' to skip the version)
    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")

    // Change these two lines in your build.gradle.kts
    // 4. JCuda
    implementation("org.jcuda:jcuda:12.6.0") {
        exclude(module = "jcuda-natives") // Forces Gradle to ignore the Maven placeholders
    }
    runtimeOnly("org.jcuda:jcuda-natives:12.6.0:windows-x86_64")
}

tasks.test {
    useJUnitPlatform()
}