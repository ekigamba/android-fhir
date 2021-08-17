plugins {
  id(Plugins.BuildPlugins.androidLib)
  id(Plugins.BuildPlugins.kotlinAndroid)
  id(Plugins.BuildPlugins.mavenPublish)
}

afterEvaluate {
  publishing {
    publications {
      register("release", MavenPublication::class) {
        from(components["release"])
        artifactId = "datacapture"
        groupId = "com.google.android.fhir"
        version = "0.1.0.1-alpha03"
        // Also publish source code for developers' convenience
        artifact(
          tasks.create<Jar>("androidSourcesJar") {
            archiveClassifier.set("sources")
            from(android.sourceSets.getByName("main").java.srcDirs)
          }
        )
        pom {
          name.set("Android FHIR Structured Data Capture Library")
          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
          }
        }
      }
    }
  }
}

android {
  compileSdkVersion(Sdk.compileSdk)
  buildToolsVersion(Plugins.Versions.buildTools)

  defaultConfig {
    minSdkVersion(Sdk.minSdk)
    targetSdkVersion(Sdk.targetSdk)
    testInstrumentationRunner(Dependencies.androidJunitRunner)
    // Need to specify this to prevent junit runner from going deep into our dependencies
    testInstrumentationRunnerArguments(mapOf("package" to "com.google.android.fhir.datacapture"))
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
  }
  compileOptions {
    // Flag to enable support for the new language APIs
    // See https://developer.android.com/studio/write/java8-support
    isCoreLibraryDesugaringEnabled = true
    // Sets Java compatibility to Java 8
    // See https://developer.android.com/studio/write/java8-support
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    // See https://developer.android.com/studio/write/java8-support
    jvmTarget = JavaVersion.VERSION_1_8.toString()
  }
  testOptions { unitTests.isIncludeAndroidResources = true }
}

configurations { all { exclude(module = "xpp3") } }

dependencies {
  androidTestImplementation(Dependencies.AndroidxTest.core)
  androidTestImplementation(Dependencies.AndroidxTest.extJunit)
  androidTestImplementation(Dependencies.AndroidxTest.extJunitKtx)
  androidTestImplementation(Dependencies.AndroidxTest.rules)
  androidTestImplementation(Dependencies.AndroidxTest.runner)
  androidTestImplementation(Dependencies.truth)

  api(Dependencies.HapiFhir.structuresR4)

  coreLibraryDesugaring(Dependencies.desugarJdkLibs)

  implementation(Dependencies.Androidx.appCompat)
  implementation(Dependencies.Androidx.fragmentKtx)
  implementation(Dependencies.apacheCommonsCompress)
  implementation(Dependencies.apacheCommonsIo)
  implementation(Dependencies.HapiFhir.validation) {
    exclude(module = "commons-logging")
    exclude(module = "httpclient")
  }
  implementation(Dependencies.Kotlin.androidxCoreKtx)
  implementation(Dependencies.Kotlin.kotlinTestJunit)
  implementation(Dependencies.Kotlin.stdlib)
  implementation(Dependencies.Lifecycle.viewModelKtx)
  implementation(Dependencies.material)
  implementation(Dependencies.flexBox)

  testImplementation(Dependencies.AndroidxTest.core)
  testImplementation(Dependencies.junit)
  testImplementation(Dependencies.mockitoKotlin)
  testImplementation(Dependencies.robolectric)
  testImplementation(Dependencies.truth)
}

/**Create github.properties in root project folder file with gpr.usr=GITHUB_USER_ID  & gpr.key=PERSONAL_ACCESS_TOKEN**/
//val githubProperties = Properties()
//githubProperties.load(FileInputStream(rootProject.file("github.properties")))

tasks {
  val sourcesJar by creating(Jar::class) {
    // dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    classifier = "sources"
    from(android.sourceSets["main"].java.srcDirs)
  }

  artifacts { add("archives", sourcesJar) }
}

fun getVersionName(): String {
  return "0.1.0.1-alpha03" // Replace with version Name
}

fun getArtificatId(): String {
  return "datacapture" // Replace with library name ID
}

publishing {
  publications {
    create<MavenPublication>("datacapture") {
      run {
        groupId = "com.google.android.fhir"
        artifactId = getArtificatId()
        version = getVersionName()
        artifact("$buildDir/outputs/aar/${getArtificatId()}-release.aar")
      }
    }
  }

  repositories {
    maven {
      name = "GitHubPackages"
      /** Configure path of your package repository on Github
       *  Replace GITHUB_USERID with your/organisation Github userID and REPOSITORY with the repository name on GitHub
       */
      url = uri("https://maven.pkg.github.com/google/android-fhir")
      credentials {
        /**Create github.properties in root project folder file with gpr.usr=GITHUB_USER_ID  & gpr.key=PERSONAL_ACCESS_TOKEN
         * OR
         * Set environment variables
         */
        username = "ekigamba"
        password = "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"

      }
    }
  }
}
