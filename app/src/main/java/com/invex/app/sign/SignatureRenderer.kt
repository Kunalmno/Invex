package com.invex.app.sign

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import java.io.ByteArrayOutputStream

/**
 * Turns the signature on the sheet (drawn strokes or typed name) into a PNG: black on a transparent background,
 * cropped to the signature. The app tints it white when it shows it on the blue admit card box.
 * Returns null when there is nothing to draw.
 */
object SignatureRenderer {

    private const val PADDING = 16f
    private const val STROKE = 5f
    private const val TYPED_TEXT_SIZE = 120f

    fun toPng(tab: SignTab, strokes: List<List<Offset>>, typedName: String): ByteArray? {
        val bitmap = when (tab) {
            SignTab.DRAW -> drawStrokes(strokes)
            SignTab.TYPE -> drawTyped(typedName.trim())
        } ?: return null
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    private fun drawStrokes(strokes: List<List<Offset>>): Bitmap? {
        val points = strokes.flatten()
        if (points.isEmpty()) return null
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        // The line was drawn about 2.5dp wide; scale up so the PNG is sharp, but keep the proportions.
        val scale = (900f / maxOf(maxX - minX, 1f)).coerceIn(1f, 6f)
        val width = ((maxX - minX) * scale + 2 * PADDING).toInt().coerceAtLeast(32)
        val height = ((maxY - minY) * scale + 2 * PADDING).toInt().coerceAtLeast(32)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        fun x(o: Offset) = (o.x - minX) * scale + PADDING
        fun y(o: Offset) = (o.y - minY) * scale + PADDING
        strokes.forEach { stroke ->
            if (stroke.isEmpty()) return@forEach
            if (stroke.size == 1) {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(x(stroke[0]), y(stroke[0]), STROKE / 2f, paint)
                paint.style = Paint.Style.STROKE
                return@forEach
            }
            val path = Path().apply {
                moveTo(x(stroke[0]), y(stroke[0]))
                for (i in 1 until stroke.size) {
                    val prev = stroke[i - 1]
                    val cur = stroke[i]
                    quadTo(x(prev), y(prev), (x(prev) + x(cur)) / 2f, (y(prev) + y(cur)) / 2f)
                }
                lineTo(x(stroke.last()), y(stroke.last()))
            }
            canvas.drawPath(path, paint)
        }
        return bitmap
    }

    private fun drawTyped(name: String): Bitmap? {
        if (name.isEmpty()) return null
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = TYPED_TEXT_SIZE
            typeface = Typeface.create("cursive", Typeface.NORMAL)
        }
        val bounds = android.graphics.Rect()
        paint.getTextBounds(name, 0, name.length, bounds)
        val width = (bounds.width() + 2 * PADDING).toInt().coerceAtLeast(32)
        val height = (bounds.height() + 2 * PADDING).toInt().coerceAtLeast(32)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawText(name, PADDING - bounds.left, PADDING - bounds.top, paint)
        return bitmap
    }
}
