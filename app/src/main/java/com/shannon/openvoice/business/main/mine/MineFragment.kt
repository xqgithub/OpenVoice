package com.shannon.openvoice.business.main.mine

import android.os.Bundle
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.intentToJump
import com.shannon.android.lib.extended.showConfirmDialog
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.draft.DraftActivity
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.business.main.mine.announcement.AnnouncementsActivity
import com.shannon.openvoice.business.main.mine.blacklist.BlackListActivity
import com.shannon.openvoice.business.main.mine.editprofile.EditProfileActivity
import com.shannon.openvoice.business.main.mine.setting.ModelYouLikesAcvity
import com.shannon.openvoice.business.main.mine.setting.SettingActivity
import com.shannon.openvoice.business.status.StatusListActivity
import com.shannon.openvoice.components.ImageAndTextUi
import com.shannon.openvoice.databinding.FragmentMineBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.AccountBean
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.main.mine
 * @ClassName:      MineFragment
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/27 9:44
 */
class MineFragment : KBaseFragment<FragmentMineBinding, MineViewModel>() {

    override fun onInit() {
        setNavigationBarColor(ThemeUtil.getColor(requireContext(), R.attr.navigationBarBackground))

        EventBus.getDefault().register(this)

        binding.apply {
//            appViewModel.setTopViewHeight(requireActivity(), clMain, R.id.clUserInfo, 16.dp)


            clUserInfo.singleClick {
                intentToJump(requireActivity(), EditProfileActivity::class.java)
            }

            itProfile.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_opv_setting_myaccount, 24.dp, 24.dp)
                setTextData(
                    requireContext().getString(R.string.summary),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        requireContext(),
                        android.R.attr.textColorSecondary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                singleClick {
                    intentToJump(requireActivity(), AccountActivity::class.java,
                        Bundle().apply {
//                        putString("accountId", "108757694605460963")
                            putString("accountId", accountManager.getAccountData().id)
                        }
                    )
                }
            }

            itCollect.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_mine_collect, 24.dp, 24.dp)
                setTextData(
                    requireContext().getString(R.string.collections),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        requireContext(),
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                singleClick {
                    startActivity(StatusListActivity.newFavouritesIntent(context))
                }
            }

            itDraft.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_mine_draft, 24.dp, 24.dp)
                setTextData(
                    requireContext().getString(R.string.drafts),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        requireContext(),
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                singleClick {
                    startActivity(DraftActivity.newIntent(context))
                }
            }

            itAnnouncement.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_mine_public, 24.dp, 24.dp)
                setTextData(
                    requireContext().getString(R.string.announcements),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        requireContext(),
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                singleClick {
//                    ToastUtil.showCenter("进入到公告页面")
                    intentToJump(requireActivity(), AnnouncementsActivity::class.java)
                }
            }

            itSettings.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_mine_set, 24.dp, 24.dp)
                setTextData(
                    requireContext().getString(R.string.preferences),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        requireContext(),
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                singleClick {
//                    ToastUtil.showCenter("进入到设置和隐私页面")
                    intentToJump(
                        requireActivity(),
                        SettingActivity::class.java,
                        bundle = Bundle().apply {
                            putBoolean("restartActivitiesOnExit", false)
                        })
                }
            }

            itBlackList.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_mine_blacklist, 24.dp, 24.dp)
                setTextData(
                    requireContext().getString(R.string.blacklist),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        requireContext(),
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                singleClick {
//                    ToastUtil.showCenter("进入黑名单页面")
                    intentToJump(requireActivity(), BlackListActivity::class.java)
                }
            }

            itQuit.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_mine_quit, 24.dp, 24.dp)
                setTextData(
                    requireContext().getString(R.string.login_out),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        requireContext(),
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                singleClick {
//                    ToastUtil.showCenter("退出程序")
                    requireContext().showConfirmDialog(
                        message = getString(R.string.desc_sure_exit)
                    ) {
                        appViewModel.jump2MainPage(requireActivity())
                    }
                }
            }

            itYouLikeModels.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_mine_you_like_models, 24.dp, 24.dp)
                setTextData(
                    requireContext().getString(R.string.you_like_models),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        requireContext(),
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)
                singleClick {
                    intentToJump(requireActivity(), ModelYouLikesAcvity::class.java)
                }
            }
        }
    }


    /**
     * 设置用户信息
     */
    private fun setUserInfo() {
        binding.apply {
            sivAvatar.loadAvatar(accountManager.getAccountData().avatar)
            tvNickname.apply {
                text = accountManager.getAccountData().name
            }
            tvUserName.apply {
                text = "@${accountManager.getAccountData().username}"
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusAccountBean(event: AccountBean) {
        binding.apply {
            sivAvatar.loadAvatar(event.avatar)
            tvNickname.apply {
                text = event.name
            }
            tvUserName.apply {
                text = "@${event.username}"
            }
        }
    }


    override fun onResume() {
        super.onResume()
        setUserInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}