plugins {
    id("net.fabricmc.fabric-loom").version("1.15-SNAPSHOT")
    id("co.uzzu.dotenv.gradle").version("4.0.0")
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
        Triple(
            "macuguita Maven",
            "https://maven.macuguita.com/releases",
            listOf("com.macuguita", "folk.sisby", "org.quiltmc.parsers")
        ),
        Triple("ParchmentMC", "https://maven.parchmentmc.org", listOf("org.parchmentmc.data")),
        Triple("TerraformersMC", "https://maven.terraformersmc.com/", listOf("com.terraformersmc")),
        Triple("Modrinth", "https://api.modrinth.com/maven", listOf("maven.modrinth")),
        Triple("Nucleoid", "https://maven.nucleoid.xyz/releases", listOf("eu.pb4")),
    )

    exclusiveRepos.forEach { (name, url, groups) ->
        if (groups.isNotEmpty()) {
            exclusiveContent {
                forRepository {
                    maven {
                        this.name = name
                        setUrl(url)
                    }
                }
                filter {
                    groups.forEach { includeGroupAndSubgroups(it) }
                }
            }
        } else {
            maven {
                this.name = name
                setUrl(url)
            }
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.minecraftVersion}")
    implementation("net.fabricmc:fabric-loader:${BuildConfig.loaderVersion}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // Fabric API. This is technically optional, but you probably want it anyway.
    implementation("net.fabricmc.fabric-api:fabric-api:${BuildConfig.fabricVersion}")

    implementation("com.macuguita:macu_lib-fabric:${BuildConfig.maculibVersion}") {
        exclude("net.fabricmc.fabric-api")
    }

    implementation("com.terraformersmc:modmenu:${BuildConfig.modMenuVersion}") {
        exclude("net.fabricmc.fabric-api")
    }

    val enableTrinkets = false
    if (enableTrinkets) {
        implementation("eu.pb4:trinkets:${BuildConfig.trinketsVersion}") {
            exclude("net.fabricmc.fabric-api")
        }
    } else {
        compileOnly("eu.pb4:trinkets:${BuildConfig.trinketsVersion}") {
            exclude("net.fabricmc.fabric-api")
        }
    }
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
        contactInformation.set(
            mapOf(
                "homepage" to "https://macuguita.com",
                "sources" to "https://github.com/macuguita/backpacks"
            )
        )
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

        suggests("trinkets", "*")
        suggests("mcqoy", "*")
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
        rename { "${it}_${BuildConfig.modId}" }
    }
}

val changelogText: String = rootProject.file("CHANGELOG.md").readText()

publishMods {
    changelog = changelogText
    file = tasks.jar.map { it.archiveFile.get() }
    additionalFiles.from(tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").map { it.archiveFile.get() })
    displayName = "${BuildConfig.modName} ${BuildConfig.modVersion} for ${BuildConfig.minecraftVersion}"
    version = "${BuildConfig.modVersion}+${BuildConfig.minecraftVersion}"
    type = if (BuildConfig.modVersion.contains("beta")) {
        BETA
    } else {
        STABLE
    }
    modLoaders.add("fabric")
    modLoaders.add("quilt")
    modrinth {
        projectId = "MjD9CI06"
        accessToken = env.MODRINTH_API_KEY.orNull()
        for (version in BuildConfig.supportedVersions)
            minecraftVersions.add(version)
        requires("fabric-api")
        requires("macu-lib")
        optional("trinkets-updated")
        optional("mcqoy")
    }
    curseforge {
        projectId = "1361094"
        changelogType = "markdown"
        accessToken = env.CURSEFORGE_API_KEY.orNull()
        for (version in BuildConfig.supportedVersions)
            minecraftVersions.add(version)
        javaVersions.add(JavaVersion.VERSION_25)
        clientRequired = true
        serverRequired = true
        projectSlug = "guitas-backpacks"
        requires("fabric-api")
        requires("macu-lib")
        optional("mcqoy")
//        optional("trinkets-updated")
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = BuildConfig.mavenGroup
            artifactId = BuildConfig.modId
            version = BuildConfig.modVersion
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
        maven {
            name = "macuguita"
            url = uri("https://maven.macuguita.com/releases")

            credentials {
                username = env.MAVEN_USERNAME.orNull()
                password = env.MAVEN_KEY.orNull()
            }
        }
    }
}
