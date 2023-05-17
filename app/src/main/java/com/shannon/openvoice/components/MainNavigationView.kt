package com.shannon.openvoice.components

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.LayoutMainNavigationBinding
import timber.log.Timber

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.components
 * @ClassName:      MainNavigationView
 * @Description:     首页底部导航
 * @Author:         czhen
 * @CreateDate:     2022/7/25 9:48
 */
class MainNavigationView : LinearLayout {

    private val viewBinding = inflateBinding<LayoutMainNavigationBinding>(this)
    private val tabIconList = arrayListOf<View>()
    private val tabTextList = arrayListOf<View>()

    private var currentPosition = -1
    private var onTabSelectedListener: OnTabSelectedListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        viewBinding.run {
            clipChildren = false
            addView(root)
            tabIconList.addArray(
                homeTabIcon,
                tradeTabIcon,
                mainAddButton,
                creationTabIcon,
                notificationTabIcon
            )
            tabTextList.addArray(
                homeTabText,
                tradeTabText,
                mainAddButton,
                creationTabText,
                notificationTabText
            )
            homeLayout.singleClick(this@MainNavigationView::onClick)
            tradeLayout.singleClick(this@MainNavigationView::onClick)
            mainAddButton.singleClick(this@MainNavigationView::onClick)
            creationLayout.singleClick(this@MainNavigationView::onClick)
            notificationLayout.singleClick(this@MainNavigationView::onClick)
        }
        selectTab(0)
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.homeLayout -> selectTab(0)
            R.id.tradeLayout -> selectTab(1)
            R.id.mainAddButton -> selectTab(2)
            R.id.creationLayout -> selectTab(3)
            R.id.notificationLayout -> selectTab(4)
        }
    }

    private fun selectTab(newPosition: Int) {
        Timber.e("newPosition = $newPosition")

        val result = onTabSelectedListener?.allowTabSelect(newPosition) ?: true
        if (result) {
            if (currentPosition != newPosition && newPosition != 2) {
                setSelectedTabView(newPosition)
                if (currentPosition >= 0) onTabSelectedListener?.onTabUnselected(currentPosition)
                currentPosition = newPosition
            }
            onTabSelectedListener?.onTabSelected(newPosition)
        }
    }

    private fun setSelectedTabView(position: Int) {
        tabIconList.withIndex().forEach {
            it.value.isSelected = it.index == position
            it.value.isActivated = it.index == position
        }
        tabTextList.withIndex().forEach {
            it.value.isSelected = it.index == position
            it.value.isActivated = it.index == position
        }
    }

    private fun hideTabText() {
        tabTextList.forEach { it.gone() }
    }

    fun setOnTabSelectedListener(tabSelectedListener: OnTabSelectedListener) {
        this.onTabSelectedListener = tabSelectedListener
    }

    fun setNotificationBadge(count: Int) {
        viewBinding.badgeView.visibility(count > 0)
        viewBinding.badgeView.text = if (count > 99) "99+" else count.toString()
    }

    interface OnTabSelectedListener {
        fun allowTabSelect(position: Int): Boolean
        fun onTabSelected(position: Int)
        fun onTabUnselected(position: Int)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.mCurrentItem = currentPosition
        return ss
    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            val newPosition = state.mCurrentItem
            setSelectedTabView(newPosition)
            currentPosition = newPosition
            super.onRestoreInstanceState(state.superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState {
        var mCurrentItem = -1

        constructor(source: Parcel) : super(source) {
            readValues(source)
        }

        constructor(superState: Parcelable?) : super(superState)

        private fun readValues(source: Parcel) {
            mCurrentItem = source.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeInt(mCurrentItem)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {

            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}