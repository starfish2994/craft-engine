import net.minecrell.pluginyml.paper.PaperPluginDescription
import net.momirealms.paperServer
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("de.eldoria.plugin-yml.paper") version "0.7.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.gtemc.net/releases/")
    mavenCentral()
}

dependencies {
    paperServer(project)

    implementation(project(":core"))
    implementation(project(":bukkit"))
    implementation(project(":bukkit:legacy"))
    implementation(project(":bukkit:compatibility"))
    implementation(project(":bukkit:compatibility:legacy"))
    implementation(project(":common-files"))

    // concurrentutil
    implementation(files("${rootProject.rootDir}/libs/concurrentutil-${rootProject.properties["concurrent_util_version"]}.jar"))

    implementation("net.momirealms:sparrow-util:${rootProject.properties["sparrow_util_version"]}")
    implementation("net.momirealms:antigrieflib:${rootProject.properties["anti_grief_version"]}")
    implementation("net.momirealms:craft-engine-nms-helper-mojmap:${rootProject.properties["nms_helper_version"]}")
    implementation("cn.gtemc:itembridge:${rootProject.properties["itembridge_version"]}")
    implementation("cn.gtemc:levelerbridge:${rootProject.properties["levelerbridge_version"]}")
    implementation(files("${rootProject.rootDir}/libs/jni-internal-lookup.jar"))
}

paper {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "net.momirealms.craftengine.bukkit.plugin.PaperCraftEnginePlugin"
    bootstrapper = "net.momirealms.craftengine.bukkit.plugin.PaperCraftEngineBootstrap"
    version = rootProject.properties["project_version"] as String
    name = "CraftEngine"
    apiVersion = "1.20"
    authors = listOf("XiaoMoMi")
    contributors = listOf("https://github.com/Xiao-MoMi/craft-engine/graphs/contributors")
    foliaSupported = true
    serverDependencies {
        // WorldEdit
        register("WorldEdit") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
        register("FastAsyncWorldEdit") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = false
        }

        register("PlaceholderAPI") { required = false }
        register("Skript") { required = false }
        register("LuckPerms") { required = false }
        register("ViaVersion") { required = false }
        register("QuickShop-Hikari") { required = false }

        // PacketEvents
        register("GrimAC") { required = false }
        register("packetevents") { required = false }

        // Geyser
        register("Geyser-Spigot") { required = false }
        register("floodgate") { required = false }

        // AdvancedSlimePaper
        register("SlimeWorldPlugin") { required = false }
        register("SlimeWorldManager") { required = false }
        register("ASPaperPlugin") { required = false }

        // external tag
        register("CustomNameplates") { required = false }

        // external models
        register("ModelEngine") { required = false }
        register("BetterModel") { required = false }

        // external items
        register("AzureFlow") { required = false }
        register("CustomFishing") { required = false }
        register("EcoArmor") { required = false }
        register("EcoCrates") { required = false }
        register("EcoItems") { required = false }
        register("EcoMobs") { required = false }
        register("EcoPets") { required = false }
        register("EcoScrolls") { required = false }
        register("ExecutableItems") { required = false }
        register("HeadDatabase") { required = false }
        register("HMCCosmetics") { required = false }
        register("ItemsAdder") { required = false }
        register("MagicGem") { required = false }
        register("MMOItems") { required = false }
        register("MythicMobs") { required = false }
        register("NeigeItems") { required = false }
        register("Nexo") { required = false }
        register("Nova") { required = false }
        register("Oraxen") { required = false }
        register("PxRpg") { required = false }
        register("Ratziel") { required = false }
        register("Reforges") { required = false }
        register("Sertraline") { required = false }
        register("Slimefun") { required = false }
        register("StatTrackers") { required = false }
        register("SX-Item") { required = false }
        register("Talismans") { required = false }
        register("Zaphkiel") { required = false }

        // leveler
        register("AuraSkills") { required = false }
        register("AureliumSkills") { required = false }
        register("mcMMO") { required = false }
        register("MMOCore") { required = false }
        register("Jobs") { required = false }
        register("EcoSkills") { required = false }
        register("EcoJobs") { required = false }

        // anti grief lib
        register("Dominion") { required = false }
        register("WorldGuard") { required = false }
        register("Kingdoms") { required = false }
        register("Lands") { required = false }
        register("IridiumSkyblock") { required = false }
        register("CrashClaim") { required = false }
        register("GriefDefender") { required = false }
        register("HuskClaims") { required = false }
        register("BentoBox") { required = false }
        register("HuskTowns") { required = false }
        register("PlotSquared") { required = false }
        register("Residence") { required = false }
        register("SuperiorSkyblock2") { required = false }
        register("Towny") { required = false }
        register("FabledSkyBlock") { required = false }
        register("GriefPrevention") { required = false }
        register("RedProtect") { required = false }
        register("Landlord") { required = false }
        register("uSkyBlock") { required = false }
        register("XClaim") { required = false }
        register("UltimateClaims") { required = false }
        register("UltimateClans") { required = false }
        register("PreciousStones") { required = false }
        register("hClaims") { required = false }
        register("Factions") { required = false }
        register("NoBuildPlus") { required = false }
    }
}

artifacts {
    implementation(tasks.shadowJar)
}

tasks {
    shadowJar {
        relocation.applyCommon(this)
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        from(project(":bukkit:proxy").tasks.shadowJar.flatMap { it.archiveFile })
        archiveFileName = "${rootProject.name}-paper-plugin-${rootProject.properties["project_version"]}.jar"
        destinationDirectory.set(file("$rootDir/target"))
    }
}

/**
 * Register Run Dev Server Tasks
 */
listOf(
    "26.1.2",
).forEach {
    registerPaperTask(it, javaVersion = 25)
}

listOf(
    "1.21.11",
    "1.21.10",
    "1.21.8",
    "1.21.5",
    "1.21.4",
    "1.21.3",
    "1.21.1",
    "1.20.6",
    "1.20.4",
    "1.20.2",
    "1.20.1",
).forEach {
    registerPaperTask(it)
}

fun registerPaperTask(
    version: String,
    dirName: String = version,
    javaVersion : Int = 21,
    serverJar: File? = null
) {
    listOf(version).forEach { taskName ->
        tasks.register(taskName, RunServer::class) {
            group = "run dev server"
            minecraftVersion(version)
            serverJar?.let { serverJar(it) }
            pluginJars.from(tasks.shadowJar.flatMap { it.archiveFile })
            runDirectory = rootProject.layout.projectDirectory.dir("runPaper/${dirName}")
            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(javaVersion)
            }
            systemProperties["com.mojang.eula.agree"] = true
            systemProperties["net.momirealms.craftengine.dev"] = true
            jvmArgs("-Dorg.bukkit.plugin.java.LibraryLoader.centralURL=https://maven.aliyun.com/repository/central")
            jvmArgs("-Dsun.stdout.encoding=UTF-8")
            jvmArgs("-Dsun.stderr.encoding=UTF-8")
            jvmArgs("-Ddisable.watchdog=true")
        }
    }
}