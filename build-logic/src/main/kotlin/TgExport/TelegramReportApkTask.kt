package TgExport

import TelegramApi
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class TelegramReportApkTask @Inject constructor(
    private val telegramApi: TelegramApi, private val variantName: String,
    private val versionCode: String
) : DefaultTask() {

    @get:InputDirectory
    abstract val apkDir: DirectoryProperty

    @get:Input
    abstract val token: Property<String>

    @get:Input
    abstract val chatId: Property<String>

    @get:Input
    @get:Optional
    abstract val apkSizeDescription: Property<RegularFileProperty>

    @TaskAction
    fun report() {
        val token = token.get()
        val chatId = chatId.get()
        apkDir.get().asFile.listFiles()
            ?.firstOrNull { it.name.endsWith(".apk") }
            ?.let { file ->
                val apkSizeInfo =
                    if (apkSizeDescription.isPresent) " ${
                        apkSizeDescription.get().asFile.get().readText()
                    }" else ""
                val customFileName = "todolist-$variantName-$versionCode.apk"
                runBlocking {
                    telegramApi.sendMessage("Build finished.$apkSizeInfo", token, chatId).apply {
                        println("Status = $status")
                    }
                    telegramApi.upload(file, customFileName, token, chatId).apply {
                        println("Status = $status")
                    }
                }
            }
    }
}