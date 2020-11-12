package xyz.liut.releaseplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input

class BaseTask extends DefaultTask {

    /**
     * 是否成功
     */
    @Input
    boolean success = false

}
