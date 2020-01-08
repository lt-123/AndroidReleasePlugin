package xyz.liut.releaseplugin.task

import org.gradle.api.tasks.TaskAction
import xyz.liut.releaseplugin.Utils
import xyz.liut.releaseplugin.bean.FileNameTemplateBean

import java.util.function.Consumer

class JiaguTask extends BaseTask {

    /**
     * 360 加固
     */
    public static final int JIAGU_360 = 360

    /**
     * 使用的加固程序， 目前仅支持 360
     */
    int jiaguProgram = JIAGU_360

    /**
     * 加固程序路径
     */
    String jiaguProgramDir

    /**
     * 360 加固参数
     */
    String jiaguCmdParams

    /**
     * 待加固的 apk
     */
    Set<FileNameTemplateBean> apkFiles

    /**
     * 文件名模板
     */
    String fileNameTemplate

    /**
     * 输出路径
     */
    String outputDir

    @TaskAction
    def jiagu() {
        if (!jiaguProgramDir) {
            throw new IllegalArgumentException("jiaguPath 为空, 请在项目根目录的 local.properties 中配置 jiaguPath")
        }
        if (!apkFiles) {
            throw new IllegalArgumentException("apkFiles 为空")
        }
        if (!outputDir) {
            throw new IllegalArgumentException("outputDir 为空")
        }

        Utils.checkDir(outputDir)

        println "=====开始加固====="

        switch (jiaguProgram) {
            case JIAGU_360:
                apkFiles.forEach(new Consumer<FileNameTemplateBean>() {
                    @Override
                    void accept(FileNameTemplateBean bean) {
                        // 判断是否存在模板
                        if (fileNameTemplate) {
                            // 根据模板生成文件名
                            String finalFileName = bean.fileNameTemplate(fileNameTemplate)
                            // 使用 tmp 文件加固， 加固产生的文件会跟 tmp 文件名一致
                            File tmp = new File(outputDir, "${finalFileName}.apk")
                            if (tmp.exists()) tmp.delete()
                            bean.outputFile.renameTo(tmp)
                            // 加固 tmp
                            jiagu360(tmp, new File(outputDir))

                            // 删除 tmp
                            if (tmp.exists()) tmp.delete()
                        } else {
                            jiagu360(bean.outputFile, new File(outputDir))
                        }
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
