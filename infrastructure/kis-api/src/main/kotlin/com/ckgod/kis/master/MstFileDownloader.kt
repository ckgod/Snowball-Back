package com.ckgod.kis.master

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import java.io.File
import java.util.zip.ZipInputStream

class MstFileDownloader(private val client: HttpClient) {

    suspend fun downloadAndUnzip(url: String, outputDir: File): File {
        val zipBytes = client.get(url).readBytes()

        outputDir.mkdirs()
        val zipFile = File(outputDir, "temp.zip")
        zipFile.writeBytes(zipBytes)

        val mstFile = unzip(zipFile, outputDir)
        zipFile.delete()

        return mstFile
    }

    private fun unzip(zipFile: File, outputDir: File): File {
        var mstFile: File? = null

        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".mst")) {
                    val file = File(outputDir, entry.name)
                    file.outputStream().use { output ->
                        zis.copyTo(output)
                    }
                    mstFile = file
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        return mstFile ?: error("No .mst file found in zip")
    }
}