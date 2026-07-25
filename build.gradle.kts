plugins {
  id("java-library")
  id("com.vanniktech.maven.publish") version "0.37.0"
  id("com.adarshr.test-logger") version "4.0.0"
  id("com.diffplug.spotless") version "8.8.0"
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:6.1.2")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
  group = "net.fornwall"
  version = "0.12.0"
  sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
}

spotless {
  java {
    palantirJavaFormat()
  }
}

tasks {
  withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:all")
  }
  withType<Javadoc>().configureEach {
    // Report javadoc problems, but do not require documenting every public member.
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
  }
  test {
    systemProperty("jelf.version", project.version.toString())
    useJUnitPlatform()
  }
}

// See https://vanniktech.github.io/gradle-maven-publish-plugin/central/
/*
 Export the signing key once, as gpg 2.1+ no longer keeps a secring.gpg around:

   gpg --export-secret-keys > ~/.gnupg/secring.gpg

 Then put the credentials in ~/.gradle/gradle.properties, never in this repository:

   mavenCentralUsername=xxx           # user token from https://central.sonatype.com/usertoken
   mavenCentralPassword=xxx
   signing.keyId=xxx                  # last 8 characters of the key id
   signing.password=xxx
   signing.secretKeyRingFile=/home/user/.gnupg/secring.gpg

 and release with:

   ./gradlew publishAndReleaseToMavenCentral
*/
mavenPublishing {
  publishToMavenCentral(automaticRelease = true)
  signAllPublications()

  coordinates(group.toString(), "jelf", version.toString())

  pom {
    name.set("JElf")
    description.set("ELF parsing library in java")
    url.set("https://github.com/fornwall/jelf")
    licenses {
      license {
        name.set("The MIT License")
        url.set("https://opensource.org/licenses/MIT")
      }
    }
    developers {
      developer {
        id.set("fornwall")
        name.set("Fredrik Fornwall")
        email.set("fredrik@fornwall.net")
      }
    }
    scm {
      connection.set("scm:git://github.com/fornwall/jelf.git")
      developerConnection.set("scm:git:ssh://git@github.com/fornwall/jelf.git")
      url.set("https://github.com/fornwall/jelf/")
    }
  }
}

// Do not sign when only installing into the local maven repository. The decision is stored in a
// property computed when the task graph is ready, so that the onlyIf predicate does not capture the
// Project instance, which the configuration cache does not allow.
// The enclosing run block keeps signingRequired a local variable: a top level val would be a field
// of the script object, which the onlyIf lambda below would then capture.
run {
  val signingRequired = objects.property(Boolean::class.java).convention(true)

  gradle.taskGraph.whenReady {
    signingRequired.set(allTasks.none { it is org.gradle.api.publish.maven.tasks.PublishToMavenLocal })
  }

  tasks.withType<Sign>().configureEach {
    onlyIf { signingRequired.get() }
  }
}
