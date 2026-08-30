import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.coroutines.core)
    api(libs.serialization.json)
    api(libs.okhttp)

    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockwebserver)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Android (API 26+) consumes this jar, so the bytecode target stays at 11 and
// nothing from a newer JDK may leak into the class files.
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.addAll(
            "-Xjdk-release=11",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

mavenPublishing {
    configure(KotlinJvm(javadocJar = JavadocJar.Javadoc(), sourcesJar = true))
    coordinates("com.fopost", "fopost-kotlin", version.toString())
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    pom {
        name = "fopost-kotlin"
        description =
            "Official Kotlin SDK for the FoPost API. Schedule and publish to +30 social platforms from your code."
        url = "https://fopost.com"
        inceptionYear = "2026"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "fopost"
                name = "FoPost"
                organization = "Porter Bridge, LLC"
                organizationUrl = "https://fopost.com"
            }
        }
        scm {
            url = "https://github.com/fopost/fopost-kotlin"
            connection = "scm:git:https://github.com/fopost/fopost-kotlin.git"
            developerConnection = "scm:git:ssh://git@github.com/fopost/fopost-kotlin.git"
        }
        issueManagement {
            system = "GitHub"
            url = "https://github.com/fopost/fopost-kotlin/issues"
        }
    }
}
