import TgExport.AnalyzeApkTask
import TgExport.TelegramReportApkTask
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import java.io.File

abstract class TelegramReporterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val androidComponents =
            project.extensions.findByType(AndroidComponentsExtension::class.java)
                ?: throw GradleException("Android not found")
        val extension = project.extensions.create("tgReporter", TelegramExtension::class)
        val telegramApi = TelegramApi(HttpClient(OkHttp))
        androidComponents.onVariants { variant ->
            val artifacts = variant.artifacts.get(SingleArtifact.APK)
            val variantName = variant.name.capitalize()
            val validateTask = project.tasks.register(
                "validateApkSizeFor${variantName}",
                ValidatorApkSizeTask::class.java,
                telegramApi
            )
            validateTask.configure {
                outputFileApkSize.set(File(temporaryDir, "apk_size.txt"))
                apkDir.set(artifacts)
                token.set(extension.token)
                chatId.set(extension.chatId)
                maxSizeApkInMb.set(extension.maxSizeApkInMb)
            }
            val versionCode =
                project.extensions.findByType(BaseAppModuleExtension::class.java)?.defaultConfig?.versionCode
                    ?: throw GradleException("Android versionCode not found")
            val telegramReportApkTask = project.tasks.register(
                "telegramReportApkFor${variantName}",
                TelegramReportApkTask::class.java,
                telegramApi, variant.name,
                versionCode.toString(),
            )
            telegramReportApkTask.configure {
                apkDir.set(artifacts)
                token.set(extension.token)
                chatId.set(extension.chatId)
            }
            val analyzeApkTask = project.tasks.register(
                "analyzeApkTaskFor${variantName}",
                AnalyzeApkTask::class.java,
                telegramApi
            )
            analyzeApkTask.configure {
                token.set(extension.token)
                chatId.set(extension.chatId)
                apkDir.set(artifacts)
            }
            val analyzeApkEnabled = extension.analyzeApkEnabled.get()
            val validateMaxSizeApkEnabled = extension.validateMaxSizeApkEnabled.get()
            project.tasks.register("telegramReportDetailedFor${variantName}").configure {
                dependsOn(telegramReportApkTask)
                if (validateMaxSizeApkEnabled) {
                    dependsOn(validateTask)
                }
                if (analyzeApkEnabled) {
                    dependsOn(analyzeApkTask)
                }
            }
            if (validateMaxSizeApkEnabled) {
                telegramReportApkTask.configure {
                    apkSizeDescription.set(validateTask.get().outputFileApkSize)
                    mustRunAfter(validateTask)
                }
            }
            if (analyzeApkEnabled) {
                analyzeApkTask.configure {
                    mustRunAfter(telegramReportApkTask)
                }
            }
        }
    }
}

interface TelegramExtension {
    val chatId: Property<String>
    val token: Property<String>
    val validateMaxSizeApkEnabled: Property<Boolean>
    val maxSizeApkInMb: Property<Int>
    val analyzeApkEnabled: Property<Boolean>
}

