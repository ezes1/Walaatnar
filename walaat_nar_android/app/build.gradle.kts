plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlinOptions {
    jvmTarget = "21"
}
    namespace = "com.walaatnar.inventory"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.walaatnar.inventory"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
