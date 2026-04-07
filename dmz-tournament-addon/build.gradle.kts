import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

plugins {
    java
    idea
    id("net.minecraftforge.gradle") version "6.0.30"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("org.spongepowered.mixin") version "0.7.38"
}

fun requiredProp(name: String): String = providers.gradleProperty(name).orNull
    ?: error("Missing Gradle property '$name'. Add it to gradle.properties or pass -P$name=...")

val modVersion = requiredProp("mod_version")
val modGroupId = requiredProp("mod_group_id")
val modId = requiredProp("mod_id")

version = modVersion
group = modGroupId

base {
    archivesName.set(modId)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

extensions.configure<org.spongepowered.asm.gradle.plugins.MixinExtension>("mixin") {
    add(sourceSets.main.get(), "dmztournament.refmap.json")
    config("dmztournament.mixins.json")
}

repositories {
    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
    maven {
        name = "Jared's maven"
        url = uri("https://maven.blamejared.com/")
    }
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    maven {
        name = "ModMaven"
        url = uri("https://modmaven.dev")
    }
    mavenCentral()
}

val minecraftVersion = requiredProp("minecraft_version")
val forgeVersion = requiredProp("forge_version")
val mappingChannelProp = requiredProp("mapping_channel")
val mappingVersionProp = requiredProp("mapping_version")
val jeiVersion = requiredProp("jei_version")
val dragonminezVersion = requiredProp("dragonminez_version")

minecraft {
    mappings(mappingChannelProp, mappingVersionProp)
    copyIdeResources.set(true)
    jarJar { enable() }
    
    runs {
        configureEach {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("fml.earlyprogresswindow", "false")
            property("geckolib.disable_examples", "true")
            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("client") {
            // Client configuration
        }
        create("server") {
            // Server configuration
        }
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    annotationProcessor("org.spongepowered:mixin:0.8.7:processor")
    
    // GeckoLib & TerraBlender
    implementation(fg.deobf("software.bernie.geckolib:geckolib-forge-1.20.1:4.8.3"))
    implementation("com.eliotlash.mclib:mclib:20")
    implementation(fg.deobf("com.github.glitchfiend:TerraBlender-forge:1.20.1-3.0.1.10"))
    
    // DragonMineZ - Main dependency
    compileOnly(fg.deobf("curse.maven:dragonminez-1136088:7798127"))
    
    // Dev utility mods
    compileOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion"))
    compileOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion"))
    runtimeOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion"))
}

sourceSets.main {
    resources.srcDir("src/generated/resources/")
}

val minecraftVersionRange = requiredProp("minecraft_version_range")
val forgeVersionRange = requiredProp("forge_version_range")
val loaderVersionRange = requiredProp("loader_version_range")
val modName = requiredProp("mod_name")
val modLicense = requiredProp("mod_license")
val modAuthors = requiredProp("mod_authors")
val modDescription = requiredProp("mod_description")
val geckolibVersionRange = requiredProp("geckolib_version_range")
val terrablenderVersionRange = requiredProp("terrablender_version_range")

tasks.named<ProcessResources>("processResources").configure {
    filteringCharset = "UTF-8"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    val replaceProperties = mapOf(
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "forge_version" to forgeVersion,
        "forge_version_range" to forgeVersionRange,
        "loader_version_range" to loaderVersionRange,
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to modVersion,
        "mod_authors" to modAuthors,
        "mod_description" to modDescription,
        "mod_license" to modLicense,
        "geckolib_version_range" to geckolibVersionRange,
        "terrablender_version_range" to terrablenderVersionRange
    )
    
    inputs.properties(replaceProperties)
    
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("slim")
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modId,
                "Specification-Vendor" to modAuthors,
                "Specification-Version" to "1",
                "Implementation-Title" to modId,
                "Implementation-Version" to modVersion,
                "Implementation-Vendor" to modAuthors,
                "MixinConfigs" to "${modId}.mixins.json"
            )
        )
    }
}
