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
        val chainUrl = getChainUrl()

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "CHAIN_URL", chainUrl[0])
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "CHAIN_URL", chainUrl[1])
        }
        create(BuildType.FOX) {
            isMinifyEnabled = false
            buildConfigField("String", "CHAIN_URL", chainUrl[1])
        }
        create(BuildType.RELEASE_TEST) {
            isMinifyEnabled = false
            buildConfigField("String", "CHAIN_URL", chainUrl[1])
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
    implementation(Deps.AndroidX.material)
    implementation(Deps.AndroidX.kotlinCoroutines)
    implementation(Deps.AndroidX.fragment)

    testImplementation(Deps.Test.junit)
    androidTestImplementation(Deps.Test.testExt)
    androidTestImplementation(Deps.Test.testEspresso)

    implementation(Deps.Web3.walletConnect)
    implementation(Deps.Web3.walletCore)
    implementation(Deps.Web3.walletSocket)
    implementation(Deps.Web3.walletAdapter)
    implementation(Deps.Web3.walletStore)
    implementation(Deps.Web3.walletCustom)
    implementation(Deps.Web3.web3jCore)

    implementation(Deps.Http.okhttp)
    implementation(Deps.Http.okhttpLogger)
    implementation(Deps.Http.gson)
    implementation(Deps.Http.retrofit)
    implementation(Deps.Http.converterScalars)
    implementation(Deps.Http.converterGson)

}

fun getChainUrl(): Array<String> {
    val properties = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir)
    val releaseChainUrl = properties.getProperty("releaseChainUrl")
    val debugChainUrl = properties.getProperty("debugChainUrl")

    if (releaseChainUrl.isNullOrEmpty() || debugChainUrl.isNullOrEmpty() ) {
        throw IllegalStateException(
            "Please configure 'releaseChainUrl,debugChainUrl' related information in the 'local.properties',refer to README.md"
        )
    }
    return arrayOf(releaseChainUrl, debugChainUrl)
}