import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.shannon.plugin.version.*
import com.shannon.plugin.version.AndroidConfig.applicationId
import com.shannon.plugin.version.AndroidConfig.versionCode
import com.shannon.plugin.version.AndroidConfig.versionName
import com.shannon.plugin.version.SignConfig.keyAlias
import com.shannon.plugin.version.SignConfig.keyPassword
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.shannon.plugin.version")
    id("kotlin-parcelize")
//    id("kotlin-android-extensions")
    id("com.google.gms.google-services")
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
        applicationId = AndroidConfig.applicationId
        minSdk = AndroidConfig.minSdkVersion
        targetSdk = AndroidConfig.targetSdkVersion
        versionCode = AndroidConfig.versionCode
        versionName = AndroidConfig.versionName

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
//        ndk {
//            abiFilters.add("armeabi-v7a")
//            abiFilters.add("arm64-v8a")
//        }
    }
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
    signingConfigs {
        create(BuildType.RELEASE) {
            storeFile = File(SignConfig.keyStoreFile)
            storePassword = SignConfig.keyStorePassword
            keyAlias = SignConfig.keyAlias
            keyPassword = SignConfig.keyPassword

            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        val hostInfo = getHostInfo()
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            signingConfig = signingConfigs.getByName(BuildType.RELEASE)
            manifestPlaceholders["appName"] = AndroidConfig.appName
            buildConfigField("String", "HOST_URL", hostInfo[0])
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            buildConfigField("String", "versionBuild", "\"\"")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".${BuildType.DEBUG}"
            manifestPlaceholders["appName"] = AndroidConfig.appName.plus("-dev")
            buildConfigField("String", "HOST_URL", hostInfo[1])
            buildConfigField("String", "versionBuild", "\"\"")
        }
        create(BuildType.FOX) {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".${BuildType.FOX}"
            signingConfig = signingConfigs.getByName(BuildType.DEBUG)
            manifestPlaceholders["appName"] = AndroidConfig.appName.plus("-pre")
            buildConfigField("String", "HOST_URL", hostInfo[2])
            buildConfigField("String", "versionBuild", "\"\"")
        }
        create(BuildType.RELEASE_TEST) {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName(BuildType.DEBUG)
            applicationIdSuffix = ".${BuildType.RELEASE_TEST.toLowerCaseAsciiOnly()}"
            manifestPlaceholders["appName"] = AndroidConfig.appName.plus("-d")
            buildConfigField("String", "HOST_URL", hostInfo[1])
            buildConfigField("String", "versionBuild", "\"${AndroidConfig.versionBuild}\"")
        }
    }

    applicationVariants.all {
        val buildType = buildType.name
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                if (buildType == BuildType.RELEASE) {
                    outputFileName =
                        "OpenVoice_v${versionName}_${flavorName}_${AndroidConfig.baleDate}_${buildType}.apk"
                } else if (buildType == BuildType.RELEASE_TEST) {
                    outputFileName =
                        "OpenVoice_v${versionName}${
                            AndroidConfig.versionBuild.replace(
                                " ",
                                "_"
                            )
                        }_${flavorName}_${AndroidConfig.baleDate}_${buildType}.apk"
                }
            }
        }
    }

    flavorDimensions.add(AndroidConfig.appName)
    productFlavors {
        create("official")
        create("internal")
        create("google")
    }
    productFlavors.all {
        manifestPlaceholders["APP_CHANNEL"] = name
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

//    splits {
//        abi {
//            isEnable = true
//            reset()
//            include("armeabi-v7a", "arm64-v8a")
//            isUniversalApk = true
//        }
//    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(Deps.AndroidX.coreKtx)
    implementation(Deps.AndroidX.appcompat)
    implementation(Deps.AndroidX.material)
    implementation(Deps.AndroidX.activity)
    implementation(Deps.AndroidX.fragment)
    implementation(Deps.AndroidX.recyclerview)
    implementation(Deps.AndroidX.constraintLayout)
    implementation(Deps.AndroidX.multidex)
    implementation(Deps.AndroidX.kotlinCoroutines)
    implementation(Deps.AndroidX.kotlinCoroutinesAndroid)

    implementation(Deps.Glide.glide)
    kapt(Deps.Glide.glideCompiler)
    implementation(Deps.Libs.eventBus)
    implementation(Deps.Libs.startup)
    implementation(Deps.Libs.permissionX)
    implementation(Deps.Libs.lottie)
    implementation(Deps.Libs.cosCloud)
    implementation(Deps.Libs.sparkButton)
    implementation(Deps.Libs.pictureSelector)
    implementation(Deps.Libs.pictureSelectorCompress)
    implementation(Deps.Libs.pictureSelectorCrop)
    implementation(Deps.Libs.pictureSelectorCamerax)

    implementation(Deps.Libs.kotlinCallAdapter)

    implementation(Deps.Room.roomRuntime)
    kapt(Deps.Room.roomCompiler)
    implementation(Deps.Room.roomKtx)
    implementation(Deps.Room.roomRxJava)
    implementation(Deps.Libs.androidPickerView)
    implementation(Deps.Libs.swipeDelMenuLayout)
    implementation(Deps.Libs.photoView)
    implementation(Deps.Libs.xpopup)
    implementation(Deps.Libs.agentwebCore)
    implementation(Deps.Libs.agentwebFilechooser)
    implementation(Deps.Libs.awsS3)
    implementation(Deps.Libs.awsMobileClient)
    implementation(Deps.Libs.lightCompressor)

    // Import the Firebase BoM
    implementation(platform(Deps.GoogleFirebase.firebasebom))
    // Firebase Cloud Messaging (Kotlin)
    implementation(Deps.GoogleFirebase.firebasemessaging)
    // For an optimal experience using FCM, add the Firebase SDK
    // for Google Analytics. This is recommended, but not required.
    implementation(Deps.GoogleFirebase.firebaseanalytics)
//    implementation(Deps.GoogleFirebase.firebaseinstallations)
    implementation(Deps.GoogleFirebase.workruntime)


    testImplementation(Deps.Test.junit)
    androidTestImplementation(Deps.Test.testExt)
    androidTestImplementation(Deps.Test.testEspresso)

    implementation(project(":lib-core"))
    implementation(project(":lib-player"))
    implementation(project(":lib-web3"))

}

fun getHostInfo(): Array<String> {
    val properties = gradleLocalProperties(rootDir)
    val releaseHostUrl = properties.getProperty("releaseHostUrl")
    val debugHostUrl = properties.getProperty("debugHostUrl")
    val foxHostUrl = properties.getProperty("foxHostUrl")

    if (releaseHostUrl.isNullOrEmpty() || debugHostUrl.isNullOrEmpty() || foxHostUrl.isNullOrEmpty()) {
        throw IllegalStateException(
            "Please configure 'releaseHostUrl,debugHostUrl,foxHostUrl' related information in the 'local.properties',refer to README.md"
        )
    }
    return arrayOf(releaseHostUrl, debugHostUrl, foxHostUrl)
}
