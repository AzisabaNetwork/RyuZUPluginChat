plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.lombok)
}

group = "net.azisaba"
version = "4.5.1"
description = "RyuZUPluginChat"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val orgName: String by project
val repoUrl: String by project

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.azisaba.net/repository/maven-public/")
    maven("https://papermc.io/repo/repository/maven-snapshots/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation(libs.discord4j)
    implementation(libs.jedis)
    implementation(libs.aikar.taskchain)
    implementation(libs.semver4j)
    compileOnly(libs.paper.api)
    compileOnly(libs.luckperms.api)
    compileOnly(libs.lunachatplus)
    compileOnly(libs.jetbrains.annotation)

    // Test dependencies
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testCompileOnly(libs.jetbrains.annotation)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props =
        mapOf(
            "name" to name,
            "version" to version,
            "description" to description,
            "orgName" to orgName,
            "url" to repoUrl,
        )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    isEnableRelocation = true
    relocationPrefix = "net.azisaba.ryuzupluginchat.dependency"
}

lombok {
    version = libs.versions.lombok.asProvider()
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.jar)
        }
    }

    repositories {
        maven {
            name = "azisaba-repo"
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
            url =
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    uri("https://repo.azisaba.net/repository/maven-snapshots/")
                } else {
                    uri("https://repo.azisaba.net/repository/maven-releases/")
                }
        }
    }
}
