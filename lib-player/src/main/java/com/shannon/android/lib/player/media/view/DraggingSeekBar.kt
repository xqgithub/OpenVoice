package com.shannon.android.lib.player.media.view

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar

/**
 *
 * @ClassName:      DraggingSeekBar
 * @Description:     java类作用描述
 * @Author:         czhen
 */
class DraggingSeekBar : AppCompatSeekBar, SeekBar.OnSeekBarChangeListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    var isDragging = false
        private set

    private var changeListener: OnSeekBarChangeListener? = null

    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
        this.changeListener = l
        super.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        this.changeListener?.onProgressChanged(seekBar, progress, fromUser)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        isDragging = true
        this.changeListener?.onStartTrackingTouch(seekBar)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        isDragging = false
        this.changeListener?.onStopTrackingTouch(seekBar)
    }

    override fun setMax(max: Int) {
        if (max > 1)
            super.setMax(max)
    }

}