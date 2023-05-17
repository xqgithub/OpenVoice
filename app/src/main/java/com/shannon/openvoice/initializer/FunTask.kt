package com.shannon.openvoice.initializer

import com.caij.app.startup.Task

/**
 *
 * @ClassName:      FunTask
 * @Author:         czhen
 * @CreateDate:     2021/10/27 10:37
 */
abstract class FunTask : Task() {

    override fun dependencies(): MutableList<Class<out Task>> {
        return arrayListOf()
    }

    override fun getTaskName(): String {
        return this::class.java.simpleName
    }
}