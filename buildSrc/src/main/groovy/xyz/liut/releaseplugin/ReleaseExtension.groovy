package xyz.liut.releaseplugin

class ReleaseExtension {

    private static String DEFAULT_FILE_NAME_FORMAT = '$app-$b-$f_$vn.$vc'
    private static String DEFAULT_OUTPUT_DIR = ".${File.separator}output${File.separator}"
    private static String DEFAULT_JIAGU_OUTPUT_DIR = ".${File.separator}output${File.separator}jiagu${File.separator}"
    private static String JIAGU_360_CMD_PARAMS = "-autosign -automulpkg"

    /**
     * 文件名模板
     *
     * '$app-$b-$f-$vn.$vc'
     */
    private String fileNameTemplate = DEFAULT_FILE_NAME_FORMAT

    /**
     * 加固文件名
     */
    private String jiaguFileNameTemplate = DEFAULT_FILE_NAME_FORMAT

    /**
     * 输出路径
     */
    private String outputPath = DEFAULT_OUTPUT_DIR

    /**
     * 加固输出路径
     */
    private String jiaguOutputPath = DEFAULT_JIAGU_OUTPUT_DIR

    /**
     * 360 加固参数
     */
    private String jiaguCmdParams = JIAGU_360_CMD_PARAMS

    /**
     * 操作完成后， 打开输出目录
     */
    private boolean openDir = false

    private String workDir;

    void setWorkDir(String workDir) {
        this.workDir = workDir
    }

    String getFileNameTemplate() {
        return fileNameTemplate
    }

    String getJiaguFileNameTemplate() {
        return jiaguFileNameTemplate
    }

    String getOutputPath() {
        // 相对路径
        if (outputPath.startsWith(".")) {
            return new File(workDir, outputPath).getPath()
        }
        // 绝对路径
        else {
            return outputPath
        }
    }

    String getJiaguOutputPath() {
        // 相对路径
        if (outputPath.startsWith(".")) {
            return new File(workDir, jiaguOutputPath).getPath()
        }
        // 绝对路径
        else {
            return jiaguOutputPath
        }
    }

    boolean getOpenDir() {
        return openDir
    }

    String getJiaguCmdParams() {
        return jiaguCmdParams
    }


    void setFileNameTemplate(String fileNameTemplate) {
        this.fileNameTemplate = fileNameTemplate
    }

    void setJiaguFileNameTemplate(String jiaguFileNameTemplate) {
        this.jiaguFileNameTemplate = jiaguFileNameTemplate
    }

    void setOutputPath(String outputPath) {
        this.outputPath = outputPath
    }

    void setJiaguOutputPath(String jiaguOutputPath) {
        this.jiaguOutputPath = jiaguOutputPath
    }

    void setOpenDir(boolean openDir) {
        this.openDir = openDir
    }

    void setJiaguCmdParams(String jiaguCmdParams) {
        this.jiaguCmdParams = jiaguCmdParams
    }

}
