import gg.meza.stonecraft.mod

plugins {
    id("gg.meza.stonecraft")
}

// Version helpers

// 26.1+ ships with Mojang mappings directly, no Loom remap step needed -
// everything below that branches on obfuscation keys off this one flag.
val isDeobfuscated = stonecutter.current.parsed >= "26.1"

// Minimum Java version required per targeted MC version. Forge below 26.1
// crashes with "Unsupported class file major version" if this doesn't match
// the toolchain actually installed for that run.
// Taken from MidnightLib https://github.com/TeamMidnightDust/MidnightLib/blob/multiversion/build.fabric.gradle.kts, thank you!
val requiredJava: JavaVersion = when {
    stonecutter.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    stonecutter.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    stonecutter.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    stonecutter.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}



loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
    }

    mods {
        register(mod.id) {
            sourceSet(sourceSets.main.get())
        }
    }

    if (mod.isForge) {
        forge {
            mixinConfig("${mod.id}.client.mixins.json")
        }
    }
}

// Mod settings (Stonecraft)
modSettings {
    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }

    val authorList = project.property("mod.authors").toString()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val authorsFormatted = if (mod.isFabric) {
        authorList.joinToString("\", \"")   // Dev1", "Dev2", "Dev3
    } else {
        authorList.joinToString(", ")       // Dev1, Dev2, Dev3
    }

    variableReplacements = mapOf(
        "fabricId" to when {
            stonecutter.current.parsed > "1.19.1" -> "fabric-api"
            else -> "fabric"
        },
        "authors" to authorsFormatted,
        "license" to project.property("mod.license").toString(),
        "issueUrl" to project.property("mod.issueUrl").toString()
    )
}

// Stonecutter string replacements - renamed/relocated vanilla classes across
// versions that aren't expressible as simple `//?` conditionals.
stonecutter {
    replacements.string(stonecutter.current.parsed < "1.21.11") {
        replace("Identifier", "ResourceLocation")
    }
    replacements.string(stonecutter.current.parsed > "1.19.4") {
        replace("ItemStack.isSame(", "ItemStack.isSameItem(")
    }
    replacements.string(stonecutter.current.parsed > "1.21.11") {
        replace("GuiGraphics", "GuiGraphicsExtractor")
    }

    replacements.string(stonecutter.current.parsed < "26.2") {
        replace("Minecraft.getInstance().gui.screen()", "Minecraft.getInstance().screen")
        replace("Minecraft.getInstance().gui.setScreen(", "Minecraft.getInstance().setScreen(")
        replace("minecraft.gui.screen()", "minecraft.screen")
        replace("client.gui.setScreen(", "client.setScreen(")
    }

    replacements.string(stonecutter.current.parsed > "26.2") {
        replace("InputConstants.Type.KEYSYM", "InputConstants.Type.KEYBOARD")
    }
}

// Repositories & dependencies
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.meza.gg/releases/")
    maven("https://maven.fabricmc.net/")

    // Modrinth Maven: only ever used for the maven.modrinth group
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

fun shwgconfigVersion(): String =
project.findProperty("shwgconfig_version")?.toString()
?: error("shwgconfig_version is not set in versions/dependencies/${stonecutter.current.version}.properties or gradle.properties")

// if ShwgConfig has no build of, e.g. 1.21.10 using the 1.21.9 build, specify in versions/dependencies/<mc>.properties. Defaults to Minecraft version
fun shwgconfigMcVersion(): String =
project.findProperty("shwgconfig_mc_version")?.toString() ?: mod.minecraftVersion

dependencies {
    // Pre-26.1 (obfuscated) needs the `mod*` configurations for Loom's remap
    // step; 26.1+ (deobfuscated) uses plain Gradle configurations instead.
    val implementationConfiguration = if (isDeobfuscated) "implementation" else "modImplementation"
    val apiConfiguration = if (isDeobfuscated) "api" else "modApi"

    val shwgconfigVersion = shwgconfigVersion()
    val shwgconfigMcVersion = shwgconfigMcVersion()

    // Local Maven (`publishToMavenLocal` in the library project). Stonecraft versions
    // the library as "<version>+mc<mc version>", e.g. 0.2.0-alpha.1+mc1.21.9.
    add(apiConfiguration, "dev.shwg:shwgconfig-${mod.loader}:$shwgconfigVersion+$shwgconfigMcVersion")

    // Modrinth Maven, once the library is approved there. Stonecraft names Modrinth
    // versions "<version>+<loader>-<mc version>".
    //add(apiConfiguration, "maven.modrinth:shwgconfig:$shwgconfigVersion+${mod.loader}-$shwgconfigMcVersion")


    // Required by ModMenuIntegration on Fabric.
    if (mod.isFabric && mod.hasProp("modmenu_version")) {
        add(apiConfiguration, "com.terraformersmc:modmenu:${mod.prop("modmenu_version")}") {
            exclude(group = "net.fabricmc", module = "fabric-loader")
        }
    }

    val mixinExtras = "0.5.5"   // use the newest release; 0.4+ is needed for @WrapMethod

    // compile against the annotations
    compileOnly("io.github.llamalad7:mixinextras-common:$mixinExtras")

    // ship the loader-specific build inside the mod jar
    implementation("io.github.llamalad7:mixinextras-${mod.loader}:$mixinExtras")
    include("io.github.llamalad7:mixinextras-${mod.loader}:$mixinExtras")
}

// Mod publishing (Modrinth / CurseForge)
// Note: no requires("fabric-api") here, Stonecraft already adds it for Fabric
// and declaring it again makes CurseForge reject the upload as a duplicate.
publishMods {
    changelog.set(
        rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText()
            ?: "No changelog provided."
    )

    modrinth {
        environment = CLIENT_ONLY

        requires("shwgconfig")
        if (mod.isFabric) optional("modmenu")
    }

    curseforge {
        client = true
        server = false

        requires("shwgconfig")
        if (mod.isFabric) optional("modmenu")
    }
}

// Java toolchain
java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}
