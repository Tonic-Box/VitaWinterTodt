plugins {
    id("java")
}

group = "com.tonic.vitawintertodt"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
        content {
            includeGroupByRegex("net\\.runelite.*")
        }
    }
    mavenCentral()
}

val apiVersion = "latest.release"

dependencies {
    compileOnly("net.runelite:client:$apiVersion")
    compileOnly("com.tonic:base-api:$apiVersion")
    compileOnly("com.tonic:api:$apiVersion")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

tasks.build {
    finalizedBy("deployToRuneLite")
}

tasks.register<Copy>("deployToRuneLite") {
    description = "Copy built JAR to RuneLite sideloaded-plugins directory"

    dependsOn(tasks.jar)

    from(tasks.jar.get().archiveFile)
    into("${System.getProperty("user.home")}/.runelite/sideloaded-plugins")

    doLast {
        println("Deployed ${tasks.jar.get().archiveFileName.get()} to RuneLite sideloaded-plugins")
    }
}