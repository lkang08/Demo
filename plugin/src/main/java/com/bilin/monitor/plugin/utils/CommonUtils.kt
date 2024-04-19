package com.bilin.monitor.plugin.utils

import java.io.File
import org.apache.commons.codec.binary.Hex

object CommonUtils {

    private fun isAndroidGeneratedClass(className: String): Boolean {
        return className.contains("R$") ||
            className.contains("R2$") ||
            className.contains("R.class") ||
            className.contains("R2.class") ||
            className == "BuildConfig.class"
    }

    fun isLegalJar(file: File): Boolean {
        return file.isFile &&
            file.length() > 0L &&
            file.name != "R.jar" &&
            file.name.endsWith(".jar")
    }

    fun isLegalClass(file: File): Boolean {
        return file.isFile && isLegalClass(file.name)
    }

    fun isLegalClass(fileName: String): Boolean {
        return fileName.endsWith(".class") && !isAndroidGeneratedClass(fileName)
    }

    fun generateJarFileName(jarFile: File): String {
        return getMd5ByFilePath(jarFile) + "_" + jarFile.name
    }

    fun generateClassFileName(classFile: File): String {
        return getMd5ByFilePath(classFile) + "_" + classFile.name
    }

    private fun getMd5ByFilePath(file: File): String {
        return Hex.encodeHexString(file.absolutePath.toByteArray()).substring(0, 8)
    }

    fun printLog(tag: String?, string: String?) {
        println("$tag,$string")
    }
}