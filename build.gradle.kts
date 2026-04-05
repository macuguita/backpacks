plugins {
    id("net.fabricmc.fabric-loom").version("1.14-SNAPSHOT")
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
    if (project.file("src/main/resources/${BuildConfig.modId}.classtweaker").exists()) {
        accessWidenerPath = project.file("src/main/resources/${BuildConfig.modId}.classtweaker")
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
    mavenLocal()
    val exclusiveRepos = listOf(
        Triple("macuguita Maven", "https://maven.macuguita.com/releases", listOf("com.macuguita", "folk.sisby", "org.quiltmc.parsers")),
        Triple("ParchmentMC", "https://maven.parchmentmc.org", listOf("org.parchmentmc.data")),
        Triple("Shedaniel", "https://maven.shedaniel.me/", listOf("me.shedaniel.cloth")),
        Triple("TerraformersMC", "https://maven.terraformersmc.com/", listOf("com.terraformersmc", "dev.emi")),
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

configurations {
    create("prodMods")
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.minecraftVersion}")
    implementation("net.fabricmc:fabric-loader:${BuildConfig.loaderVersion}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    implementation("net.fabricmc.fabric-api:fabric-api:${BuildConfig.fabricVersion}")

//    implementation("maven.modrinth:macu-lib:${BuildConfig.maculibVersion}-fabric"){
//        exclude("net.fabricmc.fabric-api")
//    }
    implementation("com.macuguita:macu_lib-fabric:${BuildConfig.maculibVersion}"){
        exclude("net.fabricmc.fabric-api")
    }

//    implementation("com.terraformersmc:modmenu:${BuildConfig.modMenuVersion}"){
//        exclude("net.fabricmc.fabric-api")
//    }

//    if (false) {
//        implementation("io.wispforest:accessories-fabric:${BuildConfig.accessoriessVersion}") {
//            exclude("net.fabricmc.fabric-api")
//        }
//        add("prodMods", "io.wispforest:accessories-fabric:${BuildConfig.accessoriessVersion}")
//    } else {
//        compileOnly("io.wispforest:accessories-fabric:${BuildConfig.accessoriessVersion}") {
//            exclude("net.fabricmc.fabric-api")
//        }
//    }
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

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
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    )
}

tasks.register<net.fabricmc.loom.task.FabricModJsonV1Task>("genModJson") {
    outputFile = project.file("src/main/resources/fabric.mod.json")

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
        accessWidener = "${BuildConfig.modId}.classtweaker"
        environment = "*"

        entrypoint("main", "com.macuguita.backpacks.common.GuitaBackpacks")
        entrypoint("client", "com.macuguita.backpacks.client.GuitaBackpacksClient")
        entrypoint("fabric-datagen", "com.macuguita.backpacks.datagen.GuitaBackpacksDatagen")

        depends("fabricloader", ">=${BuildConfig.loaderVersion}")
        depends("minecraft", BuildConfig.minecraftVersionRange)
        depends("java", ">=25")
        depends("fabric-api", "*")
        depends("macu_lib", ">=${BuildConfig.maculibVersion}")

//        suggests("accessories", "*")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.processResources {
    dependsOn(tasks.named("genModJson"))
}

tasks.named("sourcesJar") {
    dependsOn(tasks.named("genModJson"))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${BuildConfig.modId}"}
    }
}

val changelogText: String = rootProject.file("CHANGELOG.md").readText()

publishMods {
    changelog = changelogText
    file = tasks.jar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile.get() })
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
//        optional("accessories")
    }
    curseforge {
        projectId = "1361094"
        changelogType = "markdown"
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        for (version in BuildConfig.supportedVersions)
            minecraftVersions.add(version)
        javaVersions.add(JavaVersion.VERSION_25)
        clientRequired = true
        serverRequired = true
        projectSlug = "guitas-backpacks"
        requires("fabric-api")
        requires("macu-lib")
//        optional("accessories")
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
