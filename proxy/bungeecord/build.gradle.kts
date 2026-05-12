import net.momirealms.netty

plugins {
    id("net.minecrell.plugin-yml.bungee") version "0.6.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.gtemc.net/releases/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":proxy"))
    netty(project)
    // Platform
    compileOnly("net.md-5:bungeecord-api:${rootProject.properties["bungeecord_version"]}")
    compileOnly("org.jetbrains:annotations:${rootProject.properties["jetbrains_annotations_version"]}")
}

tasks {
    shadowJar {
        relocation.applyProxy(this)
        archiveFileName = "${rootProject.name}-bungeecord-plugin-${rootProject.properties["project_version"]}.jar"
        destinationDirectory.set(file("$rootDir/target"))
    }
}

bungee {
    name = "CraftEngine"
    version = rootProject.properties["project_version"] as String
    main = "net.momirealms.craftengine.proxy.bungeecord.BungeeCordCraftEngine"
    author = "Catnies"
}

artifacts {
    implementation(tasks.shadowJar)
}
