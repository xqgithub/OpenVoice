package com.shannon.openvoice.initializer

import android.os.Process
import android.util.Log
import androidx.core.os.TraceCompat
import com.caij.app.startup.Task
import com.caij.app.startup.TaskListener

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.initializer
 * @ClassName:      MonitorTaskListener
 * @Description:     初始化日志
 * @Author:         czhen
 * @CreateDate:     2022/7/20 15:54
 */
class MonitorTaskListener(private val tag: String, private val isLog: Boolean) : TaskListener {
    override fun onWaitRunning(task: Task?) {
    }

    override fun onStart(task: Task) {
        if (isLog) {
            Log.d(
                tag + "-START-" + "mainThread: ${task.isMustRunMainThread}",
                "task start : ${task.taskName}"
            )
        }
        TraceCompat.beginSection(task.taskName)
        if (task.isWaitOnMainThread) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        }
    }

    override fun onFinish(task: Task, dw: Long, df: Long) {
        if (task.isWaitOnMainThread) {
            Process.setThreadPriority(ThreadManager.getInstance().DEFAULT_PRIORITY)
        }
        TraceCompat.endSection()
        if (isLog) {
            Log.d(
                tag + "-END-" + "mainThread:" + task.isMustRunMainThread,
                "task end :" + task.taskName + " wait " + dw + " cost " + df
            )
        }
    }
}