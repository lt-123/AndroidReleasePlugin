package xyz.liut.releaseplugin

class ReleaseExtension {

    private static String DEFAULT_FILE_NAME_FORMAT = '$app-$b-$f_$vn.$vc'
    private static String DEFAULT_OUTPUT_DIR = './output/'
    private static String DEFAULT_JIAGU_OUTPUT_DIR = './output/jiagu/'

    /**
     * 文件名模板
     *
     * '$app-$b-$f-$vn.$vc'
     */
    private String fileNameTemplate = DEFAULT_FILE_NAME_FORMAT

    /**
     * 输出路径
     */
    private String outputPath = DEFAULT_OUTPUT_DIR

    /**
     * 加固输出路径
     */
    private String jiaguOutputPath = DEFAULT_JIAGU_OUTPUT_DIR

    String getFileNameTemplate() {
        return fileNameTemplate
    }

    void setFileNameTemplate(String fileNameTemplate) {
        this.fileNameTemplate = fileNameTemplate
    }

    String getOutputPath() {
        return outputPath
    }

    void setOutputPath(String outputPath) {
        this.outputPath = outputPath
    }

    String getJiaguOutputPath() {
        return jiaguOutputPath
    }

    void setJiaguOutputPath(String jiaguOutputPath) {
        this.jiaguOutputPath = jiaguOutputPath
    }

}
