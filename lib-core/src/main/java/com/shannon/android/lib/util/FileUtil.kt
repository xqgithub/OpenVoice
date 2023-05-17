package com.shannon.android.lib.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.*
import java.nio.channels.FileChannel

/**
 *
 * @ClassName:      FileUtil
 * @Description:     java类作用描述
 * @Author:         czhen
 */
object FileUtil {

    private const val TAG = "FileUtil"


    fun getAppInternalFilesDir(context: Context): File {
        val path = context.applicationContext.filesDir
        Log.d(TAG, "getAppInternalFilesDir: applicationContext.filesDir = $path ")
        if (path != null) {
            return path
        }
        try {
            val info = context.applicationInfo
            val pathData = File(info.dataDir, "files")
            pathData.mkdirs()
            Log.e(TAG, "getAppInternalFilesDir: applicationInfo.dataDir = ${pathData.path} ")

            return pathData
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return File("/storage/emulated/0/Android/data/${context.packageName}/files")
    }

    /**
     * 生成一个File, 包含生成相关的目录
     *
     * @param path
     * @return
     */
    fun createFile(path: String): File {
        var file = File(path)
        file = if (file.exists()) {
            return file
        } else {
            val index = path.lastIndexOf('/')
            val dirPath = path.substring(0, index)
            val dir = File(dirPath)
            dir.mkdirs()
            File(path)
        }
        return file
    }

    //创建文件夹
    fun createDir(path: String, delete: Boolean): File? {
        val file = File(path)
        if (file.exists() && file.isDirectory) {
            if (delete) {
                deleteDir(path)
            }
        }
        file.mkdirs()
        return file
    }

    //删除文件夹
    fun deleteDir(path: String): Boolean {
        val success = deleteChildFiles(path)
        val dirs = getChildDirs(null, path, true)
        for (dir in dirs) {
            deleteDir(dir.absolutePath)
        }
        deleteFile(path)
        return success
    }

    //删除文件
    fun deleteFile(path: String): Boolean {
        if (TextUtils.isEmpty(path)) return false
        var deleteSuccess = false
        val file = File(path)
        if (file.exists()) {
            deleteSuccess = file.delete()
        }
        return deleteSuccess
    }

    /**
     * 删除文件夹下的文件
     */
    fun deleteChildFiles(path: String): Boolean {
        var success = true
        val files = getChildFiles(null, path, true)
        for (file in files) {
            if (!file.delete()) {
                success = false
            }
        }
        return success
    }

    //获取目标路径下的所有文件
    fun getChildFiles(filter: FileFilter?, path: String, includeHidden: Boolean): List<File> {
        val files: MutableList<File> = ArrayList()
        val file = File(path)
        if (file.isDirectory) {
            val fileArray = file.listFiles(filter)
            if (fileArray != null) {
                for (childFile in fileArray) {
                    if (!childFile.isDirectory) {
                        if (includeHidden) {
                            files.add(childFile)
                        } else {
                            if (!childFile.isHidden) {
                                files.add(childFile)
                            }
                        }
                    }
                }
            }
        }
        return files
    }

    //获取目标路径下的所有文件夹
    fun getChildDirs(filter: FileFilter?, path: String, includeHidden: Boolean): List<File> {
        val dirs: MutableList<File> = ArrayList()
        val file = File(path)
        if (file.isDirectory) {
            val fileArray = file.listFiles(filter)
            if (fileArray != null) {
                for (childFile in fileArray) {
                    if (includeHidden) {
                        if (childFile.isDirectory) {
                            dirs.add(childFile)
                        }
                    } else {
                        if (childFile.isDirectory && !childFile.isHidden) {
                            dirs.add(childFile)
                        }
                    }
                }
            }
        }
        return dirs
    }

    /**
     * 删除指定目录下文件及目录
     */
    fun deleteFolderFile(filePath: String, deleteThisPath: Boolean) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                val file = File(filePath)
                if (file.isDirectory) { // 处理目录
                    val files = file.listFiles()
                    if (files != null) {
                        for (i in files.indices) {
                            deleteFolderFile(files[i].absolutePath, true)
                        }
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory) { // 如果是文件，删除
                        file.delete()
                    } else { // 目录
                        if ((file.listFiles()?.size ?: 0) == 0) { // 目录下没有文件或者目录，删除
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //判断文件是否存在
    fun isFileExist(file: File): Boolean {
        return file.exists()
    }

    fun isFileExist(filePath: String?): Boolean {
        if (filePath.isNullOrEmpty()) {
            return false
        }
        val file = File(filePath)
        return file.exists()
    }


    //拷贝文件
    fun copy(source: File, target: File): Boolean {
        if (!source.exists()) {
            return false
        }
        var isSuccess = false
        var `in`: FileChannel? = null
        var out: FileChannel? = null
        var inStream: FileInputStream? = null
        var outStream: FileOutputStream? = null
        try {
            inStream = FileInputStream(source)
            outStream = FileOutputStream(target)
            `in` = inStream.channel
            out = outStream.channel
            `in`.transferTo(0, `in`.size(), out)
            isSuccess = true
        } catch (e: IOException) {
            isSuccess = false
            e.printStackTrace()
        } finally {
            close(inStream)
            close(`in`)
            close(outStream)
            close(out)
        }
        return isSuccess
    }


    fun getFileSize(path: String): Long {
        val file = File(path)
        var size = -1L
        if (file.exists() && file.isFile) {
            size = file.length()
        }
        return size
    }

    fun close(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    fun readStringFromFile(filePath: String): String? {
        val f = File(filePath)
        if (!f.exists()) {
            return null
        }
        var reader: BufferedReader? = null
        val sb = StringBuffer()
        try {
            reader = BufferedReader(FileReader(f))
            var lineTxt: String? = null
            while (reader.readLine().also { lineTxt = it } != null) {
                sb.append(
                    """
                    $lineTxt
                    
                    """.trimIndent()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            close(reader)
        }
        return sb.toString()
    }

    /***
     * 读取文件到字节数组，
     * MappedByteBuffer 可以在处理大文件时，提升性能
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    fun readFileToByteArray(filePath: String): ByteArray? {
        var fc: FileChannel? = null
        try {
            fc = RandomAccessFile(filePath, "r").channel
            val byteBuffer = fc.map(
                FileChannel.MapMode.READ_ONLY, 0,
                fc.size()
            ).load()
            val result = ByteArray(fc.size().toInt())
            if (byteBuffer.remaining() > 0) {
                byteBuffer[result, 0, byteBuffer.remaining()]
            }
            return result
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            close(fc)
        }
        return null
    }

    /***
     * 将字节数组写回文件
     * @param data
     * @param filePath
     */
    fun writeByteArrayToFile(data: ByteArray, filePath: File) {
        var raf: RandomAccessFile? = null
        try {
            raf = RandomAccessFile(filePath, "rw")
            raf.write(data)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            close(raf)
        }
    }

    /**
     * 获得文件存储路径
     *
     * @return
     */
    fun getAppExternalStorageFilePath(context: Context): String {
        var file: File? = null
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment.isExternalStorageRemovable()) { //如果外部储存可用
            file =
                context.getExternalFilesDir(null) //获得外部存储路径,默认路径为 /storage/emulated/0/Android/data
            if (file == null) {
                try {
                    file = Environment.getExternalStorageDirectory()
                } catch (e: Exception) {
                }
            }
        }
        if (file == null) {
            file = context.filesDir //直接存在/data/data里，非root手机是看不到的
        }
        return file?.path ?: ""
    }


    /**
     * 判断SDCard是否可用
     */
    fun existSDCard(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getName(filename: String): String {
        return if (filename.isNotEmpty() && filename.contains("/")) {
            val index = filename.lastIndexOf('/')
            filename.substring(index + 1)
        } else {
            ""
        }
    }

    fun copyToFile(contentResolver: ContentResolver, uri: Uri?, file: File?): Boolean {
        val from: InputStream?
        val to: FileOutputStream
        try {
            from = contentResolver.openInputStream(uri!!)
            to = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            return false
        }
        if (from == null) {
            return false
        }
        val chunk = ByteArray(16384)
        try {
            while (true) {
                val bytes = from.read(chunk, 0, chunk.size)
                if (bytes < 0) {
                    break
                }
                to.write(chunk, 0, bytes)
            }
        } catch (e: IOException) {
            return false
        }
        closeQuietly(from)
        closeQuietly(to)
        return true
    }

    private fun closeQuietly(stream: Closeable?) {
        try {
            stream?.close()
        } catch (e: IOException) {
            // intentionally unhandled
        }
    }
}