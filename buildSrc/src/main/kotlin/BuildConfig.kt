object BuildConfig {
    val minecraftVersion: String = "1.21.1"
    val minecraftVersionRange: String = "~1.21.1"
    val supportedVersions: Array<String> = arrayOf("1.21.1")
    val yarnMappings: String = minecraftVersion + "+build.3"
    val loaderVersion: String = "0.18.3"

    val modVersion: String = minecraftVersion + "-beta+6"
    val mavenGroup: String = "com.macuguita"
    val modId: String = "gbackpacks"
    val modName: String = "guita's Backpacks"
    val description: String = "backpacks!"
    val license: String = "MIT"

    val fabricVersion: String = "0.116.7+" + minecraftVersion
    val modMenuVersion: String = "11.0.3"

    val maculibVersion: String = "2.0.0+" + minecraftVersion
    val accessoriesVersion: String = "1.1.0-beta.52+$minecraftVersion"
    val ccaVersion: String = "6.1.2"
}
