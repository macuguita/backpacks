plugins {
    id("fabric-loom").version("1.13-SNAPSHOT")
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
        register("clientMacuguita") {
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
    if (project.file("src/main/resources/${BuildConfig.modId}.accesswidener").exists()) {
        accessWidenerPath = project.file("src/main/resources/${BuildConfig.modId}.accesswidener")
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
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
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

configurations {
    create("prodMods")
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.minecraftVersion}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${BuildConfig.minecraftVersion}:${BuildConfig.parchmentMappings}@zip")
    })
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
    if (true) {
        modImplementation("maven.modrinth:trinkets-canary:${BuildConfig.trinketsVersion}") {
            exclude("net.fabricmc.fabric-api")
            exclude("org.ladysnake.cardinal-components-api")
        }
        add("prodMods", "maven.modrinth:trinkets-canary:${BuildConfig.trinketsVersion}")
    } else {
        modCompileOnly("maven.modrinth:trinkets-canary:${BuildConfig.trinketsVersion}") {
            exclude("net.fabricmc.fabric-api")
            exclude("org.ladysnake.cardinal-components-api")
        }
    }

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

    add("prodMods", "net.fabricmc.fabric-api:fabric-api:${BuildConfig.fabricVersion}")
    add("prodMods", "maven.modrinth:macu-lib:${BuildConfig.maculibVersion}-fabric")
    add("prodMods", "com.terraformersmc:modmenu:${BuildConfig.modMenuVersion}")
}

tasks.register<net.fabricmc.loom.task.prod.ClientProductionRunTask>("prodClient") {

    mods.from(configurations.named("prodMods"))
    //jvmArgs.add("-Dfabric.client.gametest")
    programArgs.add("--username=macuguita")
    programArgs.add("--uuid=0e56050b-ee27-478a-a345-d2b384919081")
    runDir.set(file("run"))
    useXVFB = false

    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    )
}

tasks.register<net.fabricmc.loom.task.FabricModJsonV1Task>("genModJson") {
    outputFile = project.file("src/main/generated/fabric.mod.json")

    json {
        modId = BuildConfig.modId
        version = BuildConfig.modVersion
        name = BuildConfig.modName
        description = BuildConfig.description
        author("macuguita") {
            contactInformation = mapOf(
                "discord" to "macuguita"
            )
        }
        contactInformation.set(mapOf(
            "homepage" to "https://macuguita.com",
            "sources" to "https://github.com/macuguita/backpacks"
        ))
        licenses = listOf(BuildConfig.license)
        icon("assets/${BuildConfig.modId}/icon.png")
        mixin("${BuildConfig.modId}.mixins.json")
        accessWidener = "${BuildConfig.modId}.accesswidener"
        environment = "*"

        entrypoint("main", "com.macuguita.backpacks.common.GuitaBackpacks")
        entrypoint("client", "com.macuguita.backpacks.client.GuitaBackpacksClient")
        entrypoint("fabric-datagen", "com.macuguita.backpacks.datagen.GuitaBackpacksDatagen")
        entrypoint("cardinal-components", "com.macuguita.backpacks.common.components.GuitaBackpacksComponents")

        depends("fabricloader", ">=${BuildConfig.loaderVersion}")
        depends("minecraft", BuildConfig.minecraftVersionRange)
        depends("java", ">=21")
        depends("fabric-api", "*")
        depends("macu_lib", ">=${BuildConfig.maculibVersion}")

        suggests("trinkets", "*")

        customData.put("cardinal-components", arrayOf("${BuildConfig.modId}:backpacks", "${BuildConfig.modId}:equipment"))
    }
}

tasks.processResources {}

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
    type = if (BuildConfig.modVersion.contains("beta")) {
        BETA
    } else {
        STABLE
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
        optional("trinkets-canary")
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
        embeds("cardinal-components-api")
    }
    github {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        repository = providers.environmentVariable("GITHUB_REPOSITORY").getOrElse("macuguita/dryRun")
        commitish = providers.environmentVariable("GITHUB_REF_NAME").getOrElse("dryrun")

        tagName = "release/${BuildConfig.modVersion}"
        allowEmptyFiles = true
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
