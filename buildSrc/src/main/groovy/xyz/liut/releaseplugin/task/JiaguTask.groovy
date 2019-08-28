package xyz.liut.releaseplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import xyz.liut.releaseplugin.Utils

import java.util.function.Consumer

class JiaguTask extends DefaultTask {

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
    File jiaguProgramDir

    /**
     * 待加固的 apk
     */
    Set<File> apkFiles

    /**
     * 输出路径
     */
    File outputDir

    @TaskAction
    def jiagu() {
        if (!jiaguProgramDir) {
            throw new IllegalArgumentException("jiaguProgramDir 为空")
        }
        if (!apkFiles) {
            throw new IllegalArgumentException("apkFiles 为空")
        }
        if (!outputDir) {
            throw new IllegalArgumentException("outputDir 为空")
        }

        println "=====开始加固====="

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        switch (jiaguProgram) {
            case JIAGU_360:
                apkFiles.forEach(new Consumer<File>() {
                    @Override
                    void accept(File file) {
                        jiagu360(file, outputDir)
                    }
                })
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
