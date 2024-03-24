package com.image.editor

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

fun Canvas.drawGridLines(imageRect: RectF, paint: Paint) {
    val minGridSize = 1 // Minimum grid size
    val maxGridLines = 5 // Maximum number of grid lines

    // Calculate the ideal grid size based on the dimensions of the imageRect
    val idealGridSizeX = imageRect.width() / maxGridLines
    val idealGridSizeY = imageRect.height() / maxGridLines

    // Determine the final grid size as the maximum of the minimum grid size and the ideal grid size
    val gridSizeX = Math.max(minGridSize, idealGridSizeX.toInt())
    val gridSizeY = Math.max(minGridSize, idealGridSizeY.toInt())

    // Draw vertical grid lines
    var x = imageRect.left + gridSizeX
    while (x < imageRect.right) {
        drawLine(x, imageRect.top, x, imageRect.bottom, paint)
        x += gridSizeX
    }

    // Draw horizontal grid lines
    var y = imageRect.top + gridSizeY
    while (y < imageRect.bottom) {
        drawLine(imageRect.left, y, imageRect.right, y, paint)
        y += gridSizeY
    }
}


fun Canvas.drawHandles(imageRect: RectF, paint: Paint, handleLength: Float = 70f) {

    // Draw top-left corner L shape
    drawLine(imageRect.left, imageRect.top, imageRect.left, imageRect.top + handleLength, paint)
    drawLine(imageRect.left, imageRect.top, imageRect.left + handleLength, imageRect.top, paint)

    // Draw top-right corner L shape
    drawLine(imageRect.right, imageRect.top, imageRect.right, imageRect.top + handleLength, paint)
    drawLine(imageRect.right, imageRect.top, imageRect.right - handleLength, imageRect.top, paint)

    // Draw bottom-left corner L shape
    drawLine(
        imageRect.left,
        imageRect.bottom,
        imageRect.left,
        imageRect.bottom - handleLength,
        paint
    )
    drawLine(
        imageRect.left,
        imageRect.bottom,
        imageRect.left + handleLength,
        imageRect.bottom,
        paint
    )

    // Draw bottom-right corner L shape
    drawLine(
        imageRect.right,
        imageRect.bottom,
        imageRect.right,
        imageRect.bottom - handleLength,
        paint
    )
    drawLine(
        imageRect.right,
        imageRect.bottom,
        imageRect.right - handleLength,
        imageRect.bottom,
        paint
    )

    // Draw handle line at the center of the top side
    val topCenterX = (imageRect.left + imageRect.right) / 2
    val topCenterY = imageRect.top
    drawLine(
        topCenterX - handleLength / 2,
        topCenterY,
        topCenterX + handleLength / 2,
        topCenterY,
        paint
    )

    // Draw handle line at the center of the bottom side
    val bottomCenterX = (imageRect.left + imageRect.right) / 2
    val bottomCenterY = imageRect.bottom
    drawLine(
        bottomCenterX - handleLength / 2,
        bottomCenterY,
        bottomCenterX + handleLength / 2,
        bottomCenterY,
        paint
    )

    // Draw handle line at the center of the left side
    val leftCenterX = imageRect.left
    val leftCenterY = (imageRect.top + imageRect.bottom) / 2
    drawLine(
        leftCenterX,
        leftCenterY - handleLength / 2,
        leftCenterX,
        leftCenterY + handleLength / 2,
        paint
    )

    // Draw handle line at the center of the right side
    val rightCenterX = imageRect.right
    val rightCenterY = (imageRect.top + imageRect.bottom) / 2
    drawLine(
        rightCenterX,
        rightCenterY - handleLength / 2,
        rightCenterX,
        rightCenterY + handleLength / 2,
        paint
    )
}

