package com.huawei.quickhandanalyzer.graphic.hand

import android.graphics.*
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoint
import com.huawei.hms.mlsdk.handkeypoint.MLHandKeypoints
import com.huawei.quickhandanalyzer.graphic.GraphicOverlay
import com.huawei.quickhandanalyzer.graphic.GraphicOverlay.Graphic
import java.util.*
import kotlin.math.abs

class HandKeypointGraphic(private val overlay: GraphicOverlay, private val result: MLHandKeypoints) : Graphic(
    overlay
) {
    private val circlePaint: Paint = Paint()
    private val linePaint: Paint
    private val rectPaint: Paint
    private val rect: Rect

    @Synchronized
    override fun draw(canvas: Canvas) {
        canvas.drawRect(rect, rectPaint)
        val paths: MutableList<Path?> = ArrayList()
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_THUMB_FOURTH),
                result.getHandKeypoint(MLHandKeypoint.TYPE_THUMB_THIRD)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_THUMB_SECOND),
                result.getHandKeypoint(MLHandKeypoint.TYPE_THUMB_THIRD)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_WRIST),
                result.getHandKeypoint(MLHandKeypoint.TYPE_THUMB_FIRST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_THUMB_SECOND),
                result.getHandKeypoint(MLHandKeypoint.TYPE_THUMB_FIRST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_FOURTH),
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_THIRD)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_SECOND),
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_THIRD)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_SECOND),
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_SECOND)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_SECOND),
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_FIRST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_FOREFINGER_FIRST),
                result.getHandKeypoint(MLHandKeypoint.TYPE_WRIST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_MIDDLE_FINGER_FOURTH),
                result.getHandKeypoint(MLHandKeypoint.TYPE_MIDDLE_FINGER_THIRD)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_MIDDLE_FINGER_THIRD),
                result.getHandKeypoint(MLHandKeypoint.TYPE_MIDDLE_FINGER_SECOND)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_MIDDLE_FINGER_SECOND),
                result.getHandKeypoint(MLHandKeypoint.TYPE_MIDDLE_FINGER_FIRST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_MIDDLE_FINGER_FIRST),
                result.getHandKeypoint(MLHandKeypoint.TYPE_WRIST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_RING_FINGER_FOURTH),
                result.getHandKeypoint(MLHandKeypoint.TYPE_RING_FINGER_THIRD)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_RING_FINGER_THIRD),
                result.getHandKeypoint(MLHandKeypoint.TYPE_RING_FINGER_SECOND)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_RING_FINGER_SECOND),
                result.getHandKeypoint(MLHandKeypoint.TYPE_RING_FINGER_FIRST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_RING_FINGER_FIRST),
                result.getHandKeypoint(MLHandKeypoint.TYPE_WRIST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_LITTLE_FINGER_FOURTH),
                result.getHandKeypoint(MLHandKeypoint.TYPE_LITTLE_FINGER_THIRD)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_LITTLE_FINGER_THIRD),
                result.getHandKeypoint(MLHandKeypoint.TYPE_LITTLE_FINGER_SECOND)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_LITTLE_FINGER_SECOND),
                result.getHandKeypoint(MLHandKeypoint.TYPE_LITTLE_FINGER_FIRST)
            )
        )
        paths.add(
            getPath(
                result.getHandKeypoint(MLHandKeypoint.TYPE_LITTLE_FINGER_FIRST),
                result.getHandKeypoint(MLHandKeypoint.TYPE_WRIST)
            )
        )
        for (j in paths.indices) {
            if (paths[j] != null) {
                canvas.drawPath(paths[j]!!, linePaint)
            }
        }
        for (handKeypoint in result.handKeypoints) {
            if (!(abs(handKeypoint.pointX - 0f) == 0f && abs(handKeypoint.pointY - 0f) == 0f)) {
                canvas.drawCircle(
                    translateX(handKeypoint.pointX),
                    translateY(handKeypoint.pointY), 24f, circlePaint
                )
            }
        }
    }

    private fun getPath(p1: MLHandKeypoint?, p2: MLHandKeypoint?): Path? {
        if (p1 == null || p2 == null) {
            return null
        }
        if (p1.pointX == 0f && p1.pointY == 0f) {
            return null
        }
        if (p2.pointX == 0f && p2.pointY == 0f) {
            return null
        }
        val path = Path()
        path.moveTo(
            translateX(p1.pointX),
            translateY(p1.pointY)
        )
        path.lineTo(
            translateX(p2.pointX),
            translateY(p2.pointY)
        )
        return path
    }

    init {
        circlePaint.color = Color.RED
        circlePaint.style = Paint.Style.FILL
        circlePaint.isAntiAlias = true
        linePaint = Paint()
        linePaint.color = Color.GREEN
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = 4f
        linePaint.isAntiAlias = true
        rectPaint = Paint()
        rectPaint.color = Color.BLUE
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 5f
        rectPaint.isAntiAlias = true
        rect = Rect()
    }
}
