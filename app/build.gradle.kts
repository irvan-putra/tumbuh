plugins {
    id("com.android.application")
}

val appVersionName = "0.1.0"
val appVersionCode = 1

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
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
