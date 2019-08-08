package xyz.liut.releaseplugin

/**
 * 执行命令并返回执行结果是否成功
 *
 * @param cmd 命令体
 * @return 结果
 */
static boolean execCommand(String cmd) {
    println()
    println "执行Command: $cmd"
    def result = true
    def proc = cmd.execute()
    proc.inputStream.eachLine {
        println it
    }
    proc.errorStream.eachLine {
        System.err.println("ERROR: $it")
        result = false
    }
    proc.waitFor()
    println "执行结果: $result"

    if (!result) {
        throw new Exception("命令执行失败 $cmd")
    }

    println()
    return result
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
        System.err.println("ERROR: $it")
        new RuntimeException("获取系统uname失败").printStackTrace()
    }
    proc.waitFor()

    return uname
}

/**
 * 检查文件夹， 如果不存在则创建， 否则终止程序
 *
 * @param dirPath 待检查的文件夹
 */
static void checkAndDir(String dirPath) {
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


