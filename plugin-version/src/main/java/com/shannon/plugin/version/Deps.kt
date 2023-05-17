package com.shannon.plugin.version

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.plugin.version
 * @ClassName:      Deps
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/18 13:59
 */
object Deps {

    object AndroidX {
        private const val activityVersion = "1.3.1"
        private const val fragmentVersion = "1.3.1"
        private const val recyclerviewVersion = "1.2.1"
        private const val appcompatVersion = "1.3.0"
        private const val materialVersion = "1.4.0"
        private const val constraintLayoutVersion = "2.1.3"
        private const val coreKtxVersion = "1.6.0"
        private const val multidexVersion = "2.0.1"
        private const val mediaVersion = "1.4.3"
        private const val kotlinCoroutinesVersion = "1.6.3"


        const val coreKtx = "androidx.core:core-ktx:$coreKtxVersion"
        const val activity = "androidx.activity:activity-ktx:$activityVersion"
        const val fragment = "androidx.fragment:fragment-ktx:$fragmentVersion"
        const val recyclerview = "androidx.recyclerview:recyclerview:$recyclerviewVersion"
        const val appcompat = "androidx.appcompat:appcompat:$appcompatVersion"
        const val material = "com.google.android.material:material:$materialVersion"
        const val constraintLayout =
            "androidx.constraintlayout:constraintlayout:$constraintLayoutVersion"
        const val multidex = "androidx.multidex:multidex:$multidexVersion"
        const val media = "androidx.media:media:$mediaVersion"
        const val kotlinCoroutines =
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion"
        const val kotlinCoroutinesAndroid =
            "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutinesVersion"

    }

    object Test {
        private const val testExtVersion = "1.1.2"
        private const val testEspressoVersion = "3.3.0"
        private const val junitVersion = "4.12"
        const val testExt = "androidx.test.ext:junit:$testExtVersion"
        const val testEspresso = "androidx.test.espresso:espresso-core:$testEspressoVersion"
        const val junit = "junit:junit:$junitVersion"
    }

    object Glide {
        private const val glideVersion = "4.13.2"
        const val glide = "com.github.bumptech.glide:glide:$glideVersion"
        const val glideCompiler = "com.github.bumptech.glide:compiler:$glideVersion"
    }

    object Http {
        private const val retrofitVersion = "2.9.0"
        private const val okhttpVersion = "4.10.0"
        private const val gsonVersion = "2.9.0"
        private const val rxJavaVersion = "3.1.5"
        private const val rxAndroidVersion = "3.0.0"
        private const val rxKotlinVersion = "3.0.1"
        private const val rxlifeRxjava3Version = "2.2.2"
        const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
        const val converterScalars = "com.squareup.retrofit2:converter-scalars:$retrofitVersion"
        const val converterGson = "com.squareup.retrofit2:converter-gson:$retrofitVersion"
        const val adapterRxjava = "com.squareup.retrofit2:adapter-rxjava3:$retrofitVersion"
        const val okhttp = "com.squareup.okhttp3:okhttp:$okhttpVersion"
        const val okhttpLogger = "com.squareup.okhttp3:logging-interceptor:$okhttpVersion"
        const val gson = "com.google.code.gson:gson:$gsonVersion"
        const val rxJava = "io.reactivex.rxjava3:rxjava:$rxJavaVersion"
        const val rxAndroid = "io.reactivex.rxjava3:rxandroid:$rxAndroidVersion"
        const val rxKotlin = "io.reactivex.rxjava3:rxkotlin:$rxKotlinVersion"
        const val rxlifeRxjava3 =
            "com.github.liujingxing.rxlife:rxlife-rxjava3:$rxlifeRxjava3Version"
    }

    object Libs {
        private const val byRecyclerViewVersion = "1.3.2"
        private const val permissionVersion = "1.6.4"
        private const val bannerVersion = "2.1.0"
        private const val cosVersion = "5.7.1"
        private const val timberVersion = "5.0.1"
        private const val startupVersion = "1.0.2"
        private const val immersionBarVersion = "3.2.2"
        private const val mmkvVersion = "1.2.13"
        private const val eventBusVersion = "3.3.1"
        private const val autoSizeVersion = "1.2.1"
        private const val utilcodexVersion = "1.31.0"
        private const val lottieVersion = "3.5.0"
        private const val sparkButtonVersion = "4.1.0"
        private const val pictureSelectorVersion = "v3.10.8"
        private const val kotlinCallAdapterVersion = "1.0.1"
        private const val androidPickerViewVersion = "4.1.9"
        private const val swipeDelMenuLayoutVersion = "V1.3.0"
        private const val photoViewVersion = "2.3.0"
        private const val xpopupVersion = "2.8.15"
        private const val agentwebVersion = "v5.0.6-androidx"
        private const val awsS3Version = "2.55.0"
        private const val lightCompressorVersion = "1.2.0"

