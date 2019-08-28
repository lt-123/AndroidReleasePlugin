package xyz.liut.releaseplugin

import xyz.liut.logcat.L

/**
 * 执行命令并返回 exitValue
 *
 * @param cmd 命令体
 * @return exitValue
 */
static def execCommand(String cmd) {
    println()
    println "执行Command:\n$cmd"

    int value = -1123

    try {
        def proc = cmd.execute()
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
 * 执行命令并返回 exitValue
 *
 * @param cmd 命令体
 * @return exitValue
 */
static def execWindowsCmdCommand(String cmd) {
    println()
    println "执行Command:\n$cmd"

    int value = -1123

    try {
        String[] cmds = new String[3]
        cmds[0] = "cmd"
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
    String uname = uname()

    path = new File(path).toString()

    println "openPath uname = $uname\npath = $path"

    try {
        // mac
        if (uname == "Darwin") {
            execCommand("open $path")
        }
        // win
        else if (uname.toUpperCase().contains("MINGW")) {
            execWindowsCmdCommand("explorer $path")
        }
        // linux 系统的文件管理器不统一， 判断起来很麻烦， 暂不处理
        else {
            printlnError "当前系统未适配打开文件夹功能"
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