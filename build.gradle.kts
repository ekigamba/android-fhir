// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  dependencies {
    classpath(Plugins.androidGradlePlugin)
    classpath(Plugins.kotlinGradlePlugin)
    classpath(Plugins.navSafeArgsGradlePlugin)
  }
}

plugins {
  id("com.vanniktech.android.junit.jacoco") version "0.16.0"
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "org.smartregister"
version = "-SNAPSHOT"

nexusPublishing {


  repositories {
    sonatype()  //sonatypeUsername and sonatypePassword properties are used automatically

  }

  // these are not strictly required. The default timeouts are set to 1 minute. But Sonatype can be really slow.
  // If you get the error "java.net.SocketTimeoutException: timeout", these lines will help.
  //connectTimeout = Duration.ofMinutes(3)
  //clientTimeout = Duration.ofMinutes(3)
}

allprojects {
  repositories {
    google()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    gradlePluginPortal()
  }
  configureSpotless()
}

subprojects { configureLicensee() }

// Create a CI repository and also change versions to include the build number
afterEvaluate {
  val buildNumber = System.getenv("GITHUB_RUN_ID")
  if (buildNumber != null) {
    subprojects {
      apply(plugin = Plugins.BuildPlugins.mavenPublish)
      configure<PublishingExtension> {
        repositories {
          maven {
            name = "CI"
            url = uri("file://${rootProject.buildDir}/ci-repo")
          }
        }
        // update version to have suffix of build id
        project.version = "${project.version}-build_$buildNumber"
      }
    }
  }
}
