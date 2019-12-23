package xyz.liut.releaseplugin

class ReleaseExtension {

    private static String DEFAULT_FILE_NAME_FORMAT = '$app-$b-$f_$vn.$vc'
    private static String DEFAULT_OUTPUT_DIR = ".${File.separator}output${File.separator}"
    private static String DEFAULT_JIAGU_OUTPUT_DIR = ".${File.separator}output${File.separator}jiagu${File.separator}"

    /**
     * 文件名模板
     *
     * '$app-$b-$f-$vn.$vc'
     */
    private String fileNameTemplate = DEFAULT_FILE_NAME_FORMAT

    /**
     * 加固文件名
     */
    private String jiaguFileNameTemplate

    /**
     * 输出路径
     */
    private String outputPath = DEFAULT_OUTPUT_DIR

    /**
     * 加固输出路径
     */
    private String jiaguOutputPath = DEFAULT_JIAGU_OUTPUT_DIR

    /**
     * 操作完成后， 打开输出目录
     */
    private boolean openDir = false

    String getFileNameTemplate() {
        return fileNameTemplate
    }

    void setFileNameTemplate(String fileNameTemplate) {
        this.fileNameTemplate = fileNameTemplate
    }

    String getJiaguFileNameTemplate() {
        return jiaguFileNameTemplate
    }

    void setJiaguFileNameTemplate(String jiaguFileNameTemplate) {
        this.jiaguFileNameTemplate = jiaguFileNameTemplate
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

    boolean getOpenDir() {
        return openDir
    }

    void setOpenDir(boolean openDir) {
        this.openDir = openDir
    }


}
