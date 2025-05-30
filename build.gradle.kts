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
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
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
    version = "1.18.38"
}
