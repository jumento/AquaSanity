import java.net.URL
plugins {
    id("java")
}

group = "mx.jume.aquasanity"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(fileTree("libs") { include("*.jar") })
    compileOnly("com.google.code.gson:gson:2.10.1")
    // MMOSkillTree integration (optional — plugin must be present at runtime)
    compileOnly(fileTree("libs/optional") { include("*.jar") })
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


// Generate Manifest Task based on BOT.md constraints
val generateManifest by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/resources")
    outputs.dir(outputDir)
    
    inputs.property("group", project.group)
    inputs.property("name", rootProject.name)
    inputs.property("version", project.version)

    doLast {
        var activeServerVersion = "2026.02.18-f3b8fff95" // Fallback seguro
        try {
            val xmlText = URL("https://maven.hytale.com/release/com/hypixel/hytale/Server/maven-metadata.xml").readText()
            val regex = "<release>(.*?)</release>".toRegex()
            val match = regex.find(xmlText)
            if (match != null) {
                activeServerVersion = match.groupValues[1]
            }
        } catch (e: Exception) {
            println("Advertencia: No se pudo conectar al maven oficial de Hytale. Se usa la versión base.")
        }

        val json = """
            {
              "Group": "${project.group}",
              "Name": "AquaSanity",
              "Version": "${project.version}",
              "ServerVersion": "$activeServerVersion",
              "Main": "mx.jume.aquasanity.AquaSanity",
              "Authors": [
                  { "Name": "jume" }
              ],
              "IncludesAssetPack": true,
              "OptionalDependencies": {
                "ziggfreed:MMOSkillTree": "*",
                "Zuxaw:RPGLeveling": "*"
              }
            }
        """.trimIndent()

        outputDir.get().file("manifest.json").asFile.apply {
            parentFile.mkdirs()
            writeText(json)
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir(generateManifest)
        }
    }
}


tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to rootProject.name,
            "Implementation-Version" to project.version,
            "Main-Class" to "${project.group}.AquaSanity"
        )
    }
}
