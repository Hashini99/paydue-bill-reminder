package com.hrdevzone.paydue.data.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.hrdevzone.paydue.data.local.InvoiceRecord
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f

    private val NAVY = Color.rgb(0x1B, 0x36, 0x5D)
    private val GRAY_TEXT = Color.rgb(0x4A, 0x4A, 0x4A)
    private val LIGHT_GRAY = Color.rgb(0xF4, 0xF7, 0xFA)

    fun generate(
        context: Context,
        invoice: InvoiceRecord,
        currencySymbol: String
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        draw(canvas, invoice, currencySymbol)

        document.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, invoice.pdfFileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun draw(canvas: Canvas, invoice: InvoiceRecord, currencySymbol: String) {
        var y = MARGIN

        // Header band
        val headerPaint = Paint().apply { color = NAVY; isAntiAlias = true }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 120f, headerPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("INVOICE", MARGIN, 65f, titlePaint)

        val subPaint = Paint().apply {
            color = Color.rgb(0xE3, 0xEA, 0xF2)
            textSize = 14f
            isAntiAlias = true
        }
        canvas.drawText(invoice.invoiceNumber, MARGIN, 90f, subPaint)

        y = 160f

        val labelPaint = Paint().apply {
            color = GRAY_TEXT
            textSize = 11f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // From / To block
        canvas.drawText("FROM", MARGIN, y, labelPaint)
        canvas.drawText("BILL TO", PAGE_WIDTH / 2f, y, labelPaint)
        y += 20f
        canvas.drawText(invoice.fromName.ifBlank { "Your name" }, MARGIN, y, valuePaint)
        canvas.drawText(invoice.clientName.ifBlank { "Client" }, PAGE_WIDTH / 2f, y, valuePaint)
        y += 40f

        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        val issueDate = LocalDate.ofEpochDay(invoice.issueDateEpochDay).format(dateFormatter)

        canvas.drawText("DATE", MARGIN, y, labelPaint)
        y += 20f
        canvas.drawText(issueDate, MARGIN, y, valuePaint)
        y += 40f

        // Table header
        val tableTop = y
        val tablePaint = Paint().apply { color = LIGHT_GRAY; isAntiAlias = true }
        canvas.drawRect(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop + 36f, tablePaint)

        val headerTextPaint = Paint().apply {
            color = NAVY
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("DESCRIPTION", MARGIN + 12f, tableTop + 23f, headerTextPaint)
        canvas.drawText("AMOUNT", PAGE_WIDTH - MARGIN - 90f, tableTop + 23f, headerTextPaint)

        y = tableTop + 36f
        val rowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            isAntiAlias = true
        }
        val description = invoice.note.ifBlank { "Payment for services rendered" }
        val wrapped = wrapText(description, rowPaint, PAGE_WIDTH - (2 * MARGIN) - 130f)
        var rowY = y + 26f
        wrapped.forEach { line ->
            canvas.drawText(line, MARGIN + 12f, rowY, rowPaint)
            rowY += 18f
        }
        canvas.drawText(
            "$currencySymbol${"%,.2f".format(invoice.amount)}",
            PAGE_WIDTH - MARGIN - 90f,
            y + 26f,
            rowPaint
        )

        val rowBottom = maxOf(rowY + 8f, y + 50f)
        val border = Paint().apply {
            color = Color.rgb(0xD0, 0xD7, 0xDE)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, rowBottom), border)

        y = rowBottom + 30f

        // Total block
        val totalLabelPaint = Paint().apply {
            color = GRAY_TEXT
            textSize = 13f
            isAntiAlias = true
        }
        val totalValuePaint = Paint().apply {
            color = NAVY
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("TOTAL DUE", PAGE_WIDTH - MARGIN - 200f, y, totalLabelPaint)
        y += 30f
        canvas.drawText(
            "$currencySymbol${"%,.2f".format(invoice.amount)}",
            PAGE_WIDTH - MARGIN - 200f,
            y,
            totalValuePaint
        )

        y += 50f

        if (invoice.paymentNote.isNotBlank()) {
            canvas.drawText("PAYMENT DETAILS", MARGIN, y, labelPaint)
            y += 20f
            val paymentLines = wrapText(invoice.paymentNote, rowPaint, PAGE_WIDTH - (2 * MARGIN))
            paymentLines.forEach { line ->
                canvas.drawText(line, MARGIN, y, rowPaint)
                y += 18f
            }
        }

        val footerPaint = Paint().apply {
            color = Color.rgb(0x9A, 0x9A, 0x9A)
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText(
            "Generated with PayDue Offline — no cloud, no login.",
            MARGIN,
            PAGE_HEIGHT - 30f,
            footerPaint
        )
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "${current} $word"
            if (paint.measureText(candidate) > maxWidth && current.isNotEmpty()) {
                lines.add(current.toString())
                current = StringBuilder(word)
            } else {
                current = StringBuilder(candidate)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }
}
