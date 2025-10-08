object BuildConfig {
    val minecraftVersion: String = "1.21.9"
    val minecraftVersionRange: String = "~1.21.9"
    val yarnMappings: String = minecraftVersion + "+build.1"
    val loaderVersion: String = "0.17.2"

    val modVersion: String = minecraftVersion + "-beta+1"
    val mavenGroup: String = "com.macuguita.backpacks"
    val modId: String = "gbackpacks"
    val modName: String = "guita's Backpacks"
    val description: String = "backpacks!"
    val license: String = "MIT"

    val fabricVersion: String = "0.134.0+" + minecraftVersion
    val modMenuVersion: String = "16.0.0-rc.1"
    //val emiVersion: String = "1.1.22+" + minecraftVersion

    val maculibVersion: String = "1.0.5-" + minecraftVersion
    val trinketsVersion: String = "3.11.0-1.21.10-rc1"
    val ccaVersion: String = "7.1.0-beta.1"
}
