package xyz.liut.releaseplugin.task

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import xyz.liut.logcat.L
import xyz.liut.releaseplugin.Utils
import xyz.liut.releaseplugin.bean.FileNameTemplateBean

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.function.Consumer

/**
 * release task
 */
class ReleaseTask extends BaseTask {

    /**
     * 待处理的数据
     */
    @Input
    Set<ApplicationVariant> inputVariants

    /**
     * 文件名模板
     */
    @Input
    String fileNameTemplate

    /**
     * 输出路径
     */
    @OutputDirectory
    String outputDir

    /**
     * 所有输出的文件
     */
    @OutputFiles
    Set<FileNameTemplateBean> outputFiles

    @TaskAction
    def release() {
        if (!inputVariants) {
            throw new IllegalArgumentException("variants 为空")
        }
        if (!fileNameTemplate) {
            throw new IllegalArgumentException("fileNameTemplate 为空")
        }
        if (!outputDir) {
            throw new IllegalArgumentException("outputDir 为空")
        }

        Utils.checkDir(outputDir)

        inputVariants.forEach(new Consumer<ApplicationVariant>() {
            @Override
            void accept(ApplicationVariant applicationVariant) {

                def outputs = applicationVariant.outputs

                if (outputs.size() != 1) {
                    throw new IllegalArgumentException("outputs.size = $outputs.size()")
                }

                def output = applicationVariant.outputs[0]

                def outputFile = output.outputFile
                if (!outputFile.exists()) {
                    return
                }
                L.d outputFile

                FileNameTemplateBean templateBean = new FileNameTemplateBean(project, applicationVariant)

                // 根据模板生成文件名
                String finalFileName = templateBean.fileNameTemplate(fileNameTemplate)

                // 判断是否已签名
                if (applicationVariant.signingReady) {
                    finalFileName = finalFileName + ".apk"
                } else {
                    finalFileName = finalFileName + "-unsigned.apk"
                }

                File resultFile = new File(outputDir + File.separator + finalFileName)

                if (resultFile.exists()) resultFile.delete()

                Files.copy(outputFile.toPath(),
                        resultFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING)

                L.d "resultFile $resultFile"

                templateBean.outputFile = resultFile
                outputFiles.add(templateBean)
            }
        })

        success = true

        L.i "-------${name} end, ${outputFiles.size()}---------"
    }

}