        const val byRecyclerView = "com.github.youlookwhat:ByRecyclerView:$byRecyclerViewVersion"
        const val permissionX = "com.guolindev.permissionx:permissionx:$permissionVersion"
        const val banner = "com.youth.banner:banner:$bannerVersion"
        const val cosCloud = "com.qcloud.cos:cos-android-lite:$cosVersion"
        const val timber = "com.jakewharton.timber:timber:$timberVersion"
        const val startup = "com.github.Caij:DGAppStartup:$startupVersion"
        const val immersionBar = "com.geyifeng.immersionbar:immersionbar:$immersionBarVersion"
        const val mmkv = "com.tencent:mmkv:$mmkvVersion"
        const val eventBus = "org.greenrobot:eventbus:$eventBusVersion"
        const val autoSize = "com.github.JessYanCoding:AndroidAutoSize:v$autoSizeVersion"
        const val utilcodex = "com.blankj:utilcodex:$utilcodexVersion"
        const val lottie = "com.airbnb.android:lottie:$lottieVersion"
        const val sparkButton = "com.github.connyduck:sparkbutton:$sparkButtonVersion"
        const val pictureSelector =
            "io.github.lucksiege:pictureselector:$pictureSelectorVersion"
        const val pictureSelectorCompress =
            "io.github.lucksiege:compress:$pictureSelectorVersion"
        const val pictureSelectorCrop =
            "io.github.lucksiege:ucrop:$pictureSelectorVersion"
        const val pictureSelectorCamerax =
            "io.github.lucksiege:camerax:$pictureSelectorVersion"

        const val androidPickerView =
            "com.contrarywind:Android-PickerView:$androidPickerViewVersion"
        const val swipeDelMenuLayout =
            "com.github.mcxtzhang:SwipeDelMenuLayout:$swipeDelMenuLayoutVersion"
        const val kotlinCallAdapter =
            "at.connyduck:kotlin-result-calladapter:$kotlinCallAdapterVersion"
        const val photoView = "com.github.chrisbanes:PhotoView:$photoViewVersion"
        const val xpopup = "com.github.li-xiaojun:XPopup:$xpopupVersion"
        const val agentwebCore = "com.github.Justson.AgentWeb:agentweb-core:$agentwebVersion"
        const val agentwebFilechooser =
            "com.github.Justson.AgentWeb:agentweb-filechooser:$agentwebVersion"
        const val awsS3 = "com.amazonaws:aws-android-sdk-s3:$awsS3Version"
        const val awsMobileClient = "com.amazonaws:aws-android-sdk-mobile-client:$awsS3Version"
        const val lightCompressor =
            "com.github.AbedElazizShe:LightCompressor:$lightCompressorVersion"
    }

    object ExoPlayer {
        private const val playerVersion = "2.16.0"
        const val playerCore = "com.google.android.exoplayer:exoplayer-core:$playerVersion"
        const val playerUi = "com.google.android.exoplayer:exoplayer-ui:$playerVersion"
        const val playerSession =
            "com.google.android.exoplayer:extension-mediasession:$playerVersion"

    }

    object Room {
        private const val roomVersion = "2.3.0"

        const val roomRuntime = "androidx.room:room-runtime:$roomVersion"
        const val roomCompiler = "androidx.room:room-compiler:$roomVersion"
        const val roomKtx = "androidx.room:room-ktx:$roomVersion"
        const val roomRxJava = "androidx.room:room-rxjava3:$roomVersion"
    }

    object Web3 {
        private const val web3jVersion = "4.8.7"
        private const val walletVersion = "0.7.0"

        const val walletConnect = "com.jemshit.walletconnect:walletconnect:$walletVersion"
        const val walletSocket =
            "com.jemshit.walletconnect:walletconnect-socket-scarlet:$walletVersion"
        const val walletAdapter =
            "com.jemshit.walletconnect:walletconnect-adapter-gson:$walletVersion"
        const val walletStore = "com.jemshit.walletconnect:walletconnect-store-prefs:$walletVersion"
        const val walletCore = "com.jemshit.walletconnect:walletconnect-core:$walletVersion"
        const val walletCustom = "com.jemshit.walletconnect:walletconnect-requests:$walletVersion"

        const val web3jCore = "org.web3j:core:$web3jVersion-android"
    }


    object GoogleFirebase {
        const val firebasebom = "com.google.firebase:firebase-bom:31.2.2"
        const val firebasemessaging = "com.google.firebase:firebase-messaging-ktx"
        const val firebaseanalytics = "com.google.firebase:firebase-analytics-ktx"
        const val firebaseinstallations = "com.google.firebase:firebase-installations-ktx:17.1.2"
        const val workruntime = "androidx.work:work-runtime-ktx:2.7.1"
    }


}