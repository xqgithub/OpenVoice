package com.shannon.openvoice.business.guide

import com.shannon.android.lib.base.viewmodel.BaseViewModel

/**
 * Date:2022/11/1
 * Time:10:44
 * author:dimple
 */
class GuideViewModel : BaseViewModel() {


    enum class Kind {
        //推荐页
        RecommendedPage,

        //推荐页1
        RecommendedPageOne,

        //推荐页2
        RecommendedPageTwo,

        //推荐页3
        RecommendedPageThree,

        //创作页
        AuthoringPage,

        AuthoringPageOne,

        AuthoringPageTwo,

        AuthoringPageThree,

        AuthoringPageFourth
    }
}