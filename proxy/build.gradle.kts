import net.momirealms.adventure
import net.momirealms.nbt
import net.momirealms.netty

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.gtemc.net/releases/")
}

dependencies {
    nbt(project, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
    netty(project, JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)
    adventure(project, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
    // Common
    compileOnly("com.google.code.gson:gson:${rootProject.properties["gson_version"]}")
    compileOnly("org.jetbrains:annotations:${rootProject.properties["jetbrains_annotations_version"]}")
    // Reflection
    implementation(files("${rootProject.rootDir}/libs/jni-internal-lookup-1.9.jar"))
    implementation("net.momirealms:sparrow-reflection:${rootProject.properties["sparrow_reflection_version"]}")
    implementation("com.github.ben-manes.caffeine:caffeine:${rootProject.properties["caffeine_version"]}")
}

tasks.named("build") {
    dependsOn(":proxy:velocity:build")
    dependsOn(":proxy:bungeecord:build")
}
