package com.dayandisli.calculator.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.dayandisli.calculator.R
import com.dayandisli.calculator.domain.GearCombination
import com.dayandisli.calculator.domain.HelicalGearResult
import com.dayandisli.calculator.domain.Machine
import com.dayandisli.calculator.domain.ReishauerHelisCombination
import com.dayandisli.calculator.domain.ReishauerResult
import com.dayandisli.calculator.domain.SpurGearResult
import com.dayandisli.calculator.domain.toDmscs
import com.dayandisli.calculator.domain.toDmscsSigned
import com.dayandisli.calculator.domain.toFixedJs
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

/**
 * Draws production-receipt PDFs as a polished A4 industrial engineering document, using Android's
 * native PdfDocument/Canvas (no third-party PDF library). Roboto (res/font/roboto_regular.ttf,
 * roboto_bold.ttf) provides Turkish, Greek and math glyph coverage (ç, ş, ı, ğ, ü, ö, α, β, °, ø).
 *
 * This file is pure presentation: every calculation, ordering and filtering of the values it
 * draws comes from the `domain` package and the calling receipt screens unchanged.
 */
object PdfReceiptGenerator {
    private const val PAGE_WIDTH = 595 // A4 at 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val FOOTER_HEIGHT = 34f
    private const val CONTENT_BOTTOM = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT

    // Module accent colors.
    private val BLUE_ACCENT = Color.rgb(37, 99, 235)
    private val PURPLE_ACCENT = Color.rgb(147, 51, 234)

    // Header band / neutral palette.
    private val NAVY = Color.rgb(15, 23, 42)
    private val NAVY_LIGHT = Color.rgb(30, 41, 59)
    private val INK = Color.rgb(15, 23, 42)
    private val MUTED = Color.rgb(100, 116, 139)
    private val LINE = Color.rgb(203, 213, 225)
    private val PANEL_BG = Color.rgb(248, 250, 252)

    private fun regularTypeface(context: Context) = ResourcesCompat.getFont(context, R.font.roboto_regular) ?: Typeface.DEFAULT
    private fun boldTypeface(context: Context) = ResourcesCompat.getFont(context, R.font.roboto_bold) ?: Typeface.DEFAULT_BOLD

    // ------------------------------------------------------------------------------------------
    // Public entry points
    // ------------------------------------------------------------------------------------------

    fun renderSpurReceiptPdf(
        context: Context,
        machine: Machine,
        result: SpurGearResult,
        combinations: List<GearCombination>,
    ): ByteArray {
        val doc = PdfDocument()
        val painter = ReceiptPainter(
            context = context,
            doc = doc,
            accentColor = BLUE_ACCENT,
            moduleLabel = "DÜZ DİŞLİ",
            machineName = machine.name,
        )
        painter.run {
            drawDocumentHeader()

            drawHighlightStrip(
                listOf(
                    Triple("MODÜL", result.mn.toFixedJs(4), "mn"),
                    Triple("DİŞ SAYISI", result.z.toString(), "Z"),
                    Triple("KAVRAMA AÇISI", result.alphaDeg.toFixedJs(2), "°"),
                    Triple("ÇAP", result.diameter.toFixedJs(2), "mm"),
                ),
            )

            drawSectionTitle("Hesaplama Parametreleri")
            drawKeyValueTable(
                listOf(
                    Triple("Modül (mn)", result.mn.toFixedJs(4), "mm"),
                    Triple("Diş Sayısı (Z)", result.z.toString(), ""),
                    Triple("Kavrama Açısı (α)", result.alphaDeg.toFixedJs(2), "°"),
                    Triple("Çap (ø)", result.diameter.toFixedJs(2), "mm"),
                    Triple("Taksimat Oranı", result.taksimatRatio.toFixedJs(6), ""),
                ),
            )

            drawSectionTitle("Wk Ölçüm Değerleri")
            drawWkCards(
                listOf(
                    Triple(result.kFloor, result.wkFloor, "mm"),
                    Triple(result.kCeil, result.wkCeil, "mm"),
                ),
            )

            drawSectionTitle("Taksimat Çark Kombinasyonları")
            drawCombinationCards(combinations, targetRatio = result.taksimatRatio, sectionLabel = "Taksimat Çark Kombinasyonları")
        }
        return doc.toByteArray()
    }

