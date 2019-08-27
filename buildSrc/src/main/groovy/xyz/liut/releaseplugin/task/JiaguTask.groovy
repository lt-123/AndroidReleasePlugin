package xyz.liut.releaseplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import xyz.liut.releaseplugin.Utils

class JiaguTask extends DefaultTask {

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
    File jiaguProgramDir

    /**
     * 待加固的 apk
     */
    @Input
    File apkFile

    /**
     * 输出路径
     */
    @OutputDirectory
    File outputDir

    @TaskAction
    def jiagu() {
        println "=====开始加固====="

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        switch (jiaguProgram) {
            case JIAGU_360:
                jiagu360(apkFile, outputDir)
                break
            default:
                throw new IllegalArgumentException("目前仅支持 360")
                break
        }

        println "=====加固完成====="

    }

    /**
     * 360 加固
     * @param path 带加固文件
     */
    private def jiagu360(File inputFileName, File outputFileDir) {
        println "360jiagu: \ninputFileName=$inputFileName \noutputFileDir=$outputFileDir"

        Utils.execCommand(" java -jar  $jiaguProgramDir  -jiagu  ${inputFileName.absolutePath} ${outputFileDir.absolutePath} -autosign -automulpkg")
    }

}
