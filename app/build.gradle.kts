plugins {
    id("android-app-convention")
    id("telegram-reporter")
    kotlin("plugin.serialization") version "2.0.20" apply true
}

android {
    defaultConfig {
        applicationId = "ru.tretyackov.todo"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["YANDEX_CLIENT_ID"] = "21931c1f0f4843338a7b362f5fbe60b7"
    }
    buildTypes {
        defaultConfig {
            buildConfigField("String", "SERVER_URL", "\"https://hive.mrdekk.ru/todo/\"")
        }
        register("staging") {
            val testServerPort = 8080
            initWith(getByName("debug"))
            buildConfigField("String", "SERVER_URL", "\"http://127.0.0.1:$testServerPort\"")
            buildConfigField("Integer", "PORT", testServerPort.toString())
        }
    }
    testBuildType = "staging"
}

tgReporter {
    token.set(providers.environmentVariable("TG_TOKEN"))
    chatId.set(providers.environmentVariable("TG_CHAT"))
    validateMaxSizeApkEnabled = false
    analyzeApkEnabled = false
    maxSizeApkInMb = 1
}

dependencies {
    implementation(libs.yandex.auth.sdk)
    implementation(libs.kotlinx.serialization.json)
}