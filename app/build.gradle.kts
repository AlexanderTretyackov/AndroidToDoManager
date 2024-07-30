plugins {
    id("android-app-convention")
    id("telegram-reporter")
}

android {
    defaultConfig {
        applicationId = "ru.tretyackov.todo"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    maxSizeApkInMb = 1
}