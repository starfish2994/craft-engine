import java.net.URI
import java.util.Properties

plugins {
    `maven-publish`
}

val projectVersion = project.rootProject.property("project_version").toString()
val isSnapshot = projectVersion.endsWith("-SNAPSHOT")

val credentialsFile = file("publishing-credentials.properties")
val credentialsProps = Properties().apply {
    if (credentialsFile.exists()) {
        load(credentialsFile.inputStream())
    }
}

publishing {
    repositories {
        maven {
            val repoName = if (isSnapshot) "snapshots" else "releases"
            name = repoName
            url = URI("https://repo.momirealms.net/$repoName")
            credentials {
                username = credentialsProps.getProperty("repoUsername") ?: System.getenv("REPO_USERNAME")
                password = credentialsProps.getProperty("repoPassword") ?: System.getenv("REPO_PASSWORD")
            }
        }
    }

    publications {
    }
}