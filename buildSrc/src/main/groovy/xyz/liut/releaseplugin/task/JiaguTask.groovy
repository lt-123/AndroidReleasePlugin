package xyz.liut.releaseplugin.task

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task

class JiaguTask extends DefaultTask {


    @Override
    Task doLast(Action<? super Task> action) {
        return super.doLast(action)
    }

    @Override
    Task doLast(Closure action) {

        return super.doLast(action)
    }

}
