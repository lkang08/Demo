package com.bilin.monitor.plugin.bitmap

import com.android.build.api.transform.TransformInvocation
import com.bilin.huijiao.plugin.BaseTransform
import com.bilin.monitor.plugin.utils.CommonUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes

class BitmapMonitorTransform(val bitmapMonitoryConfig: BitmapMonitoryConfig) : BaseTransform() {
    override val TAG = "BitmapMonitorTransform"

    private companion object {
        val IMAGE_PARENTS =
            arrayListOf("android/widget/ImageView", "androidx/appcompat/widget/AppCompatImageView")
    }

    override fun transform(transformInvocation: TransformInvocation) {
        CommonUtils.printLog(
            TAG, "transform begin"
        )
        super.transform(transformInvocation)
    }

    override fun modifyClass(byteArray: ByteArray): ByteArray {
        val classReader = ClassReader(byteArray)
        val className = classReader.className
        val superName = classReader.superName
        val isEmpty = bitmapMonitoryConfig.monitorImageViewClass.isNullOrEmpty()
        val isLegal = className != bitmapMonitoryConfig.monitorImageViewClass
            && className != bitmapMonitoryConfig.monitorAppCompatImageClass
        return if (isLegal &&
            !isEmpty &&
            className != bitmapMonitoryConfig.monitorImageViewClass
            && IMAGE_PARENTS.contains(superName)
        ) {
            val desClass = when (superName) {
                IMAGE_PARENTS.first() -> bitmapMonitoryConfig.monitorImageViewClass
                else -> bitmapMonitoryConfig.monitorAppCompatImageClass
            }
            CommonUtils.printLog(
                TAG, "className: $className superName: $superName,$isEmpty,$desClass"
            )
            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val classVisitor = object : ClassVisitor(Opcodes.ASM7, classWriter) {
                override fun visit(
                    version: Int,
                    access: Int,
                    name: String?,
                    signature: String?,
                    superName: String?,
                    interfaces: Array<out String>?
                ) {
                    super.visit(
                        version,
                        access,
                        name,
                        signature,
                        desClass,
                        interfaces
                    )
                }
            }
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            classWriter.toByteArray()
        } else {
            byteArray
        }
    }
}