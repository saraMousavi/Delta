package com.example.delta.init

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.delta.R
import com.example.delta.data.entity.Debts
import com.example.delta.data.entity.Units
import com.example.delta.formatNumberWithCommas
import java.io.ByteArrayOutputStream
import kotlin.math.roundToLong

class DashboardFinancialReport {

    data class Section(
        val title: String,
        val rows: List<UnitRow>
    )

    data class UnitRow(
        val unitNumber: String,
        val debt: Double,
        val pay: Double
    )

    fun buildDashboardPdfBytes(
        context: Context,
        buildingName: String,
        startDate: String,
        endDate: String,
        units: List<Units>,
        debts: List<Debts>,
        pays: List<Debts>
    ): ByteArray {
        val doc = PdfDocument()

        val pageWidth = 600
        val pageHeight = 842
        val margin = 34f
        val contentWidth = pageWidth - 2 * margin

        val titleText = "${context.getString(R.string.financial_report)} $buildingName"

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        val sidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 13f
        }
        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 14f
            textAlign = Paint.Align.RIGHT
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val unitNumberById = units.associate { it.unitId to it.unitNumber }

        fun unitLabel(unitId: Long?): String {
            val id = unitId ?: 0L
            return unitNumberById[id]?.ifBlank { id.toString() }
                ?: context.getString(R.string.other)
        }

        fun sectionFor(sectionTitle: String): Section {
            val debtByUnit = mutableMapOf<String, Double>()
            val payByUnit = mutableMapOf<String, Double>()

            debts.forEach { d ->
                if (d.description.trim() != sectionTitle) return@forEach
                val u = unitLabel(d.unitId)
                debtByUnit[u] = (debtByUnit[u] ?: 0.0) + d.amount
            }

            pays.forEach { p ->
                if (p.description.trim() != sectionTitle) return@forEach
                val u = unitLabel(p.unitId)
                payByUnit[u] = (payByUnit[u] ?: 0.0) + p.amount
            }

            val allUnits = (debtByUnit.keys + payByUnit.keys).distinct()
            val rows = allUnits
                .map { u ->
                    UnitRow(
                        unitNumber = u,
                        debt = debtByUnit[u] ?: 0.0,
                        pay = payByUnit[u] ?: 0.0
                    )
                }
                .sortedBy { it.unitNumber.toIntOrNull() ?: Int.MAX_VALUE }

            return Section(title = sectionTitle, rows = rows)
        }

        val fixedTitles = listOf("شارژ عمرانی", "شارژ جاری")
        val sections = fixedTitles.map { sectionFor(it) }.filter { it.rows.isNotEmpty() }

        val paidSum = pays.sumOf { it.amount }
        val unpaidSum = debts.sumOf { it.amount }

        val colWidths = floatArrayOf(
            55f,   // Row
            130f,  // Unit
            170f,  // Debt
            170f   // Pay
        )

        val tableWidth = colWidths.sum()
        val scale = if (tableWidth > contentWidth) contentWidth / tableWidth else 1f
        for (i in colWidths.indices) colWidths[i] *= scale

        fun rowHeight(): Float = 26f
        fun headerHeight(): Float = 28f

        fun drawRowRtl(
            canvas: android.graphics.Canvas,
            top: Float,
            cells: List<String>,
            paint: Paint
        ): Float {
            val right = pageWidth - margin
            var xRight = right
            val h = if (paint === headerPaint) headerHeight() else rowHeight()
            val yText = top + 18f

            for (i in cells.indices) {
                val w = colWidths[i]
                val left = xRight - w
                canvas.drawRect(left, top, xRight, top + h, linePaint)
                val text = ellipsizeToWidth(cells[i], paint, w - 10f)
                canvas.drawText(text, left + w / 2f, yText, paint)
                xRight -= w
            }
            return top + h
        }

        fun newPage(pageIndex: Int): Pair<PdfDocument.Page, Float> {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            var y = margin
            canvas.drawText(titleText, pageWidth / 2f, y + 2f, titlePaint)
            y += 26f

            canvas.drawText(
                "${context.getString(R.string.date_range)}: ${toPersianDigits(startDate)} - ${toPersianDigits(endDate)}",
                pageWidth / 2f,
                y + 2f,
                subPaint
            )
            y += 18f

            val leftX = margin
            val rightX = pageWidth - margin

            sidePaint.textAlign = Paint.Align.LEFT
            canvas.drawText(
                "${context.getString(R.string.payments)}: ${toPersianDigits(formatNumberWithCommas(paidSum.roundToLong()))} ${context.getString(R.string.toman)}",
                leftX,
                y + 2f,
                sidePaint
            )

            sidePaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                "${context.getString(R.string.debt)}: ${toPersianDigits(formatNumberWithCommas(unpaidSum.roundToLong()))} ${context.getString(R.string.toman)}",
                rightX,
                y + 2f,
                sidePaint
            )

            y += 18f
            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
            y += 12f

            return page to y
        }

        var pageIndex = 1
        var (page, y) = newPage(pageIndex)
        var canvas = page.canvas
        val bottomLimit = pageHeight - margin

        if (sections.isEmpty()) {
            canvas.drawText(
                context.getString(R.string.no_data_recorded),
                pageWidth / 2f,
                y + 24f,
                subPaint
            )
            doc.finishPage(page)
        } else {
            sections.forEach { section ->
                val titleHeight = 22f
                if (y + titleHeight + headerHeight() > bottomLimit) {
                    doc.finishPage(page)
                    pageIndex += 1
                    val res = newPage(pageIndex)
                    page = res.first
                    y = res.second
                    canvas = page.canvas
                }

                canvas.drawText(section.title, pageWidth - margin, y + 2f, sectionTitlePaint)
                y += titleHeight

                val headerCells = listOf(
                    context.getString(R.string.row),
                    context.getString(R.string.unit),
                    context.getString(R.string.debt),
                    context.getString(R.string.payments)
                )
                y = drawRowRtl(canvas, y, headerCells, headerPaint)

                section.rows.forEachIndexed { idx, r ->
                    if (y + rowHeight() > bottomLimit) {
                        doc.finishPage(page)
                        pageIndex += 1
                        val res = newPage(pageIndex)
                        page = res.first
                        y = res.second
                        canvas = page.canvas

                        canvas.drawText(section.title, pageWidth - margin, y + 2f, sectionTitlePaint)
                        y += titleHeight
                        y = drawRowRtl(canvas, y, headerCells, headerPaint)
                    }

                    val rowCells = listOf(
                        toPersianDigits((idx + 1).toString()),
                        toPersianDigits(r.unitNumber),
                        "${toPersianDigits(formatNumberWithCommas(r.debt.roundToLong()))} ${context.getString(R.string.toman)}",
                        "${toPersianDigits(formatNumberWithCommas(r.pay.roundToLong()))} ${context.getString(R.string.toman)}"
                    )
                    y = drawRowRtl(canvas, y, rowCells, cellPaint)
                }

                y += 10f
            }

            doc.finishPage(page)
        }

        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    private fun ellipsizeToWidth(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + ellipsis) > maxWidth) end--
        return if (end <= 0) ellipsis else text.substring(0, end) + ellipsis
    }

    private fun toPersianDigits(input: String): String {
        val en = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val map = en.zip(fa).toMap()
        return buildString { input.forEach { append(map[it] ?: it) } }
    }
}
