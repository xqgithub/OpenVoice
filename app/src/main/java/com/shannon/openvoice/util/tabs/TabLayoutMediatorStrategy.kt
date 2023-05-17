package com.shannon.openvoice.util.tabs

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnStart
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.util.ColorUtil
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R

class TabLayoutMediatorStrategy(
    private val context: Context,
    private val tabTitleList: MutableList<String>,
    private val viewPager: ViewPager2,
    private val iTabConfigurationStrategy: ITabConfigurationStrategy? = null

) :
    TabLayoutMediator.TabConfigurationStrategy, OnTabPageChangeCallback {

    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
        createTabView(tab, position)
        iTabConfigurationStrategy?.onConfigureTab(tab, position)
    }

    /**
     * 创建自定义的TabView
     * @param position Int
     * @param tab Tab
     * @return TabLayout.Tab
     */
    private fun createTabView(tab: TabLayout.Tab, position: Int): TabLayout.Tab {

        val tabView = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val tabIndicatorView = ImageView(context).apply {
            id = R.id.tabIndicator
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            ).apply {
                bottomMargin = 8.dp

            }
            setImageResource(R.drawable.tab_underline)
            alpha = if (viewPager.currentItem == position) 1f else 0f
        }

        val tabTextView = TextView(context).apply {
            id = R.id.tabText
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
            text = tabTitleList[position]
            includeFontPadding = false
            textSize = if (viewPager.currentItem == position) 16f else 14f
            setTextColor(
                if (viewPager.currentItem == position) ThemeUtil.getColor(
                    context,
                    android.R.attr.textColorLink
                ) else ThemeUtil.getColor(
                    context, android.R.attr.textColorTertiary
                )
            )
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        val paint = Paint()
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            20f,
            context.resources.displayMetrics
        )
        val textWidth = paint.measureText(tabTitleList[position])
        val measuredWidth = (textWidth + 5.dp).toInt()
        tabView.layoutParams.width = measuredWidth
        tabView.addView(tabIndicatorView)
        tabView.addView(tabTextView)
        tab.customView = tabView

        return tab
    }

    override fun onTabSelected(view: View) {
        val tabText = view.findViewById<TextView>(R.id.tabText)
        val tabIndicator = view.findViewById<ImageView>(R.id.tabIndicator)

        val defaultLeft = (tabIndicator.width * 0.2f).toInt()
        val offset = tabIndicator.width - defaultLeft
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 200
        animator.interpolator = DecelerateInterpolator()
        animator.doOnStart {
            tabIndicator.alpha = 1f
            tabIndicator.clipBounds = Rect(0, 0, defaultLeft, tabIndicator.height)
        }
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            tabIndicator.clipBounds =
                Rect(0, 0, (defaultLeft + (offset * value)).toInt(), tabIndicator.height)
        }
        animator.start()

        tabText.textSize = 16F
        tabText.setTextColor(
            ThemeUtil.getColor(
                context,
                android.R.attr.textColorLink
            )
        )
    }

    override fun onTabUnselected(view: View) {
        val tabText = view.findViewById<TextView>(R.id.tabText)
        val tabIndicator = view.findViewById<ImageView>(R.id.tabIndicator)
        tabIndicator.alpha = 0f
        tabText.textSize = 14F
        tabText.setTextColor(
            ThemeUtil.getColor(
                context, android.R.attr.textColorTertiary
            )
        )
    }

    override fun onPageScrolled(selectedChild: View, nextTabView: View, positionOffset: Float) {
        val selectedTabText = selectedChild.findViewById<TextView>(R.id.tabText)
        val selectedTabIndicator = selectedChild.findViewById<ImageView>(R.id.tabIndicator)

        val nextTabText = nextTabView.findViewById<TextView>(R.id.tabText)
        val nextTabIndicator = nextTabView.findViewById<ImageView>(R.id.tabIndicator)

        selectedTabText.textSize = 14F + (2F * (1f - positionOffset))
        nextTabText.textSize = 14F + (2F * (positionOffset))
        selectedTabText.setTextColor(
            ColorUtil.evaluate(
                1f - positionOffset, ThemeUtil.getColor(
                    context, android.R.attr.textColorTertiary
                ), ThemeUtil.getColor(
                    context,
                    android.R.attr.textColorLink
                )
            )
        )
        nextTabText.setTextColor(
            ColorUtil.evaluate(
                positionOffset, ThemeUtil.getColor(
                    context, android.R.attr.textColorTertiary
                ), ThemeUtil.getColor(
                    context,
                    android.R.attr.textColorLink
                )
            )
        )
        if (selectedTabIndicator.alpha != 0f)
            selectedTabIndicator.alpha = (1f - positionOffset)

        if (nextTabIndicator.alpha != 0f)
            nextTabIndicator.alpha = positionOffset

    }


}