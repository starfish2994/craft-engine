import net.momirealms.adventure
import net.momirealms.nbt
import net.momirealms.netty
import net.momirealms.paperServer

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.momirealms.net/releases/")
    maven("https://repo.gtemc.net/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // papi
    maven("https://maven.enginehub.org/repo/") // worldguard worldedit
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")  // slime world
    maven("https://mvn.lumine.io/repository/maven-public/") // model engine mythic mobs
    maven("https://repo.viaversion.com") // via
    maven("https://repo.skriptlang.org/releases/") // skript
    maven("https://maven.citizensnpcs.co/repo/") // denizen
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-public/") // quickshop
    maven("https://repo.opencollab.dev/main/") // geyser
    maven("https://maven.playpro.com/") // coreprotect
}

dependencies {
    paperServer(project)
    nbt(project)
    netty(project)
    adventure(project)

    compileOnly(project(":core"))
    compileOnly(project(":bukkit"))
    compileOnly(project(":bukkit:proxy"))
    compileOnly(project(":bukkit:compatibility:legacy"))
    compileOnly(files("${rootProject.rootDir}/libs/leafpile-${rootProject.properties["leafpile_version"]}.jar"))

    // Reflection
    compileOnly("net.momirealms:sparrow-reflection:${rootProject.properties["sparrow_reflection_version"]}")
    // NMS
    compileOnly("net.momirealms:craft-engine-nms-helper:${rootProject.properties["nms_helper_version"]}")
    // Placeholder
    compileOnly("me.clip:placeholderapi:${rootProject.properties["placeholder_api_version"]}")
    // SlimeWorld
    compileOnly("com.infernalsuite.asp:api:4.2.0-SNAPSHOT")
    // ModelEngine
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.9")
    // BetterModel
    compileOnly("io.github.toxicity188:bettermodel-bukkit-api:3.3.0")
    compileOnly("com.mojang:authlib:${rootProject.properties["authlib_version"]}")
    // LuckPerms
    compileOnly("net.luckperms:api:5.4")
    // viaversion
    compileOnly("com.viaversion:viaversion-api:5.5.1")
    compileOnly("com.viaversion:viaversion-bukkit:5.5.1")
    // Skript
    compileOnly("com.github.SkriptLang:Skript:2.16.1")
    // Denizen
    compileOnly("com.denizenscript:denizen:1.3.3-SNAPSHOT")
    // FAWE
    compileOnly(platform("com.intellectualsites.bom:bom-newest:1.52"))
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }
    // MythicMobs
    compileOnly("io.lumine:Mythic-Dist:5.9.0")
    // CustomNameplates
    compileOnly("net.momirealms:custom-nameplates:3.0.33")
    // Axiom
    compileOnly(files("${rootProject.rootDir}/libs/AxiomPaperPlugin-5.0.4.jar"))
    // WorldGuard
    compileOnly(files("${rootProject.rootDir}/libs/worldguard-bukkit-7.0.14-dist.jar"))
    // QuickShop
    compileOnly("com.ghostchu:quickshop-api:6.2.0.10")
    // Geyser
    compileOnly("org.geysermc.geyser:api:2.9.0-SNAPSHOT")
    // Floodgate
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")
    // Vault
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    // ItemBridge
    compileOnly("cn.gtemc:itembridge:${rootProject.properties["itembridge_version"]}")
    // LevelerBridge
    compileOnly("cn.gtemc:levelerbridge:${rootProject.properties["levelerbridge_version"]}")
    // CoreProtect
    compileOnly("net.coreprotect:coreprotect:24.0")
}