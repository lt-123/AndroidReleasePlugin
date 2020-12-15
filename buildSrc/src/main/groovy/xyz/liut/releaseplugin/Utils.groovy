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
        if (!files && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File f = new File(files[i])
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
 * 执行命令并返回 exitValue
 *
 * @param cmd 命令体
 * @return exitValue
 */
static int execCommand(String cmd) {
    println()
    println "exec:\n$cmd"

    String fileName = "delete_me"
    FileWriter writer = new FileWriter(fileName, false)
    writer.append("#!/usr/bin/env bash").append("\n").append(cmd)
    writer.flush()
    writer.close()

    try {
        def proc = "bash $fileName".execute()
        proc.inputStream.eachLine {
            println it
        }
        proc.errorStream.eachLine {
            printlnError "ERROR: $it"
        }
        proc.waitFor()
        println()

        int value = proc.exitValue()

        new File(fileName).delete()

        if (value != 0) {
            throw new RuntimeException("命令执行失败 exitValue = $value\n$cmd")
        } else {
            return value
        }
    } catch (Exception e) {
        new File(fileName).delete()
        throw new RuntimeException("命令执行失败 ${e.message}\n$cmd", e)
    }

}

/**
 * 执行 windows powershell 命令并返回 exitValue
 *
 * @param cmd 命令体
 * @return exitValue
 */
static def execWindowsPsCommand(String cmd) {
    println()
    println "exec:\n$cmd"

    int value = -1123

    try {
        String[] cmds = new String[3]
        cmds[0] = "powershell"
        cmds[1] = "/c"
        cmds[2] = cmd
        def proc = Runtime.getRuntime().exec(cmds)

        proc.inputStream.eachLine {
            println it
        }
        proc.errorStream.eachLine {
            printlnError "ERROR: $it"
        }
        proc.waitFor()
        println()

        value = proc.exitValue()
        if (value != 0) {
            throw new RuntimeException("命令执行失败 exitValue = $value\n$cmd")
        }
    } catch (Exception e) {
        throw new RuntimeException("命令执行失败 ${e.message}\n$cmd", e)
    }

    return value
}

/**
 * 获取系统 uname
 *
 * @return uanme
 */
static String uname() {
    def proc = "uname -s".execute()

    String uname = "none"

    proc.inputStream.eachLine {
        // 结果只有一行
        if (it != null)
            uname = it
        else
            println "获取系统uname 为空"
    }
    proc.errorStream.eachLine {
        printlnError "ERROR: $it"
    }
    proc.waitFor()

    if (proc.exitValue() != 0) {
        new RuntimeException("获取系统uname失败").printStackTrace()
    }

    return uname
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
            execCommand("open $path")
        }
        // win
        else if (osName.contains("windows")) {
            execWindowsPsCommand("explorer $path")
        }
        // linux
        else if (osName.contains("linux")) {
            if (new File("/usr/bin/nautilus").exists()) {
                execCommand("nohup nautilus $path &")
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

// ===========================================

static void println() {
    L.i ""
}

static void println(obj) {
    L.i obj
}

static void printlnError(obj) {
    L.e obj
}