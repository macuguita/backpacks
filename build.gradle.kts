plugins {
    id("net.fabricmc.fabric-loom-remap").version("1.14-SNAPSHOT")
    id("maven-publish")
    id("me.modmuss50.mod-publish-plugin").version("1.0.0")
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
    val exclusiveRepos = listOf(
        Triple("ParchmentMC", "https://maven.parchmentmc.org", listOf("org.parchmentmc.data")),
        Triple("Shedaniel", "https://maven.shedaniel.me/", listOf("me.shedaniel.cloth")),
        Triple("TerraformersMC", "https://maven.terraformersmc.com/", listOf("com.terraformersmc", "dev.emi")),
        Triple("Ladysnake", "https://maven.ladysnake.org/releases", listOf("org\\.ladysnake(\\..+)?")),
        Triple("Modrinth", "https://api.modrinth.com/maven", listOf("maven.modrinth")),
        Triple("BlameJared", "https://maven.blamejared.com", listOf("net\\.darkhax\\..+", "mezz.jei")),
        Triple("WispForest", "https://maven.wispforest.io/releases", listOf("io\\.wispforest(\\..+)?")),
    )

    exclusiveRepos.forEach { (name, url, groups) ->
        exclusiveContent {
            forRepository {
                maven {
                    this.name = name
                    setUrl(url)
                }
            }
            if (groups.isNotEmpty())
                filter {
                    groups.forEach { includeGroupByRegex(it) }
                }
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

    if (true) {
        modImplementation("io.wispforest:accessories-fabric:${BuildConfig.accessoriesVersion}") {
            exclude("net.fabricmc.fabric-api")
        }
    } else {
        modCompileOnly("io.wispforest:accessories-fabric:${BuildConfig.accessoriesVersion}") {
            exclude("net.fabricmc.fabric-api")
            exclude("org.ladysnake.cardinal-components-api")
        }
    }
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

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

    modRuntimeOnly("me.shedaniel.cloth:cloth-config-fabric:15.0.140") {
        exclude("net.fabricmc.fabric-api")
    }
    modRuntimeOnly("maven.modrinth:freecam:1.3.0+mc1.21.1"){
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

publishMods {
    changelog = changelogText
    file.set(tasks.remapJar.get().archiveFile)
    additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)
    displayName = BuildConfig.modName + " " + BuildConfig.modVersion
    version = BuildConfig.modVersion
    if (BuildConfig.modVersion.contains("beta")) {
        type = BETA
    } else {
        type = STABLE
    }
    modLoaders.add("fabric")
    modLoaders.add("quilt")
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null || providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null
    modrinth {
        projectId = "MjD9CI06"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        for (version in BuildConfig.supportedVersions)
            minecraftVersions.add(version)
        requires("fabric-api")
        requires("macu-lib")
        optional("trinkets")
        embeds("cardinal-components-api")
    }
    modrinth("modrinthNeoforge") {
        modLoaders.empty()
        modLoaders.add("neoforge")
        projectId = "MjD9CI06"
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        for (version in BuildConfig.supportedVersions)
            minecraftVersions.add(version)
        requires("forgified-fabric-api")
        requires("macu-lib")
        requires("connector")
        embeds("cardinal-components-api")
    }
    curseforge {
        projectId = "1361094"
        changelogType = "markdown"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        for (version in BuildConfig.supportedVersions)
            minecraftVersions.add(version)
        javaVersions.add(JavaVersion.VERSION_21)
        clientRequired = true
        serverRequired = true
        projectSlug = "guitas-backpacks"
        requires("fabric-api")
        requires("macu-lib")
        optional("trinkets")
        embeds("cardinal-components-api")
    }
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
