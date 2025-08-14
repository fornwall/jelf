plugins {
  id("signing")
  id("maven-publish")
  id("java-library")
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
  id("com.adarshr.test-logger") version "4.0.0"
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
}

java {
  group = "net.fornwall"
  version = "0.10.0"
  sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
  withJavadocJar()
  withSourcesJar()
}

tasks {
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
