package xyz.liut.releaseplugin

import xyz.liut.logcat.L


/**
 * 删除文件夹
 *
 * @param dir 文件夹
 */
static void deleteDir(File dir) {
    if (dir == null) return
    if (dir.exists()) {
        String[] files = dir.list()
        if (files && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File f = new File(dir, files[i])
                if (f.isDirectory()) {
                    deleteDir(f)
                } else {
                    f.delete()
                }
            }
        }
        dir.delete()
    }
}

/**
 * 检查文件夹， 如果不存在则创建， 否则终止程序
 *
 * @param dirPath 待检查的文件夹
 */
static void checkDir(String dirPath) {
    File file = new File(dirPath)
    if (!file.exists()) {
        boolean res = file.mkdirs()
        if (!res) {
            throw new IllegalAccessError("无法创建文件夹 >> $dirPath")
        }
    } else if (file.isFile()) {
        throw new IllegalStateException("请先删除 ${file.absoluteFile}")
    }

}

/**
 * 执行命令并返回 exitValue
 *
 * @param cmd 命令体
 * @return exitValue
 */
static int execCommand(String cmd) {

    String osName = System.properties.get("os.name")
    L.i osName
    if (!osName) {
        printlnError "未知操作系统"
        osName = ""
    }

    osName = osName.toLowerCase()

    try {
        // mac
        if (osName.contains("mac")) {
            return ShellUtils.execCmd(cmd)
        }
        // win
        else if (osName.contains("windows")) {
            return ShellUtils.execWindowsPsCmd(cmd)
        }
        // linux
        else if (osName.contains("linux")) {
            return ShellUtils.execCmdByFile(cmd)
        }
        // Other System， 暂不处理
        else {
            printlnError "unknow system: $osName"
            return ShellUtils.execCmd(cmd)
        }
    } catch (Exception ignored) {
    }

    return -1
}

/**
 * 打开文件夹/文件
 *
 * @param path 文件/文件夹路径
 */
static void openPath(String path) {
    String osName = System.properties.get("os.name")
    if (!osName) {
        printlnError "未知操作系统"
    }

    osName = osName.toLowerCase()

    try {
        // mac
        if (osName.contains("mac")) {
            ShellUtils.execCmd("open $path")
        }
        // win
        else if (osName.contains("windows")) {
            ShellUtils.execWindowsPsCmd("explorer $path")
        }
        // linux
        else if (osName.contains("linux")) {
            if (new File("/usr/bin/nautilus").exists()) {
                ShellUtils.execCmdByFile("nohup nautilus $path &")
            }
            // Other Desktop, 暂不处理
            else {
                printlnError "On linux, only support nautilus."
            }
        }
        // Other System， 暂不处理
        else {
            printlnError "The current system is not compatible with the open folder function: $osName"
        }
    } catch (Exception ignored) {
    }

}
