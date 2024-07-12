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
}

tgReporter {
    token.set(providers.environmentVariable("TG_TOKEN"))
    chatId.set(providers.environmentVariable("TG_CHAT"))
    validateMaxSizeApkEnabled = false
    maxSizeApkInMb = 1
}