    fun renderHelicalReceiptPdf(
        context: Context,
        machine: Machine,
        result: HelicalGearResult,
        taksimatCombinations: List<GearCombination>,
        helicalCombinations: List<GearCombination>,
    ): ByteArray {
        val doc = PdfDocument()
        val painter = ReceiptPainter(
            context = context,
            doc = doc,
            accentColor = PURPLE_ACCENT,
            moduleLabel = "HELİS DİŞLİ",
            machineName = machine.name,
        )
        painter.run {
            drawDocumentHeader()

            drawHighlightStrip(
                listOf(
                    Triple("MODÜL", result.mn.toFixedJs(4), "mn"),
                    Triple("DİŞ SAYISI", result.z.toString(), "Z"),
                    Triple("KAVRAMA AÇISI", result.alphaDeg.toFixedJs(2), "°"),
                    Triple("HELİS AÇISI", "${result.betaDeg.toFixedJs(2)} (${result.helixDirection.label})", "°"),
                    Triple("ÇAP", result.diameter.toFixedJs(2), "mm"),
                ),
            )

            drawSectionTitle("Hesaplama Parametreleri")
            drawKeyValueTable(
                listOf(
                    Triple("Modül (mn)", result.mn.toFixedJs(4), "mm"),
                    Triple("Diş Sayısı (Z)", result.z.toString(), ""),
                    Triple("Kavrama Açısı (α)", result.alphaDeg.toFixedJs(2), "°"),
                    Triple("Helis Açısı (β)", result.betaDeg.toFixedJs(2), "°"),
                    Triple("Helis Yönü", result.helixDirection.label, ""),
                    Triple("Çap (ø)", result.diameter.toFixedJs(2), "mm"),
                    Triple("Taksimat Oranı", result.taksimatRatio.toFixedJs(6), ""),
                    Triple("Helis Çark Oranı", result.helicalGearRatio.toFixedJs(6), ""),
                ),
            )

            drawSectionTitle("Wk Ölçüm Değerleri")
            drawWkCards(
                listOf(
                    Triple(result.kFloor, result.wkFloor, "mm"),
                    Triple(result.kCeil, result.wkCeil, "mm"),
                ),
            )

            drawSectionTitle("Taksimat Çark Kombinasyonları")
            drawCombinationCards(taksimatCombinations, targetRatio = result.taksimatRatio, sectionLabel = "Taksimat Çark Kombinasyonları")

            drawSectionTitle("Helis Çark Kombinasyonları")
            drawCombinationCards(helicalCombinations, targetRatio = result.helicalGearRatio, sectionLabel = "Helis Çark Kombinasyonları")
        }
        return doc.toByteArray()
    }

