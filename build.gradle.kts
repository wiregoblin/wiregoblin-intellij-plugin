import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.gradle.api.tasks.SourceSetContainer

plugins {
    kotlin("jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.13.1"
}

group = property("pluginGroup") as String
version = property("pluginVersion") as String

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.12.2")

    intellijPlatform {
        goland(property("platformVersion") as String)
        bundledPlugin("org.jetbrains.plugins.yaml")
        bundledPlugin("org.jetbrains.plugins.go")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "261"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

val mainSourceSet = the<SourceSetContainer>()["main"]

tasks {
    register<JavaExec>("generateWireGoblinSchema") {
        group = "code generation"
        description = "Generate src/main/resources/schema/wiregoblin.schema.json from Kotlin specs."
        dependsOn("compileKotlin")
        classpath = files(mainSourceSet.output.classesDirs, mainSourceSet.compileClasspath)
        mainClass.set("io.wiregoblin.intellij.WireGoblinSchemaGeneratorCliKt")
    }

    register<JavaExec>("checkGeneratedWireGoblinSchema") {
        group = "verification"
        description = "Verify the embedded JSON schema matches generated output."
        dependsOn("compileKotlin")
        classpath = files(mainSourceSet.output.classesDirs, mainSourceSet.compileClasspath)
        mainClass.set("io.wiregoblin.intellij.WireGoblinSchemaGeneratorCliKt")
        args("--check")
    }

    test {
        useJUnitPlatform()
        dependsOn("checkGeneratedWireGoblinSchema")
    }

    processResources {
        dependsOn("checkGeneratedWireGoblinSchema")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }
}
