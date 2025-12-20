package com.example.delta.init

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.delta.R
import com.example.delta.formatNumberWithCommas
import com.example.delta.viewmodel.SharedViewModel
import java.io.ByteArrayOutputStream
import kotlin.math.roundToLong

data class FinancialReportRow(
    val description: String,
    val dueDate: String,
    val paymentDate: String?,
    val isPaid: Boolean,
    val amount: Double
)

class FinancialReport {

    fun buildFinancialReportPdfBytes(
        sharedViewModel: SharedViewModel,
        context: Context,
        title: String,
        startDate: String,
        endDate: String,
        rows: List<FinancialReportRow>,
        paidText: String = context.getString(R.string.payment_done),
        unpaidText: String = context.getString(R.string.debt)
    ): ByteArray {
        val doc = PdfDocument()

        val pageWidth = 600
        val pageHeight = 842
        val margin = 34f
        val contentWidth = pageWidth - 2 * margin

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 16f
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        val sidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 15f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val paidSum = rows.filter { it.isPaid }.sumOf { it.amount }
        val unpaidSum = rows.filter { !it.isPaid }.sumOf { it.amount }

        val colWidths = floatArrayOf(
            60f,   // Row
            110f,  // Description
            95f,   // DueDate
            95f,   // PaymentDate
            100f,   // Amount
            95f    // Status
        )



        val tableWidth = colWidths.sum()
        val scale = if (tableWidth > contentWidth) contentWidth / tableWidth else 1f
        for (i in colWidths.indices) colWidths[i] = colWidths[i] * scale

        fun rowHeight(): Float = 22f
        fun headerHeight(): Float = 26f

        fun drawTableHeader(canvas: android.graphics.Canvas, top: Float): Float {
            val right = pageWidth - margin
            var xRight = right
            val h = headerHeight()
            val yText = top + 17f

            val headersRtl = listOf(
                context.getString(R.string.row),
                context.getString(R.string.description),
                context.getString(R.string.due_date),
                context.getString(R.string.payment_date),
                context.getString(R.string.amount),
                context.getString(R.string.status)
            )

            for (i in headersRtl.indices) {
                val w = colWidths[i]
                val left = xRight - w
                canvas.drawRect(left, top, xRight, top + h, linePaint)
                canvas.drawText(headersRtl[i], left + w / 2f, yText, headerPaint)
                xRight -= w
            }
            return top + h
        }

        fun drawTableRow(
            canvas: android.graphics.Canvas,
            top: Float,
            idx: Int,
            row: FinancialReportRow
        ): Float {
            val right = pageWidth - margin
            var xRight = right
            val h = rowHeight()
            val yText = top + 15f

            val status = if (row.isPaid) paidText else unpaidText
            val amountText = sharedViewModel.toPersianDigits(formatNumberWithCommas(row.amount.roundToLong())) +
                    " " + context.getString(R.string.toman)


            val cellsRtl = listOf(
                sharedViewModel.toPersianDigits((idx + 1).toString()),
                row.description,
                sharedViewModel.toPersianDigits(row.dueDate),
                row.paymentDate?.let{
                    sharedViewModel.gregorianToPersian(it)
                } ?: "-",
                amountText,
                status
            )

            for (i in cellsRtl.indices) {
                val w = colWidths[i]
                val left = xRight - w
                canvas.drawRect(left, top, xRight, top + h, linePaint)
                val text = ellipsizeToWidth(cellsRtl[i], cellPaint, w - 10f)
                canvas.drawText(text, left + w / 2f, yText, cellPaint)
                xRight -= w
            }
            return top + h
        }

        fun newPage(pageIndex: Int): Pair<PdfDocument.Page, Float> {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            var y = margin

            canvas.drawText(title, pageWidth / 2f, y + 2f, titlePaint)
            y += 22f

            canvas.drawText(
                "${context.getString(R.string.date_range)}: ${sharedViewModel.toPersianDigits(startDate)} - ${sharedViewModel.toPersianDigits(endDate)}",
                pageWidth / 2f,
                y + 2f,
                centerPaint
            )
            y += 18f

            val leftX = margin
            val rightX = pageWidth - margin

            sidePaint.textAlign = Paint.Align.LEFT
            canvas.drawText(
                "${context.getString(R.string.payments)}: ${sharedViewModel.toPersianDigits(formatNumberWithCommas(paidSum.roundToLong()))} ${context.getString(R.string.toman)}",
                leftX,
                y + 2f,
                sidePaint
            )

            sidePaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                "${context.getString(R.string.debt)}: ${sharedViewModel.toPersianDigits(formatNumberWithCommas(unpaidSum.roundToLong()))} ${context.getString(R.string.toman)}",
                rightX,
                y + 2f,
                sidePaint
            )

            y += 18f

            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
            y += 10f

            y = drawTableHeader(canvas, y)
            return page to y
        }

        var pageIndex = 1
        var (page, y) = newPage(pageIndex)
        var canvas = page.canvas

        val bottomLimit = pageHeight - margin

        rows.forEachIndexed { idx, r ->
            val nextY = y + rowHeight()
            if (nextY > bottomLimit) {
                doc.finishPage(page)
                pageIndex += 1
                val res = newPage(pageIndex)
                page = res.first
                y = res.second
                canvas = page.canvas
            }
            y = drawTableRow(canvas, y, idx, r)
        }

        doc.finishPage(page)

        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    private fun ellipsizeToWidth(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) {
            end--
        }
        return if (end <= 0) ellipsis else text.substring(0, end) + ellipsis
    }




}
