package TgExport

import TelegramApi
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import sizeInMb
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

private const val BYTES_IN_MB = 1024 * 1024
private const val BYTES_IN_KB = 1024

private fun Float.format(digits: Int) = "%.${digits}f".format(this)

private class FileOrDirInfo(
    private val name: String,
    private val sizeInBytes: Long,
    val sizePercent: Float
) {
    fun getInfoString(): String {
        val formattedSize = formatSize(sizeInBytes)
        val formattedPercent = sizePercent.format(1)
        return "- $name $formattedSize $formattedPercent %"
    }

    private fun formatSize(sizeInBytes: Long): String {
        val sizeInMb = sizeInBytes.toFloat() / BYTES_IN_MB
        if (sizeInMb >= 0.1f)
            return "${sizeInMb.format(1)} Mb"
        val sizeInKb = sizeInBytes.toFloat() / BYTES_IN_KB
        if (sizeInKb >= 0.1f)
            return "${sizeInKb.format(1)} Kb"
        return "$sizeInBytes b"
    }
}

abstract class AnalyzeApkTask @Inject constructor(
    private val telegramApi: TelegramApi
) : DefaultTask() {
    @get:InputDirectory
    abstract val apkDir: DirectoryProperty

    @get:Input
    abstract val token: Property<String>

    @get:Input
    abstract val chatId: Property<String>

    @TaskAction
    fun analyze() {
        val token = token.get()
        val chatId = chatId.get()
        val apkFile = apkDir.get().asFile.listFiles()
            ?.firstOrNull { it.name.endsWith(".apk") }!!
        val analyzeResult = analyze(apkFile)
        val analyzeString = buildAnalyzeString(analyzeResult)
        runBlocking {
            sendAnalyze(token, chatId, analyzeString)
        }
    }

    private suspend fun sendAnalyze(token: String, chatId: String, analyze: String) {
        telegramApi.sendMessage(analyze, token, chatId)
            .apply {
                println("Status = $status")
            }
    }

    private fun analyze(apkFile: File): List<FileOrDirInfo> {
        val mapFileOrDirToCompressedSize = HashMap<String, Long>()
        ZipFile(apkFile.path).use { zip ->
            zip.entries().asSequence()
                .forEach { entry ->
                    val indexOfSlash = entry.name.indexOf('/')
                    if (indexOfSlash == -1)
                        mapFileOrDirToCompressedSize[entry.name] = entry.compressedSize
                    else {
                        val dirName = entry.name.substring(0, indexOfSlash)
                        mapFileOrDirToCompressedSize[dirName] =
                            (mapFileOrDirToCompressedSize[dirName] ?: 0) + entry.compressedSize
                    }
                }
        }
        val apkFileSizeInBytes = apkFile.sizeInMb() * BYTES_IN_MB
        val fileAndDirInfoList = mapFileOrDirToCompressedSize.map { (name, size) ->
            val percent = size / apkFileSizeInBytes * 100
            FileOrDirInfo(name, size, percent)
        }.sortedByDescending { it.sizePercent }
        return fileAndDirInfoList
    }

    private fun buildAnalyzeString(fileAndDirInfoList: List<FileOrDirInfo>): String {
        return fileAndDirInfoList.joinToString(separator = "\n") {
            it.getInfoString()
        }
    }
}
