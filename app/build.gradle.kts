plugins {
    id("com.android.application")
}

val appVersionName = "0.2.1"
val appVersionCode = 4

base {
    archivesName.set("tumbuh-v$appVersionName")
}

android {
    namespace = "com.vanipy.tumbuh"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vanipy.tumbuh"
        minSdk = 26
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        create("release") {
            val ks = System.getenv("TUMBUH_KEYSTORE_FILE")
            if (ks != null) {
                storeFile = file(ks)
                storePassword = System.getenv("TUMBUH_STORE_PASSWORD")
                keyAlias = System.getenv("TUMBUH_KEY_ALIAS")
                keyPassword = System.getenv("TUMBUH_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            if (System.getenv("TUMBUH_KEYSTORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.systemProperty("robolectric.graphicsMode", "NATIVE") }
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1") // test-only, not in the APK
    testImplementation("org.json:json:20240303")
}
