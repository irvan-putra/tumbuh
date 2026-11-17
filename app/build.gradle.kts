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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}