    fun renderReishauerReceiptPdf(
        context: Context,
        machine: Machine,
        result: ReishauerResult,
        taksimatCombinations: List<GearCombination>,
        helisCombinations: List<ReishauerHelisCombination>,
    ): ByteArray {
        val doc = PdfDocument()
        val painter = ReceiptPainter(
            context = context,
            doc = doc,
            accentColor = PURPLE_ACCENT,
            moduleLabel = "REISHAUER TAŞLAMA",
            machineName = machine.name,
        )
        painter.run {
            drawDocumentHeader()

            drawHighlightStrip(
                listOf(
                    Triple("MODÜL", result.mn.toFixedJs(4), "mn"),
                    Triple("DİŞ SAYISI", result.z.toString(), "Z"),
                    Triple("KAVRAMA AÇISI", result.alphaRaw, ""),
                    Triple("HELİS AÇISI", "${result.betaRaw} (${result.helixDirection.label})", ""),
                    Triple("ÇAP", result.diameter.toFixedJs(2), "mm"),
                ),
            )

            drawSectionTitle("Hesaplama Parametreleri")
            drawKeyValueTable(
                listOf(
                    Triple("Modül (mn)", result.mn.toFixedJs(4), "mm"),
                    Triple("Diş Sayısı (Z)", result.z.toString(), ""),
                    Triple("Kavrama Açısı (α)", result.alphaRaw, ""),
                    Triple("Helis Açısı (β)", result.betaRaw, ""),
                    Triple("Helis Yönü", result.helixDirection.label, ""),
                    Triple("Çap (ø)", result.diameter.toFixedJs(2), "mm"),
                    Triple("Nominal Çap", result.nominalDiameter.toFixedJs(2), "mm"),
                    Triple("Taksimat Oranı", result.taksimatRatio.toFixedJs(6), ""),
                    Triple("Helis Çark Oranı", result.helixGearRatio.toFixedJs(9), ""),
                ),
            )

            drawSectionTitle("Reishauer Wk Ölçüm Değerleri (k1 / k2 / k3)")
            drawWkCards(
                listOf(
                    Triple(result.k1, result.wk1, "mm"),
                    Triple(result.k2, result.wk2, "mm"),
                    Triple(result.k3, result.wk3, "mm"),
                ),
                reishauerStyle = true,
            )

            drawSectionTitle("Taksimat Çark Kombinasyonları")
            drawCombinationCards(taksimatCombinations, targetRatio = result.taksimatRatio, sectionLabel = "Taksimat Çark Kombinasyonları")

            drawSectionTitle("Helis Çark Kombinasyonları")
            drawReishauerCombinationCards(helisCombinations, targetRatio = result.helixGearRatioFullPrecision, sectionLabel = "Helis Çark Kombinasyonları")
        }
        return doc.toByteArray()
    }

    // ------------------------------------------------------------------------------------------
    // Export plumbing (unchanged)
    // ------------------------------------------------------------------------------------------

    private fun comboRow(c: GearCombination): List<String> =
        listOf(c.a.toString(), c.b.toString(), c.c?.toString() ?: "-", c.d?.toString() ?: "-")

    private fun PdfDocument.toByteArray(): ByteArray {
        val out = ByteArrayOutputStream()
        writeTo(out)
        close()
        return out.toByteArray()
    }

    /** Writes [bytes] to the app cache and returns a FileProvider content Uri, then opens the
     * system share sheet (which includes "Save to..." targets, covering the SAF save path too). */
    fun shareBytes(context: Context, bytes: ByteArray, fileName: String) {
        val uri = writeToCache(context, bytes, fileName)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Reçeteyi Paylaş / Kaydet")
        if (context !is android.app.Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun writeToCache(context: Context, bytes: ByteArray, fileName: String): android.net.Uri {
        val pdfDir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val file = File(pdfDir, "$fileName.pdf")
        file.writeBytes(bytes)
        return FileProvider.getUriForFile(context, "com.dayandisli.calculator.fileprovider", file)
    }

    /** Prints via Android's PrintManager without requiring broad storage permissions. */
    fun printBytes(context: Context, bytes: ByteArray, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val adapter = object : android.print.PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback,
                extras: android.os.Bundle?,
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                val info = android.print.PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback.onLayoutFinished(info, oldAttributes != newAttributes)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: android.os.ParcelFileDescriptor,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback,
            ) {
                try {
                    java.io.FileOutputStream(destination.fileDescriptor).use { it.write(bytes) }
                    callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: java.io.IOException) {
                    callback.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, adapter, android.print.PrintAttributes.Builder().build())
    }

    // ------------------------------------------------------------------------------------------
    // Painter
    // ------------------------------------------------------------------------------------------

    /** Handles drawing/pagination for a single receipt document. Android's [PdfDocument] only
     * allows one open (unfinished) page at a time, so the footer - which needs the final page
     * count - cannot be deferred by keeping every page open. Instead [run] draws the content twice
     * (see its doc comment): a silent measuring pass to learn the page count, then a real pass. */
    private class ReceiptPainter(
        context: Context,
        private val doc: PdfDocument,
        private val accentColor: Int,
        private val moduleLabel: String,
        private val machineName: String,
    ) {
        private val regular = regularTypeface(context)
        private val bold = boldTypeface(context)
        private val italic = Typeface.create(regular, Typeface.ITALIC)
        private val logo: Bitmap? = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.dayan_logo_header)
        }.getOrNull()

        private val docNumber = "DY-${System.currentTimeMillis().toString().takeLast(8)}"
        private val generatedAt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date())

