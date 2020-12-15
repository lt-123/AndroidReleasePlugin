package xyz.liut.releaseplugin.task


import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import xyz.liut.logcat.L
import xyz.liut.releaseplugin.Utils
import xyz.liut.releaseplugin.bean.VariantDataBean

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
    List<VariantDataBean> variantDataBeans

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
    List<File> outputFiles

    @TaskAction
    def release() {
        if (!variantDataBeans) {
            throw new IllegalArgumentException("variants 为空")
        }
        if (!fileNameTemplate) {
            throw new IllegalArgumentException("fileNameTemplate 为空")
        }
        if (!outputDir) {
            throw new IllegalArgumentException("outputDir 为空")
        }

        Utils.checkDir(outputDir)

        variantDataBeans.forEach(new Consumer<VariantDataBean>() {
            @Override
            void accept(VariantDataBean variantDataBean) {

                if (!variantDataBean.apkFile.exists()) {
                    throw new NullPointerException("apk file is not exists !!")
                }

                // 根据模板生成文件名
                String fileName = variantDataBean.metaData.fileNameTemplate(fileNameTemplate)

                // 判断是否已签名
                if (variantDataBean.metaData.signingReady) {
                    fileName = fileName + ".apk"
                } else {
                    fileName = fileName + "-unsigned.apk"
                }

                File outputFile = new File(outputDir + File.separator + fileName)

                L.d "apkFile ${variantDataBean.apkFile}"
                L.d "outputFile $outputFile"

                Files.copy(variantDataBean.apkFile.toPath(),
                        outputFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING)

                outputFiles.add(outputFile)
            }
        })

        success = true
    }

}
