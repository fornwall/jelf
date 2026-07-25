plugins {
  id("signing")
  id("maven-publish")
  id("java-library")
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
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
  withJavadocJar()
  withSourcesJar()
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

// See https://docs.gradle.org/current/userguide/publishing_maven.html
// and https://github.com/gradle-nexus/publish-plugin
/*
 gpg --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg

 gradle --info \
   -PsonatypeUsername=xxx \
   -PsonatypePassword=xxx \
   -Psigning.keyId=xxx \
   -Psigning.password=xxx \
   -Psigning.secretKeyRingFile=$HOME/.gnupg/secring.gpg \
   publishToSonatype \
   closeAndReleaseSonatypeStagingRepository
*/
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      //artifactId = 'jelf'
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
  }
}

signing {
  sign(publishing.publications["mavenJava"])
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
