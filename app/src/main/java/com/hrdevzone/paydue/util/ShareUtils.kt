package com.hrdevzone.paydue.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.hrdevzone.paydue.data.local.InvoiceRecord
import java.io.File

fun shareInvoicePdf(context: Context, invoice: InvoiceRecord) {
    val file = File(File(context.cacheDir, "invoices"), invoice.pdfFileName)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share invoice"))
}

fun invoicePdfFile(context: Context, invoice: InvoiceRecord): File {
    return File(File(context.cacheDir, "invoices"), invoice.pdfFileName)
}
