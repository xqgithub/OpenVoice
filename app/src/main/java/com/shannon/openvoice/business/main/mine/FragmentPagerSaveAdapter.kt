package com.shannon.openvoice.business.main.mine

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle

/**
 *
 * @ProjectName:    Pokémon
 * @Package:        com.samaroil.pokemon.components.ui
 * @ClassName:      FragmentPagerSaveAdapter
 * @Description:     使用Fragment的show()、hide()的逻辑来避免Fragment的View被销毁
 * @Author:         czhen
 * @CreateDate:     2021/1/7 11:19
 */
abstract class FragmentPagerSaveAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private lateinit var mCurTransaction: FragmentTransaction
    private var mCurrentPrimaryItem: Fragment? = null
    private val mFragmentManager: FragmentManager = fm

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        mCurTransaction = mFragmentManager.beginTransaction()
        val itemId = getItemId(position)

        // Do we already have this fragment?
        val name = makeFragmentName(container.id, itemId)

        var fragment = mFragmentManager.findFragmentByTag(name)
        if (fragment != null) {
            mCurTransaction.show(fragment)
        } else {
            fragment = getItem(position)
            mCurTransaction.add(
                container.id, fragment,
                makeFragmentName(container.id, itemId)
            )
        }
        if (fragment !== mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false)
            mCurTransaction.setMaxLifecycle(fragment, Lifecycle.State.STARTED)

        }
        mCurTransaction.commit()
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment
        mCurTransaction = mFragmentManager.beginTransaction()

        mCurTransaction.hide(fragment)
        if (fragment == mCurrentPrimaryItem) {
            mCurrentPrimaryItem = null
        }
        mCurTransaction.commit()
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment
        if (fragment !== mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem!!.setMenuVisibility(false)
                mCurTransaction = mFragmentManager.beginTransaction()
                mCurTransaction.setMaxLifecycle(
                    mCurrentPrimaryItem!!,
                    Lifecycle.State.STARTED
                )
                mCurTransaction.commit()
            }
            fragment.setMenuVisibility(true)
            mCurTransaction = mFragmentManager.beginTransaction()
            mCurTransaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
            mCurrentPrimaryItem = fragment
            mCurTransaction.commit()
        }
    }

    private fun makeFragmentName(viewId: Int, id: Long): String? {
        return "android:switcher:$viewId:$id"
    }
}