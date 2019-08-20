package xyz.liut.releaseplugin

class ReleaseExtension {

    private def outputPath

//    private def fileName = '$app-$b-$f_$vn.$vc'
    private def fileName

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