        // Android's PdfDocument only ever allows a single open (unfinished) page at a time -
        // calling startPage() again before finishPage() throws "Current page not finished!". Since
        // the footer needs the final page count (for "Sayfa N / total"), content is drawn twice:
        // once in [measuring] mode (all draw calls land on a throwaway canvas, only pagination
        // math runs) to discover the total page count, then for real with that total already known,
        // so the footer can be stamped immediately before each real page is finished.
        private var measuring = false
        private var totalPages = 0
        private var pageIndex = 0
        private var currentRealPage: PdfDocument.Page? = null
        private val dummyBitmap = Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        private val dummyCanvas = Canvas(dummyBitmap)
        private var canvas: Canvas? = null
        private var y = MARGIN
        private var currentSectionLabel: String? = null

        /** Runs [content] twice: a silent measuring pass to determine the total page count, then a
         * real pass that draws into the [doc], stamping a correct "Sayfa N / total" footer onto
         * each page as it is finished. Call exactly once per document. */
        fun run(content: ReceiptPainter.() -> Unit) {
            measuring = true
            pageIndex = 0
            currentSectionLabel = null
            content()
            totalPages = pageIndex

            measuring = false
            pageIndex = 0
            currentSectionLabel = null
            content()
            finishCurrentRealPageWithFooter()
        }

        private fun finishCurrentRealPageWithFooter() {
            val page = currentRealPage ?: return
            drawFooter(page.canvas, pageIndex)
            doc.finishPage(page)
            currentRealPage = null
        }

