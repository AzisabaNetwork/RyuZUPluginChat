plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.lombok)
}

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

group = "net.azisaba"
version = "4.5.1"
description = "RyuZUPluginChat"
java.sourceCompatibility = JavaVersion.VERSION_1_8

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

lombok {
    version = "1.18.38"
}

tasks.shadowJar {
    isEnableRelocation = true
    relocationPrefix = "net.azisaba.ryuzupluginchat.dependency"
}
