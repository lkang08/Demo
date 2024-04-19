package com.bilin.monitor.plugin.bitmap

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class BitmapMonitorPlugin : Plugin<Project> {
    companion object {
        const val TAG = "BitmapMonitorPlugin"
    }

    override fun apply(project: Project) {
        println("$TAG apply")
        val config =  project.extensions.create("bitmapMonitoryConfig", BitmapMonitoryConfig::class.java)
        val appExtension: AppExtension? = project.extensions.findByType(AppExtension::class.java)
        appExtension?.registerTransform(BitmapMonitorTransform(config))
        project.afterEvaluate {
            //todo 获取配置
        }
    }
}