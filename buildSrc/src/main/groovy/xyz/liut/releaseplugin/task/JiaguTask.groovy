package xyz.liut.releaseplugin.task


import org.gradle.api.tasks.TaskAction
import xyz.liut.releaseplugin.Utils

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
     * 待加固的 apk
     */
    Set<File> apkFiles

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
                apkFiles.forEach(new Consumer<File>() {
                    @Override
                    void accept(File file) {
                        jiagu360(file, new File(outputDir))
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

        String cmd = "java -jar $jiaguProgramDir -jiagu ${inputFileName.absolutePath} ${outputFileDir.absolutePath} -autosign -automulpkg"
        Utils.execCommand(cmd)
    }

}
