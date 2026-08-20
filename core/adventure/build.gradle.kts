import net.momirealms.adventure

plugins {
    id("craft-engine-publish")
}

repositories {
    mavenCentral()
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    adventure(project, JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
}

configurations.implementation {
    exclude(mapOf("group" to "org.jspecify", "module" to "jspecify"))
}

tasks.shadowJar {
    archiveBaseName.set("adventure-bundle")
    archiveClassifier = ""
    relocate("net.kyori", "net.momirealms.craftengine.libraries")
    relocate("net.momirealms.sparrow.message", "net.momirealms.craftengine.libraries.message")
}

//publishing {
//    publications {
//        create<MavenPublication>("adventure") {
//            groupId = "net.momirealms"
//            artifactId = "craft-engine-adventure"
//            version = rootProject.properties["project_version"].toString()
//            from(components["shadow"])
//            artifact(tasks["sourcesJar"])
//            publication.applyCommonPom(this, "CraftEngine Adventure API")
//        }
//    }
//}