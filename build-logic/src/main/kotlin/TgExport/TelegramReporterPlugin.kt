import TgExport.TelegramReporterTask
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
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
            val validateTask = project.tasks.register(
                "validateApkSizeFor${variant.name.capitalize()}",
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
            val reportTask = project.tasks.register(
                "reportTelegramApkFor${variant.name.capitalize()}",
                TelegramReporterTask::class.java,
                telegramApi, variant.name,
                versionCode.toString(),
            )
            if (extension.validateMaxSizeApkEnabled.get()) {
                reportTask.dependsOn(validateTask)
            }
            reportTask.configure {
                apkDir.set(artifacts)
                token.set(extension.token)
                chatId.set(extension.chatId)
                if (extension.validateMaxSizeApkEnabled.get()) {
                    apkSizeDescription.set(validateTask.get().outputFileApkSize)
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
}