        private fun startNewPage() {
            if (!measuring) {
                finishCurrentRealPageWithFooter()
            }
            pageIndex += 1
            if (measuring) {
                canvas = dummyCanvas
            } else {
                val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex).create()
                val page = doc.startPage(info)
                currentRealPage = page
                canvas = page.canvas
            }
            y = MARGIN
        }

        private fun drawFooter(cv: Canvas, pageNumber: Int) {
            val footerTop = PAGE_HEIGHT - MARGIN - FOOTER_HEIGHT + 10f
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LINE; strokeWidth = 0.75f }
            cv.drawLine(MARGIN, footerTop, PAGE_WIDTH - MARGIN, footerTop, linePaint)

            val leftPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = bold; textSize = 8.5f; color = MUTED }
            cv.drawText("DAYAN Dişli Sanayi", MARGIN, footerTop + 14f, leftPaint)
            val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = regular; textSize = 8f; color = MUTED }
            cv.drawText("Bu belge hesaplama çıktısıdır.  •  Oluşturulma: $generatedAt", MARGIN, footerTop + 25f, notePaint)

            val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 8.5f; color = MUTED; textAlign = Paint.Align.RIGHT
            }
            val totalLabel = if (totalPages > 0) totalPages else pageNumber
            cv.drawText("Sayfa $pageNumber / $totalLabel", PAGE_WIDTH - MARGIN, footerTop + 14f, pagePaint)
        }

        /** Starts the very first page and draws the full header block (logo, title, machine,
         * date, doc number, accent rule). Call once per document. */
        fun drawDocumentHeader() {
            startNewPage()
            val cv = canvas!!
            val headerHeight = 92f

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = NAVY; style = Paint.Style.FILL }
            cv.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), headerHeight, bgPaint)

            // Logo, left-aligned, preserving aspect ratio.
            var textStartX = MARGIN
            logo?.let { bmp ->
                val targetH = 30f
                val scale = targetH / bmp.height.toFloat()
                val targetW = bmp.width * scale
                val rect = RectF(MARGIN, 18f, MARGIN + targetW, 18f + targetH)
                cv.drawBitmap(bmp, null, rect, null)
                textStartX = MARGIN
            }
            val logoBottom = if (logo != null) 18f + 30f + 8f else 18f

            // Title, right side.
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 17f; color = Color.WHITE; textAlign = Paint.Align.RIGHT
            }
            cv.drawText("ÜRETİM REÇETESİ", PAGE_WIDTH - MARGIN, 30f, titlePaint)
            val modulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 10.5f; textAlign = Paint.Align.RIGHT
                color = accentColor
            }
            cv.drawText(moduleLabel, PAGE_WIDTH - MARGIN, 44f, modulePaint)

            // Machine / date / doc number line under logo.
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = regular; textSize = 9.5f; color = Color.rgb(203, 213, 225)
            }
            cv.drawText("Makine: $machineName", textStartX, logoBottom, metaPaint)
            val metaPaint2 = Paint(metaPaint)
            cv.drawText("Tarih: $generatedAt", textStartX, logoBottom + 13f, metaPaint2)
            val docNoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 9.5f; color = Color.WHITE; textAlign = Paint.Align.RIGHT
            }
            cv.drawText("Belge No: $docNumber", PAGE_WIDTH - MARGIN, logoBottom + 13f, docNoPaint)

            // Accent rule under header band.
            val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor; style = Paint.Style.FILL }
            cv.drawRect(0f, headerHeight, PAGE_WIDTH.toFloat(), headerHeight + 3f, accentPaint)

            y = headerHeight + 3f + 18f
        }

        /** Slim continuation header drawn at the top of pages after the first, so a table split
         * across a page break stays legible. */
        private fun drawContinuationHeader() {
            val cv = canvas!!
            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = NAVY_LIGHT; style = Paint.Style.FILL }
            cv.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 26f, barPaint)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 9.5f; color = Color.WHITE
            }
            val label = currentSectionLabel?.let { "$it (devam)" } ?: "DAYAN Dişli Sanayi (devam)"
            cv.drawText(label, MARGIN, 17f, textPaint)
            val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = regular; textSize = 9f; color = Color.rgb(203, 213, 225); textAlign = Paint.Align.RIGHT
            }
            cv.drawText(moduleLabel, PAGE_WIDTH - MARGIN, 17f, rightPaint)
            val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor; style = Paint.Style.FILL }
            cv.drawRect(0f, 26f, PAGE_WIDTH.toFloat(), 28f, accentPaint)
            y = 28f + 16f
        }

        private fun newPage() {
            startNewPage()
            drawContinuationHeader()
        }

        private fun ensureSpace(height: Float) {
            if (y + height > CONTENT_BOTTOM) {
                newPage()
            }
        }

        fun drawSectionTitle(text: String) {
            currentSectionLabel = text
            ensureSpace(30f)
            y += 6f
            val cv = canvas!!
            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor; style = Paint.Style.FILL }
            cv.drawRect(MARGIN, y, MARGIN + 4f, y + 15f, barPaint)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 12.5f; color = INK
            }
            cv.drawText(text, MARGIN + 10f, y + 11.5f, textPaint)
            y += 24f
        }

        /** Large, scannable highlight strip of the most operationally important values. */
        fun drawHighlightStrip(items: List<Triple<String, String, String>>) {
            ensureSpace(56f)
            val cv = canvas!!
            val width = PAGE_WIDTH - 2 * MARGIN
            val colWidth = width / items.size
            val top = y
            val height = 50f

            val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PANEL_BG; style = Paint.Style.FILL }
            cv.drawRect(MARGIN, top, MARGIN + width, top + height, panelPaint)
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1f; color = LINE }
            cv.drawRect(MARGIN, top, MARGIN + width, top + height, borderPaint)

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 7.5f; color = MUTED; textAlign = Paint.Align.CENTER
            }
            val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = regular; textSize = 8.5f; color = MUTED
            }
            val maxValueWidth = colWidth - 12f

            items.forEachIndexed { i, (label, value, unit) ->
                val cx = MARGIN + colWidth * i + colWidth / 2f
                cv.drawText(label, cx, top + 15f, labelPaint)

                // Shrink the value font until "value + unit" fits inside this column (long
                // Reishauer D,M,S,CS angle strings would otherwise overlap the next column).
                var valueSize = 15f
                val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = bold; color = accentColor; textAlign = Paint.Align.LEFT
                }
                val unitAtSize = Paint(unitPaint)
                fun totalWidth(size: Float): Float {
                    valuePaint.textSize = size
                    unitAtSize.textSize = size * (8.5f / 15f)
                    val vw = valuePaint.measureText(value)
                    val uw = if (unit.isNotEmpty()) unitAtSize.measureText(" $unit") else 0f
                    return vw + uw
                }
                while (valueSize > 8f && totalWidth(valueSize) > maxValueWidth) {
                    valueSize -= 0.5f
                }
                valuePaint.textSize = valueSize
                unitAtSize.textSize = max(valueSize * (8.5f / 15f), 6.5f)
                val valueWidth = valuePaint.measureText(value)
                val unitWidth = if (unit.isNotEmpty()) unitAtSize.measureText(" $unit") else 0f
                val startX = cx - (valueWidth + unitWidth) / 2f
                cv.drawText(value, startX, top + 37f, valuePaint)
                if (unit.isNotEmpty()) {
                    cv.drawText(" $unit", startX + valueWidth, top + 37f, unitAtSize)
                }
                if (i > 0) {
                    val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LINE; strokeWidth = 1f }
                    cv.drawLine(MARGIN + colWidth * i, top + 6f, MARGIN + colWidth * i, top + height - 6f, dividerPaint)
                }
            }
            y = top + height + 20f
        }

        /** Two-column technical key/value table: label muted, value darker/bolder with a quiet
         * unit suffix, right-aligned numeric value column. */
        fun drawKeyValueTable(rows: List<Triple<String, String, String>>) {
            val width = PAGE_WIDTH - 2 * MARGIN
            val rowHeight = 20f
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = regular; textSize = 10f; color = MUTED }
            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 10.5f; color = INK; textAlign = Paint.Align.RIGHT
            }
            val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = regular; textSize = 8.5f; color = MUTED }
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LINE; strokeWidth = 0.75f }

            rows.forEachIndexed { index, (label, value, unit) ->
                ensureSpace(rowHeight)
                val cv = canvas!!
                val baseline = y + rowHeight / 2f + 3.5f
                cv.drawText(label, MARGIN, baseline, labelPaint)
                val rightEdge = MARGIN + width
                val unitWidth = if (unit.isNotEmpty()) unitPaint.measureText(" $unit") else 0f
                cv.drawText(value, rightEdge - unitWidth, baseline, valuePaint)
                if (unit.isNotEmpty()) {
                    cv.drawText(" $unit", rightEdge - unitWidth, baseline, unitPaint)
                }
                if (index < rows.size - 1) {
                    cv.drawLine(MARGIN, y + rowHeight, MARGIN + width, y + rowHeight, linePaint)
                }
                y += rowHeight
            }
            y += 18f
        }

        /** Wk / machine-setting measurement cards: one small card per k-value. */
        fun drawWkCards(values: List<Triple<Int, Double, String>>, reishauerStyle: Boolean = false) {
            ensureSpace(64f)
            val cv = canvas!!
            val width = PAGE_WIDTH - 2 * MARGIN
            val gap = 10f
            val cardWidth = (width - gap * (values.size - 1)) / values.size
            val cardHeight = 58f
            val top = y

            values.forEachIndexed { i, (k, wk, unit) ->
                val left = MARGIN + i * (cardWidth + gap)
                val rect = RectF(left, top, left + cardWidth, top + cardHeight)
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (reishauerStyle) Color.rgb(250, 245, 255) else Color.rgb(239, 246, 255)
                    style = Paint.Style.FILL
                }
                cv.drawRoundRect(rect, 6f, 6f, bgPaint)
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE; strokeWidth = 1f; color = accentColor
                }
                cv.drawRoundRect(rect, 6f, 6f, borderPaint)

                val kPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = bold; textSize = 9f; color = accentColor; textAlign = Paint.Align.CENTER
                }
                cv.drawText("k = $k", left + cardWidth / 2f, top + 17f, kPaint)

                val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = bold; textSize = 13.5f; color = INK; textAlign = Paint.Align.CENTER
                }
                cv.drawText(wk.toFixedJs(4), left + cardWidth / 2f, top + 38f, valuePaint)
                val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = regular; textSize = 8f; color = MUTED; textAlign = Paint.Align.CENTER
                }
                cv.drawText(unit, left + cardWidth / 2f, top + 50f, unitPaint)
            }
            y = top + cardHeight + 20f
        }

        // -- Gear-combination cards --------------------------------------------------------------

        fun drawCombinationCards(combinations: List<GearCombination>, targetRatio: Double, sectionLabel: String) {
            currentSectionLabel = sectionLabel
            if (combinations.isEmpty()) {
                drawEmptyCombinationsNote()
                return
            }
            combinations.forEachIndexed { index, combo ->
                val error = abs(combo.ratio - targetRatio)
                drawOneCombinationCard(
                    index = index + 1,
                    a = combo.a, b = combo.b, c = combo.c, d = combo.d, ratio = combo.ratio,
                    targetRatio = targetRatio, error = error,
                    realizedAngle = null, deviation = null, isPerfectMatch = false,
                )
            }
            y += 4f
        }

        fun drawReishauerCombinationCards(combinations: List<ReishauerHelisCombination>, targetRatio: Double, sectionLabel: String) {
            currentSectionLabel = sectionLabel
            if (combinations.isEmpty()) {
                drawEmptyCombinationsNote()
                return
            }
            combinations.forEachIndexed { index, rc ->
                val combo = rc.combination
                drawOneCombinationCard(
                    index = index + 1,
                    a = combo.a, b = combo.b, c = combo.c, d = combo.d, ratio = combo.ratio,
                    targetRatio = targetRatio, error = rc.absoluteError,
                    realizedAngle = rc.actualHelixAngleDeg, deviation = rc.deviationDeg,
                    isPerfectMatch = rc.isPerfectMatch,
                )
            }
            y += 4f
        }

        private fun drawEmptyCombinationsNote() {
            ensureSpace(28f)
            val cv = canvas!!
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = regular; textSize = 10f; color = MUTED }
            cv.drawText("Uygun kombinasyon bulunamadı.", MARGIN, y + 12f, textPaint)
            y += 26f
        }

        private fun drawOneCombinationCard(
            index: Int,
            a: Int, b: Int, c: Int?, d: Int?, ratio: Double,
            targetRatio: Double, error: Double,
            realizedAngle: Double?, deviation: Double?, isPerfectMatch: Boolean,
        ) {
            val isFourGear = c != null && d != null
            val metricLines = mutableListOf(
                "Çark Oranı" to ratio.toFixedJs(6),
                "Hedef Oran" to targetRatio.toFixedJs(6),
                "Hata" to error.toFixedJs(9),
            )
            if (realizedAngle != null) {
                metricLines.add("Gerçekleşen Helis Açısı" to realizedAngle.toDmscs())
                if (deviation != null) metricLines.add("Fark (β)" to deviation.toDmscsSigned())
            }
            val metricsHeight = metricLines.size * 13f
            val cardHeight = 22f /* header row */ + 46f /* fraction row */ + metricsHeight + 16f /* paddings */

            ensureSpace(cardHeight + 8f)
            val cv = canvas!!
            val width = PAGE_WIDTH - 2 * MARGIN
            val top = y
            val rect = RectF(MARGIN, top, MARGIN + width, top + cardHeight)

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
            cv.drawRoundRect(rect, 5f, 5f, bgPaint)
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = if (isPerfectMatch) 1.6f else 1f
                color = if (isPerfectMatch) Color.rgb(16, 185, 129) else LINE
            }
            cv.drawRoundRect(rect, 5f, 5f, borderPaint)

            var cy = top + 8f

            // Header row: index badge, gear-count chip, perfect-match badge.
            val indexPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 9f; color = MUTED
            }
            cv.drawText("#${index.toString().padStart(2, '0')}", MARGIN + 10f, cy + 9f, indexPaint)

            val chipText = if (isFourGear) "4 ÇARK" else "2 ÇARK"
            val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = bold; textSize = 8f; color = accentColor; textAlign = Paint.Align.RIGHT }
            var chipRight = MARGIN + width - 10f
            cv.drawText(chipText, chipRight, cy + 9f, chipPaint)
            if (isPerfectMatch) {
                val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = bold; textSize = 8f; color = Color.rgb(5, 150, 105); textAlign = Paint.Align.RIGHT }
                val chipWidth = chipPaint.measureText(chipText)
                chipRight -= chipWidth + 14f
                cv.drawText("TAM EŞLEŞME", chipRight, cy + 9f, badgePaint)
            }
            cy += 20f

            // Fraction row.
            val fracTop = cy
            drawFractionRow(cv, MARGIN + 14f, fracTop, a, b, c, d, isFourGear)
            cy += 40f

            // Divider.
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = LINE; strokeWidth = 0.75f }
            cv.drawLine(MARGIN + 10f, cy, MARGIN + width - 10f, cy, dividerPaint)
            cy += 12f

            // Metrics, two columns to conserve space when there are many rows.
            val metricLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = regular; textSize = 8.5f; color = MUTED }
            val metricValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = bold; textSize = 8.5f; color = INK; textAlign = Paint.Align.RIGHT }
            val halfWidth = width / 2f - 10f
            metricLines.forEachIndexed { i, (label, value) ->
                val col = 0
                val rowY = cy + i * 13f
                val colLeft = MARGIN + 10f
                val colRight = MARGIN + 10f + halfWidth
                cv.drawText(label, colLeft, rowY, metricLabelPaint)
                cv.drawText(value, colRight, rowY, metricValuePaint)
            }

            y = top + cardHeight + 8f
        }

        private fun drawFractionRow(cv: Canvas, startX: Float, top: Float, a: Int, b: Int, c: Int?, d: Int?, isFourGear: Boolean) {
            val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = italic; textSize = 11f; color = MUTED; textAlign = Paint.Align.CENTER
            }
            val equalsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 13f; color = MUTED
            }
            val timesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = regular; textSize = 11f; color = MUTED
            }
            val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bold; textSize = 13f; color = accentColor; textAlign = Paint.Align.CENTER
            }
            val lineY = top + 18f

            var x = startX
            // Symbolic side.
            x += drawFraction(cv, x, top, lineY, "A", "B", symbolPaint) + 4f
            if (isFourGear) {
                cv.drawText(" × ", x, lineY + 3f, timesPaint)
                x += timesPaint.measureText(" × ") + 4f
                x += drawFraction(cv, x, top, lineY, "C", "D", symbolPaint) + 4f
            }
            x += 8f
            cv.drawText("=", x, lineY + 4f, equalsPaint)
            x += equalsPaint.measureText("=") + 14f

            x += drawFraction(cv, x, top, lineY, a.toString(), b.toString(), valuePaint) + 4f
            if (isFourGear && c != null && d != null) {
                cv.drawText(" × ", x, lineY + 3f, timesPaint)
                x += timesPaint.measureText(" × ") + 4f
                drawFraction(cv, x, top, lineY, c.toString(), d.toString(), valuePaint)
            }
        }

        /** Draws a numerator / divider line / denominator stacked fraction with the given
         * (center-aligned) paint, starting at [startX]. Returns the width consumed. */
        private fun drawFraction(cv: Canvas, startX: Float, numeratorBaseline: Float, lineY: Float, numerator: String, denominator: String, paint: Paint): Float {
            val width = max(paint.measureText(numerator), paint.measureText(denominator)) + 4f
            val cx = startX + width / 2f
            cv.drawText(numerator, cx, numeratorBaseline + 9f, paint)
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paint.color; strokeWidth = 1f }
            cv.drawLine(cx - width / 2f, lineY, cx + width / 2f, lineY, linePaint)
            cv.drawText(denominator, cx, lineY + 12f, paint)
            return width
        }

    }
}
