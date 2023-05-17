import com.shannon.plugin.version.*

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.shannon.plugin.version")
}

private object BuildType {
    const val RELEASE = "release"
    const val DEBUG = "debug"
    const val FOX = "fox"
    const val RELEASE_TEST = "releaseTest"
}

android {
    compileSdk = AndroidConfig.compileSdkVersion

    defaultConfig {
        minSdk = AndroidConfig.minSdkVersion
        targetSdk = AndroidConfig.targetSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
        create(BuildType.FOX) {
            isMinifyEnabled = false
        }
        create(BuildType.RELEASE_TEST) {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(Deps.AndroidX.coreKtx)
    implementation(Deps.AndroidX.appcompat)
    implementation(Deps.AndroidX.recyclerview)
    implementation(Deps.AndroidX.constraintLayout)
    implementation(Deps.AndroidX.material)

    api(Deps.Http.retrofit)
    api(Deps.Http.converterScalars)
    api(Deps.Http.converterGson)
    api(Deps.Http.adapterRxjava)
    api(Deps.Http.okhttp)
    api(Deps.Http.okhttpLogger)
    api(Deps.Http.gson)
    api(Deps.Http.rxJava)
    api(Deps.Http.rxAndroid)
    api(Deps.Http.rxKotlin)
    api(Deps.Http.rxlifeRxjava3)

    api(Deps.Libs.mmkv)
    api(Deps.Libs.byRecyclerView)
    api(Deps.Libs.timber)
    api(Deps.Libs.immersionBar)
    api(Deps.Libs.utilcodex)
    api(Deps.Libs.autoSize)


    testImplementation(Deps.Test.junit)
    androidTestImplementation(Deps.Test.testExt)
    androidTestImplementation(Deps.Test.testEspresso)
}