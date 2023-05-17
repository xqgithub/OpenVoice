plugins {
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
}
repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    //添加Gradle相关的API，否则无法自定义Plugin和Task
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("version") {
            //添加插件
            id = "com.shannon.plugin.version"
            //在根目录创建类 VersionPlugin 继承 Plugin<Project>
            implementationClass = "com.shannon.plugin.version.VersionPlugin"
        }
    }
}