package com.shannon.android.lib.extended

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 *
 * @ClassName:      InflateBinding
 * @Description:     java类作用描述
 * @Author:         czhen
 */
inline fun <reified T : ViewBinding> Activity.inflate(): Lazy<T> = lazy {
    inflateBinding<T>(layoutInflater).apply { setContentView(root) }
}

inline fun <reified T : ViewBinding> Fragment.inflate(): Lazy<T> = lazy {
    inflateBinding<T>(layoutInflater)
}

inline fun <reified T : ViewBinding> Dialog.inflate(): Lazy<T> = lazy {
    inflateBinding<T>(layoutInflater).apply { setContentView(root) }
}

inline fun <reified T : ViewBinding> RecyclerView.ViewHolder.inflate(context: Context): Lazy<T> =
        lazy {
            inflateBinding<T>(LayoutInflater.from(context))
        }

inline fun <reified T : ViewBinding> RecyclerView.Adapter<out RecyclerView.ViewHolder>.inflate(
        context: Context
): T {
    return inflateBinding<T>(LayoutInflater.from(context))
}

inline fun <reified T : ViewBinding> RecyclerView.Adapter<out RecyclerView.ViewHolder>.inflate(
        parent: ViewGroup
): T {
    return inflateBinding<T>(parent)
}

inline fun <reified T : ViewBinding> inflateBinding(inflater: LayoutInflater): T {
    val inflateMethod = T::class.java.getMethod("inflate", LayoutInflater::class.java)
    return inflateMethod.invoke(null, inflater) as T
}

inline fun <reified T : ViewBinding> inflateBinding(parent: ViewGroup): T {
    val inflateMethod = T::class.java.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
    return inflateMethod.invoke(null, LayoutInflater.from(parent.context), parent, false) as T
}