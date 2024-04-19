package com.demo.common

interface FlavorConfig {
    val name: String
    val externalName: String get() = name
    val applicationId: String
    val appName: String
    /**
     * 申请的马甲项目名字
     */
    val cixProjectName: String get() = name
}

val maJiaFlavors: List<FlavorConfig> = listOf(Demo1, Demo2)

fun getFlavorConfig(flavorName: String): FlavorConfig? {
    for (flavorConfig in maJiaFlavors) {
        if (flavorConfig.cixProjectName == flavorName) {
            return flavorConfig
        }
    }
    return null
}