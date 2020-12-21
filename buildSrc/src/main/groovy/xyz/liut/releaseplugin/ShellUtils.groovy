package xyz.liut.releaseplugin

import xyz.liut.logcat.L


/**
 * 执行命令并返回 exitValue
 *
 * mac
 *
 * @param cmd 命令体
 * @return exitValue
 */
static def execCmd(String cmd) {
    println()
    println "exec:\n$cmd"

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

        int value = proc.exitValue()
        if (value == 0) {
            return value
        } else {
            throw new RuntimeException("命令执行失败 exitValue = $value\n$cmd")
        }
    } catch (Exception e) {
        throw new RuntimeException("命令执行失败 ${e.message}\n$cmd", e)
    }
}

/**
 * 执行命令并返回 exitValue
 *
 * @param cmd 命令体
 * @return exitValue
 */
static int execCmdByFile(String cmd) {
    println()
    println "exec:\n$cmd"

    String fileName = "/tmp/delete_me"
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
static def execWindowsPsCmd(String cmd) {
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
