package com.bilin.huijiao.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.bilin.monitor.plugin.utils.CommonUtils.generateClassFileName
import com.bilin.monitor.plugin.utils.CommonUtils.generateJarFileName
import com.bilin.monitor.plugin.utils.CommonUtils.isLegalClass
import com.bilin.monitor.plugin.utils.CommonUtils.isLegalJar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.Type

abstract class BaseTransform : Transform() {
    open val TAG = "BaseTransform"
    private val executorService: ExecutorService = ForkJoinPool.commonPool()

    private val taskList = mutableListOf<Callable<Unit>>()

    override fun transform(transformInvocation: TransformInvocation) {
        printLog(TAG, "transform start---------------> isIncremental=${transformInvocation.isIncremental}")
        onTransformStart()
        val startTime = System.currentTimeMillis()
        val inputs = transformInvocation.inputs
        val outputProvider = transformInvocation.outputProvider
        val context = transformInvocation.context
        val isIncremental = transformInvocation.isIncremental
        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                submitTask {
                    forEachJar(jarInput, outputProvider, context, isIncremental)
                }
            }
            input.directoryInputs.forEach { dirInput ->
                submitTask {
                    forEachDirectory(dirInput, outputProvider, context, isIncremental)
                }
            }
        }
        val taskListFeature = executorService.invokeAll(taskList)
        taskListFeature.forEach {
            it.get()
        }
        onTransformEnd()
        printLog(
            TAG, "transform end--------------->duration : ${(System.currentTimeMillis() - startTime)} ms"
        )
    }

    protected open fun onTransformStart() {
    }

    protected open fun onTransformEnd() {
    }

    private fun forEachJar(
        jarInput: JarInput,
        outputProvider: TransformOutputProvider,
        context: Context,
        isIncremental: Boolean
    ) {
        printLog(TAG, "jarInput: ${jarInput.file.absolutePath}")
        val destFile = outputProvider.getContentLocation(
            generateJarFileName(jarInput.file),
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        if (isIncremental) {
            when (jarInput.status) {
                Status.NOTCHANGED -> {
                }
                Status.REMOVED -> {
                    printLog(TAG, "handle jar ${jarInput.file?.absoluteFile} REMOVED")
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                    return
                }
                Status.ADDED, Status.CHANGED -> {
                    printLog(TAG, "handle jar ${jarInput.file?.absoluteFile} ADDED or CHANGED")
                }
                else -> {
                    return
                }
            }
        }
        if (destFile.exists()) {
            FileUtils.forceDelete(destFile)
        }
        val modifiedJar = if (isLegalJar(jarInput.file)) {
            modifyJar(jarInput.file, context.temporaryDir)
        } else {
            printLog(TAG, "not handle ${jarInput.file?.absoluteFile}")
            jarInput.file
        }
        FileUtils.copyFile(modifiedJar, destFile)
    }

    private fun modifyJar(jarFile: File, temporaryDir: File): File {
        printLog(TAG, "handle jar ${jarFile.absoluteFile}")
        val tempOutputJarFile = File(temporaryDir, generateJarFileName(jarFile))
        if (tempOutputJarFile.exists()) {
            tempOutputJarFile.delete()
        }
        val jarOutputStream = JarOutputStream(FileOutputStream(tempOutputJarFile))
        val inputJarFile = JarFile(jarFile, false)
        try {
            val enumeration = inputJarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val jarEntryName = jarEntry.name
                if (jarEntryName.endsWith(".DSA") || jarEntryName.endsWith(".SF")) {
                    //ignore
                } else {
                    val inputStream = inputJarFile.getInputStream(jarEntry)
                    try {
                        val sourceClassBytes = IOUtils.toByteArray(inputStream)
                        val modifiedClassBytes =
                            if (jarEntry.isDirectory || !isLegalClass(jarEntryName)) {
                                null
                            } else {
                                modifyClass(sourceClassBytes)
                            }
                        jarOutputStream.putNextEntry(JarEntry(jarEntryName))
                        jarOutputStream.write(modifiedClassBytes ?: sourceClassBytes)
                        jarOutputStream.closeEntry()
                    } finally {
                        IOUtils.closeQuietly(inputStream)
                    }
                }
            }
        } finally {
            jarOutputStream.flush()
            IOUtils.closeQuietly(jarOutputStream)
            IOUtils.closeQuietly(inputJarFile)
        }
        return tempOutputJarFile
    }

    private fun forEachDirectory(
        directoryInput: DirectoryInput,
        outputProvider: TransformOutputProvider,
        context: Context,
        isIncremental: Boolean
    ) {
        val dir = directoryInput.file
        val dest = outputProvider.getContentLocation(
            directoryInput.name,
            directoryInput.contentTypes,
            directoryInput.scopes,
            Format.DIRECTORY
        )
        val srcDirPath = dir.absolutePath
        val destDirPath = dest.absolutePath
        val temporaryDir = context.temporaryDir
        FileUtils.forceMkdir(dest)
        if (isIncremental) {
            val changedFilesMap = directoryInput.changedFiles
            for (mutableEntry in changedFilesMap) {
                val classFile = mutableEntry.key
                when (mutableEntry.value) {
                    Status.NOTCHANGED -> {
                    }
                    Status.REMOVED -> {
                        printLog(TAG, "处理 class：${classFile.absoluteFile} REMOVED")
                        //最终文件应该存放的路径
                        val destFilePath = classFile.absolutePath.replace(srcDirPath, destDirPath)
                        val destFile = File(destFilePath)
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                    }
                    Status.ADDED, Status.CHANGED -> {
                        printLog(TAG, "处理 class： ${classFile.absoluteFile} ADDED or CHANGED")
                        modifyClassFile(classFile, srcDirPath, destDirPath, temporaryDir)
                    }
                    else -> {
                    }
                }
            }
        } else {
            directoryInput.file.walkTopDown().filter { it.isFile }
                .forEach { classFile ->
                    modifyClassFile(classFile, srcDirPath, destDirPath, temporaryDir)
                }
        }
    }

    private fun modifyClassFile(
        classFile: File,
        srcDirPath: String,
        destDirPath: String,
        temporaryDir: File
    ) {
        printLog(TAG, "处理 class：${classFile.absoluteFile}")
        //最终文件应该存放的路径
        val destFilePath = classFile.absolutePath.replace(srcDirPath, destDirPath)
        val destFile = File(destFilePath)
        if (destFile.exists()) {
            destFile.delete()
        }
        //拿到修改后的临时文件
        val modifyClassFile = if (isLegalClass(classFile)) {
            modifyClass(classFile, temporaryDir)
        } else {
            null
        }
        //将修改结果保存到目标路径
        FileUtils.copyFile(modifyClassFile ?: classFile, destFile)
        modifyClassFile?.delete()
    }

    private fun modifyClass(classFile: File, temporaryDir: File): File {
        val byteArray = IOUtils.toByteArray(FileInputStream(classFile))
        val modifiedByteArray = modifyClass(byteArray)
        val modifiedFile = File(temporaryDir, generateClassFileName(classFile))
        if (modifiedFile.exists()) {
            modifiedFile.delete()
        }
        modifiedFile.createNewFile()
        val fos = FileOutputStream(modifiedFile)
        fos.write(modifiedByteArray)
        fos.close()
        return modifiedFile
    }

    protected fun getVisitPosition(
        argumentTypes: Array<Type>,
        parameterIndex: Int,
        isStaticMethod: Boolean
    ): Int {
        if (parameterIndex < 0 || parameterIndex >= argumentTypes.size) {
            throw Error("getVisitPosition error")
        }
        return if (parameterIndex == 0) {
            if (isStaticMethod) {
                0
            } else {
                1
            }
        } else {
            getVisitPosition(
                argumentTypes,
                parameterIndex - 1,
                isStaticMethod
            ) + argumentTypes[parameterIndex - 1].size
        }
    }

    private fun submitTask(task: () -> Unit) {
        taskList.add(Callable<Unit> {
            task()
        })
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return mutableSetOf(
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.SUB_PROJECTS,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES
        )
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getName(): String {
        return javaClass.simpleName
    }

    override fun isIncremental(): Boolean {
        return true
    }

    protected abstract fun modifyClass(byteArray: ByteArray): ByteArray

    private fun printLog(tag: String?, string: String?) {
        println("$tag,$string")
    }
}