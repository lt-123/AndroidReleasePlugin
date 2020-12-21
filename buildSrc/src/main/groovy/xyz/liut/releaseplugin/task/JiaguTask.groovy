package xyz.liut.releaseplugin.task

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import xyz.liut.logcat.L
import xyz.liut.releaseplugin.Utils
import xyz.liut.releaseplugin.bean.VariantDataBean

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.function.Consumer

class JiaguTask extends BaseTask {

    /**
     * 360 加固
     */
    public static final int JIAGU_360 = 360

    /**
     * 使用的加固程序， 目前仅支持 360
     */
    @Input
    int jiaguProgram = JIAGU_360

    /**
     * 加固程序路径
     */
    @Input
    String jiaguProgramDir

    /**
     * 360 加固参数
     */
    @Input
    String jiaguCmdParams

    /**
     * 待加固的 apk
     */
    @Input
    List<VariantDataBean> variantDataBeans

    /**
     * 文件名模板
     */
    @Input
    String fileNameTemplate

    /**
     * 工作文件夹
     */
    @Input
    String workDir

    /**
     * 输出路径
     */
    @OutputDirectory
    String outputDir

    @TaskAction
    def jiagu() {
        if (!jiaguProgramDir) {
            throw new IllegalArgumentException("jiaguPath 为空, 请在项目根目录的 local.properties 中配置 jiaguPath")
        }
        if (!variantDataBeans) {
            throw new IllegalArgumentException("输入apk为空")
        }
        if (!outputDir) {
            throw new IllegalArgumentException("outputDir 为空")
        }

        Utils.checkDir(outputDir)

        println "=====开始加固====="

        switch (jiaguProgram) {
            case JIAGU_360:
                variantDataBeans.forEach(new Consumer<VariantDataBean>() {
                    @Override
                    void accept(VariantDataBean variantDataBean) {
                        // 根据模板生成文件名
                        String finalFileName = variantDataBean.metaData.fileNameTemplate(fileNameTemplate)
                        // 使用 tmpFile 文件加固， 加固产生的文件会跟 tmpFile 文件名一致
                        File tmpFile = new File(workDir, "${finalFileName}.apk")
                        File tmpDir = tmpFile.parentFile
                        tmpDir.mkdirs()

                        L.d "apkFile ${variantDataBean.apkFile}"
                        L.d "outputFile $tmpFile"

                        Files.copy(variantDataBean.apkFile.toPath(), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        // 加固 tmp
                        jiagu360(tmpFile, new File(outputDir))

                        // 删除临时文件夹
                        Utils.deleteDir(tmpDir)
                    }
                })
                break
            default:
                throw new IllegalArgumentException("目前仅支持 360")
                break
        }

        success = true

        println "=====加固完成====="

    }

    /**
     * 360 加固
     * @param path 带加固文件
     */
    private def jiagu360(File inputFileName, File outputFileDir) {
        println "360jiagu: \ninputFileName=$inputFileName \noutputFileDir=$outputFileDir"

        String cmd = "java -jar $jiaguProgramDir -jiagu ${inputFileName.absolutePath} ${outputFileDir.absolutePath} ${jiaguCmdParams}"
        Utils.execCommand(cmd)
    }

}
