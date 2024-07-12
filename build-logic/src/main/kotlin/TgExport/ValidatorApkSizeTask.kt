import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class ValidatorApkSizeTask @Inject constructor(
    private val telegramApi: TelegramApi
) : DefaultTask() {

    @get:InputDirectory
    abstract val apkDir: DirectoryProperty

    @get:Input
    abstract val token: Property<String>

    @get:Input
    abstract val chatId: Property<String>

    @get:Input
    abstract val maxSizeApkInMb: Property<Int>

    @get:OutputFile
    abstract val outputFileApkSize: RegularFileProperty

    @TaskAction
    fun report() {
        outputFileApkSize.get().asFile.writeText("")
        val token = token.get()
        val chatId = chatId.get()
        apkDir.get().asFile.listFiles()
            ?.filter { it.name.endsWith(".apk") }
            ?.forEach { file ->
                runBlocking {
                    val fileSizeInMb = file.sizeInMb()
                    val sizeFormatted = formatSizeInMb(fileSizeInMb)
                    if (fileSizeInMb > maxSizeApkInMb.get()) {
                        val msg = "Apk file size is too big: $sizeFormatted when limit is ${
                            formatSizeInMb(
                                maxSizeApkInMb.get().toFloat()
                            )
                        }"
                        telegramApi.sendMessage(msg, token, chatId)
                            .apply {
                                println("Status = $status")
                            }
                        throw Exception(msg)
                    }
                    outputFileApkSize.get().asFile.writeText("Apk file size is $sizeFormatted")
                }
            }
    }
}