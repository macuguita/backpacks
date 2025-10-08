import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options

plugins {
    id("fabric-loom").version("1.11-SNAPSHOT")
    id("maven-publish")
}

loom {
    runs {
        register("datagen") {
            client()
            name = "Data Generation"

            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.modid=${BuildConfig.modId}")
            vmArg("-Dfabric-api.datagen.output-dir=${project.file("src/main/generated")}")
            runDir("build/datagen")

            ideConfigGenerated(true)
        }
        register("client2") {
            client()
            name = "Minecraft Client macuguita"
            programArgs.add("--username=macuguita")
            programArgs.add("--uuid=0e56050b-ee27-478a-a345-d2b384919081")
        }
        configureEach {
            if (name == "client") {
                programArgs.add("--username=Ladybrine")
                programArgs.add("--uuid=5d66606c-949c-47ce-ba4c-a1b9339ba3c8")
            }
        }
    }
    accessWidenerPath.set(file(project.file("src/main/resources/${BuildConfig.modId}.accesswidener")))
}

sourceSets {
    main {
        resources.srcDir("src/main/generated")
        resources.exclude(".cache")
    }
}

version = BuildConfig.modVersion
group = BuildConfig.mavenGroup

base {
    archivesName.set(BuildConfig.modId)
}

repositories {
    maven {
        name = "Shedaniel maven"
        url = uri("https://maven.shedaniel.me/")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Ladysnake"
        url = uri("https://maven.ladysnake.org/releases")
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.minecraftVersion}")
    mappings("net.fabricmc:yarn:${BuildConfig.yarnMappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${BuildConfig.loaderVersion}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${BuildConfig.fabricVersion}")

    modImplementation("maven.modrinth:macu-lib:${BuildConfig.maculibVersion}-fabric"){
        exclude("net.fabricmc.fabric-api")
    }

    modImplementation("com.terraformersmc:modmenu:${BuildConfig.modMenuVersion}"){
        exclude("net.fabricmc.fabric-api")
    }
//    modLocalRuntime("dev.emi:emi-fabric:${BuildConfig.emiVersion}"){
//        exclude("net.fabricmc.fabric-api")
//    }
//    if (true) {
//        modImplementation("dev.emi:trinkets:${BuildConfig.trinketsVersion}") {
//            exclude("net.fabricmc.fabric-api")
//            exclude("org.ladysnake.cardinal-components-api")
//        }
//    } else {
//        modCompileOnly("dev.emi:trinkets:${BuildConfig.trinketsVersion}") {
//            exclude("net.fabricmc.fabric-api")
//            exclude("org.ladysnake.cardinal-components-api")
//        }
//    }

    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-base:${BuildConfig.ccaVersion}"){
        exclude("net.fabricmc.fabric-api")
    }
    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-scoreboard:${BuildConfig.ccaVersion}"){
        exclude("net.fabricmc.fabric-api")
    }
    modImplementation("org.ladysnake.cardinal-components-api:cardinal-components-entity:${BuildConfig.ccaVersion}"){
        exclude("net.fabricmc.fabric-api")
    }

    include("org.ladysnake.cardinal-components-api:cardinal-components-base:${BuildConfig.ccaVersion}"){
        exclude("net.fabricmc.fabric-api")
    }
    include("org.ladysnake.cardinal-components-api:cardinal-components-scoreboard:${BuildConfig.ccaVersion}"){
        exclude("net.fabricmc.fabric-api")
    }
    include("org.ladysnake.cardinal-components-api:cardinal-components-entity:${BuildConfig.ccaVersion}"){
        exclude("net.fabricmc.fabric-api")
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(
            "version"  to BuildConfig.modVersion,
            "modId"                 to BuildConfig.modId,
            "modName"               to BuildConfig.modName,
            "description"           to BuildConfig.description,
            "license"               to BuildConfig.license,
            "loaderVersion"         to BuildConfig.loaderVersion,
            "minecraftVersion"      to BuildConfig.minecraftVersion,
            "minecraftVersionRange" to BuildConfig.minecraftVersionRange,
            "macuLibVersion"        to BuildConfig.maculibVersion
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${BuildConfig.modId}"}
    }
}

val changelogText: String = rootProject.file("CHANGELOG.md").readText()

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("MjD9CI06")
    versionNumber.set(BuildConfig.modVersion)
    versionName.set("guita's Backpacks ${BuildConfig.modVersion}")
    if (BuildConfig.modVersion.contains("beta")) {
        versionType.set("beta")
    } else {
        versionType.set("release")
    }
    uploadFile.set(tasks.named("remapJar").get())
    additionalFiles.add(tasks.named("remapSourcesJar").get())
    changelog.set(changelogText)
    gameVersions.add(BuildConfig.minecraftVersion)
    loaders.addAll("fabric", "quilt")

    dependencies {
        required.project("fabric-api")
        required.project("macu-lib")
        //optional.project("trinkets-canary")
        embedded.project("cardinal-components-api")
    }
}

curseforge {
    options(closureOf<Options> {
        forgeGradleIntegration = false
    })

    project(closureOf<CurseProject> {
        apiKey = System.getenv("CURSEFORGE_TOKEN")
        id = "1308420"
        if (BuildConfig.modVersion.contains("beta")) {
            releaseType = ("beta")
        } else {
            releaseType = ("release")
        }
        addGameVersion(BuildConfig.minecraftVersion)
        addGameVersion("Fabric")
        addGameVersion("Quilt")
        addGameVersion("Java 21")

        changelogType = "markdown"
        changelog = changelogText

        mainArtifact(tasks.named("remapJar").get(), closureOf<CurseArtifact> {
            displayName = "guita's Backpacks ${BuildConfig.modVersion}"
        })

        addArtifact(tasks.named("remapSourcesJar").get())

        relations(closureOf<CurseRelation> {
            requiredDependency("fabric-api")
            requiredDependency("macu-lib")
            embeddedLibrary("cardinal-components-api")
        })
    })
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = BuildConfig.modId
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
