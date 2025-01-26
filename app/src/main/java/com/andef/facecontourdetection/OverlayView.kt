package com.andef.facecontourdetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private var _faces = listOf<Face>()
    var faces = _faces.toList()
        get() = _faces.toList()
        set(value) {
            _faces = value
            field = _faces.toList()
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (_faces.isNotEmpty()) {
            for (face in _faces) {
                val faceContour = face.allContours[0]
                val points = faceContour.points
                var startX = points[0].x
                var startY = points[0].y
                for (point in points) {
                    val finishX = point.x
                    val finishY = point.y
                    canvas.drawLine(startX, startY, finishX, finishY, paint)
                    startX = finishX
                    startY = finishY
                }
                canvas.drawLine(startX, startY, points[0].x, points[0].y, paint)
            }
        }
    }
}