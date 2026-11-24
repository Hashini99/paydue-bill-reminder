package com.hrdevzone.paydue.data.backup

import android.content.Context
import android.net.Uri
import com.hrdevzone.paydue.data.local.Due
import java.io.BufferedReader
import java.io.InputStreamReader

class BackupManager(private val context: Context) {

    fun exportJson(uri: Uri, dues: List<Due>) {
        val json = BackupCodec.encode(dues)
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray())
        }
    }

    fun exportCsv(uri: Uri, dues: List<Due>) {
        val csv = BackupCodec.encodeCsv(dues)
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(csv.toByteArray())
        }
    }

    fun importJson(uri: Uri): List<Due> {
        val text = context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        } ?: throw IllegalStateException("Could not read the selected file")
        return BackupCodec.decode(text)
    }
}
