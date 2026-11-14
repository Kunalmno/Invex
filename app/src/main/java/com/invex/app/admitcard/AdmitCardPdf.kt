package com.invex.app.admitcard

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * "Download": draws the card on an A4 page and saves it as a PDF in the Downloads folder.
 * Returns where it went (for the message), or null when saving failed.
 */
object AdmitCardPdf {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val BLUE = 0xFF0070BB.toInt()

    fun save(context: Context, card: AdmitCard): String? = try {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        draw(page.canvas, card)
        doc.finishPage(page)
        val fileName = "InvEx-Admit-Card-${card.student.enrollmentNumber}.pdf"
        val location = write(context, doc, fileName)
        doc.close()
        location
    } catch (e: Exception) {
        null
    }

    private fun write(context: Context, doc: PdfDocument, fileName: String): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            context.contentResolver.openOutputStream(uri)?.use { doc.writeTo(it) } ?: return null
            return "Downloads"
        }
        // Android 8 / 9: no storage permission, so the app's own downloads folder.
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
        File(dir, fileName).outputStream().use { doc.writeTo(it) }
        return dir.absolutePath
    }

    private fun draw(canvas: Canvas, card: AdmitCard) {
        val margin = 40f
        val black = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
        fun text(size: Float, bold: Boolean = false, color: Int = Color.BLACK) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }

        // Frame
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 1.2f }
        canvas.drawRoundRect(RectF(margin, margin, PAGE_W - margin, PAGE_H - margin), 18f, 18f, frame)

        var y = margin + 50
        canvas.drawText("Admit Card", margin + 24, y, text(24f, bold = true))
        canvas.drawText("InvEx", PAGE_W - margin - 24 - 40, y, text(14f, bold = true, color = BLUE))

        // Student details, two columns
        val s = card.student
        val fields = listOf(
            "Name" to s.name, "Enrollment Number" to s.enrollmentNumber,
            "Course" to s.course, "Specialization" to s.specialization,
            "Semester" to semesterOrdinalText(s.semester), "Branch" to s.branch,
        )
        y += 34
        val colX = listOf(margin + 24, PAGE_W / 2f + 8)
        fields.chunked(2).forEach { row ->
            row.forEachIndexed { i, (label, value) ->
                canvas.drawText(label, colX[i], y, text(9f, color = Color.GRAY))
                canvas.drawText(value.ifBlank { "-" }, colX[i], y + 15, text(13f))
            }
            y += 38
        }

        // Exams table
        y += 8
        val line = Paint().apply { color = Color.BLACK; strokeWidth = 0.8f }
        canvas.drawLine(margin + 24, y, PAGE_W - margin - 24, y, line)
        y += 22
        val dateX = PAGE_W - margin - 24 - 170
        val shiftX = PAGE_W - margin - 24 - 40
        canvas.drawText("Course", margin + 24, y, text(9f, color = Color.GRAY))
        canvas.drawText("Date", dateX, y, text(9f, color = Color.GRAY))
        canvas.drawText("Shift", shiftX, y, text(9f, color = Color.GRAY))
        y += 6
        card.exams.forEach { exam ->
            y += 18
            canvas.drawText(exam.courseTitle.take(42), margin + 24, y, text(12f))
            canvas.drawText(exam.dateText, dateX, y, text(12f))
            canvas.drawText(exam.shift, shiftX, y, text(12f))
            y += 6
            canvas.drawLine(margin + 24, y, PAGE_W - margin - 24, y, line)
        }

        // QR (left) and signature (right), blue boxes like the app
        y += 28
        val box = 120f
        val qrBox = RectF(margin + 24, y, margin + 24 + box, y + box)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BLUE }
        canvas.drawRoundRect(qrBox, 10f, 10f, fill)
        card.qrCode?.let { code ->
            val qr = qrBitmap(code, 400)
            canvas.drawBitmap(qr, null, RectF(qrBox.left + 10, qrBox.top + 10, qrBox.right - 10, qrBox.bottom - 10), null)
        }
        val sigBox = RectF(qrBox.right + 16, y, PAGE_W - margin - 24, y + box)
        canvas.drawRoundRect(sigBox, 10f, 10f, fill)
        card.signaturePng?.let { png ->
            BitmapFactory.decodeByteArray(png, 0, png.size)?.let { sig ->
                canvas.drawBitmap(whiteSignature(sig), null, fit(sig.width, sig.height, sigBox, 14f), null)
            }
        }
        canvas.drawText("Signature of the student", sigBox.left, sigBox.bottom + 14, text(9f, color = Color.GRAY))
        canvas.drawText("Show this QR code to the invigilator", qrBox.left, qrBox.bottom + 14, text(9f, color = Color.GRAY))
    }

    /** The saved signature is black on transparent; on the blue box it is shown white. */
    fun whiteSignature(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = android.graphics.PorterDuffColorFilter(0xFFF7F9F7.toInt(), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        Canvas(out).drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun fit(w: Int, h: Int, box: RectF, pad: Float): RectF {
        val availW = box.width() - 2 * pad
        val availH = box.height() - 2 * pad
        val scale = minOf(availW / w, availH / h)
        val dw = w * scale
        val dh = h * scale
        val left = box.centerX() - dw / 2
        val top = box.centerY() - dh / 2
        return RectF(left, top, left + dw, top + dh)
    }

    private fun semesterOrdinalText(n: Int?): String = com.invex.app.medicalleave.semesterOrdinal(n)
}
