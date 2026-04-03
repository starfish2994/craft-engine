package net.momirealms

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.exclude

fun DependencyHandlerScope.nbt(project: org.gradle.api.Project, impl : Boolean = false) {
    fun v(key: String) = project.rootProject.property(key).toString()
    val name = if (impl) "implementation" else "compileOnly"
    add(name, "net.momirealms:sparrow-nbt:${v("sparrow_nbt_version")}")
    add(name, "net.momirealms:sparrow-nbt-adventure:${v("sparrow_nbt_version")}")
    add(name, "net.momirealms:sparrow-nbt-codec:${v("sparrow_nbt_version")}")
    add(name, "net.momirealms:sparrow-nbt-legacy-codec:${v("sparrow_nbt_version")}")
}

fun DependencyHandlerScope.common(project: org.gradle.api.Project) {
    fun v(key: String) = project.rootProject.property(key).toString()
    add("compileOnly", project.files("${project.rootProject.rootDir}/libs/boosted-yaml-${v("boosted_yaml_version")}.jar"))
    add("compileOnly", "com.google.guava:guava:${v("guava_version")}")
    add("compileOnly", "com.google.code.gson:gson:${v("gson_version")}")
    add("compileOnly", "it.unimi.dsi:fastutil:${v("fastutil_version")}")
    add("compileOnly", "org.joml:joml:${v("joml_version")}")
    add("compileOnly", "org.snakeyaml:snakeyaml-engine:${v("snake_yaml_version")}")
    add("compileOnly", "org.slf4j:slf4j-api:${v("slf4j_version")}")
    add("compileOnly", "org.apache.logging.log4j:log4j-core:${v("log4j_version")}")
    add("compileOnly", "com.github.ben-manes.caffeine:caffeine:${v("caffeine_version")}")
    add("compileOnly", "commons-io:commons-io:${v("commons_io_version")}")
    add("compileOnly", "com.mojang:datafixerupper:${v("datafixerupper_version")}")
    add("compileOnly", "com.mojang:authlib:${v("authlib_version")}")
    add("compileOnly", "ca.spottedleaf:concurrentutil:${v("concurrent_util_version")}")
    add("compileOnly", "org.ahocorasick:ahocorasick:${v("ahocorasick_version")}")
    add("compileOnly", "com.bucket4j:bucket4j_jdk17-core:${v("bucket4j_version")}")
    add("compileOnly", "com.ezylang:EvalEx:${v("evalex_version")}")
    add("compileOnly", "com.google.jimfs:jimfs:${v("jimfs_version")}")
}

fun DependencyHandlerScope.netty(project: org.gradle.api.Project) {
    fun v(key: String) = project.rootProject.property(key).toString()
    add("compileOnly", "io.netty:netty-all:${v("netty_version")}")
    add("compileOnly", "io.netty:netty-codec-http:${v("netty_version")}")
}

fun DependencyHandlerScope.compression(project: org.gradle.api.Project) {
    fun v(key: String) = project.rootProject.property(key).toString()
    add("compileOnly", "com.github.luben:zstd-jni:${v("zstd_version")}")
    add("compileOnly", "at.yawk.lz4:lz4-java:${v("lz4_version")}")
}

fun DependencyHandlerScope.cloud(project: org.gradle.api.Project) {
    fun v(key: String) = project.rootProject.property(key).toString()
    add("compileOnly", "com.mojang:brigadier:${v("mojang_brigadier_version")}")
    add("compileOnly", "org.incendo:cloud-core:${v("cloud_core_version")}")
    add("compileOnly", "org.incendo:cloud-minecraft-extras:${v("cloud_minecraft_extras_version")}")
    add("compileOnly", "org.incendo:cloud-paper:${v("cloud_paper_version")}")
}

fun DependencyHandlerScope.paperServer(project: org.gradle.api.Project) {
    fun v(key: String) = project.rootProject.property(key).toString()
    add("compileOnly", "io.papermc.paper:paper-api:${v("paper_version")}-R0.1-SNAPSHOT")
}

fun DependencyHandlerScope.asm(project: org.gradle.api.Project) {
    fun v(key: String) = project.rootProject.property(key).toString()
    add("compileOnly", "org.ow2.asm:asm:${v("asm_version")}")
    add("compileOnly", "net.bytebuddy:byte-buddy:${v("byte_buddy_version")}")
    add("compileOnly", "net.bytebuddy:byte-buddy-agent:${v("byte_buddy_version")}")
}

fun DependencyHandlerScope.adventure(project: org.gradle.api.Project, impl : Boolean = false) {
    fun v(key: String) = project.rootProject.property(key).toString()
    val name = if (impl) "implementation" else "compileOnly"
    add(name, "net.kyori:adventure-api:${v("adventure_bundle_version")}")
    add(name, "net.kyori:adventure-text-minimessage:${v("adventure_bundle_version")}")
    add(name, "net.kyori:adventure-text-serializer-json-legacy-impl:${v("adventure_bundle_version")}")
    add(name, "net.kyori:adventure-text-serializer-legacy:${v("adventure_bundle_version")}")
    val gsonSerializer = add(name, "net.kyori:adventure-text-serializer-gson:${v("adventure_bundle_version")}")
    (gsonSerializer as? ExternalModuleDependency)?.apply {
        exclude("com.google.code.gson", "gson")
    }
}