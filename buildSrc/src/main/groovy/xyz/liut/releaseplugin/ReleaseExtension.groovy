package xyz.liut.releaseplugin

class ReleaseExtension {

    private String outputPath

//    private def fileName = '$app-$b-$f_$vn.$vc'
    private String fileName

    def getOutputPath() {
        return outputPath
    }

    void setOutputPath(outputPath) {
        this.outputPath = outputPath
    }

    def getFileName() {
        return fileName
    }

    void setFileName(fileName) {
        this.fileName = fileName
    }
}
