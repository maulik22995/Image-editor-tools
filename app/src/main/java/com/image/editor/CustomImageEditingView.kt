package com.image.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView



class CustomImageEditingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    enum class Mode {
        NONE, DRAG, RESIZE, CROP, SKEW, ALPHA
    }

    private var currentMatrix = Matrix()
    private var cropMatrix = Matrix()
    private var savedMatrix = Matrix()

    private var mode: Mode = Mode.DRAG

    //PointF objects are used to record the point(s) the user is touching
    private var startPoint = PointF()


    // Variables to store crop area
    private var cropStartPoint = PointF()
    private var cropEndPoint = PointF()

    init {
        scaleType = ScaleType.MATRIX
        post {
            currentMatrix.set(savedMatrix)
            imageMatrix = currentMatrix
        }
    }

    private var skewMatrix = Matrix()
    private var startCornerPoint = PointF()
    private val minWidth = 10
    private val minHeight = 10
    private val cropPaint = Paint().apply {
        color = context.getColor(R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val paint = Paint().apply {
        color = context.getColor(R.color.primary)
        style = Paint.Style.FILL
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }
    private val gridPaint = Paint().apply {
        color = context.getColor(R.color.primary)
        strokeWidth = 3f
        alpha = 70
    }

    /**
     * set current mode
     */
    fun setMode(mode: Mode) {
        this.mode = mode
        if (mode == Mode.CROP) {
            prepareCropFrame()
        }
        invalidate()
    }

    /**
     * get current mode
     */
    fun getCurrentMode(): Mode {
        return mode
    }

    private fun prepareCropFrame() {
        cropMatrix = Matrix(currentMatrix)
        val cropRect = calculateImageRect(cropMatrix)
        cropStartPoint = PointF(cropRect.left, cropRect.top)
        cropEndPoint = PointF(cropRect.right, cropRect.bottom)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val matrixValues = FloatArray(9)
        currentMatrix.getValues(matrixValues)
        val imageRect = calculateImageRect(currentMatrix)
        val cropRect = calculateImageRect(cropMatrix)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(currentMatrix)
                startPoint.set(event.x, event.y)

                // Check if the touch is on any of the corners
                if (mode == Mode.RESIZE && isTouchOnHandle(
                        event.x,
                        event.y,
                        imageRect
                    ) !== Handle.NONE
                ) {
                    startCornerPoint.set(event.x, event.y)
                } else if (mode == Mode.CROP) {
                    startCornerPoint.set(event.x, event.y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == Mode.DRAG) {
                    currentMatrix.set(savedMatrix)
                    currentMatrix.postTranslate(
                        event.x - startPoint.x,
                        event.y - startPoint.y
                    )
                } else if (mode == Mode.RESIZE && isTouchOnHandle(
                        event.x,
                        event.y,
                        imageRect
                    ) !== Handle.NONE
                ) {
                    if (startCornerPoint.x == 0f && startCornerPoint.y == 0f) {
                        startCornerPoint.set(event.x, event.y)
                    }
                    calculateAndUpdateMatrix(imageRect, currentMatrix, event.x, event.y)
                } else if (mode == Mode.CROP) {
                    if (isPointInsideCropRect(event.x, event.y)) {
                        calculateAndUpdateMatrix(cropRect, cropMatrix, event.x, event.y)
                        invalidate()
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (mode == Mode.RESIZE) {
                    startCornerPoint.set(0f, 0f)
                } else if (mode == Mode.CROP) {
                    val rect = calculateImageRect(cropMatrix)
                    cropStartPoint = PointF(rect.left, rect.top)
                    cropEndPoint = PointF(rect.right, rect.bottom)
                }
            }
        }
        imageMatrix = currentMatrix
        return true
    }

    /**
     * It will skew image based on input from slider
     * */
    fun skewImage(horizontal: Float, vertical: Float, isTop: Boolean) {
        val imageRect = calculateImageRect(currentMatrix)
        skewMatrix = Matrix()
        skewMatrix.set(currentMatrix)
        // Apply skewing only to the top part of the image
        if (horizontal == 0f) {
            if (isTop) {
                skewMatrix.postSkew(0f, vertical, imageRect.left, imageRect.top)
            } else {
                skewMatrix.postSkew(0f, vertical, imageRect.right, imageRect.bottom)
            }
        } else {
            if (isTop) {
                skewMatrix.postSkew(horizontal, 0f, imageRect.right, imageRect.bottom)
            } else {
                skewMatrix.postSkew(horizontal, 0f, imageRect.left, imageRect.top)
            }
        }
        imageMatrix = skewMatrix
        invalidate()

    }

    /**
     * Check if crop area is not moving outside of image
     */
    private fun isPointInsideCropRect(x: Float, y: Float): Boolean {
        val imageRect = calculateImageRect(currentMatrix)
        return imageRect.contains(x, y)
    }

    /**
     * Move the picked corner and update the matrix accordingly
     * For Crop frame or resizing image
     */
    private fun calculateAndUpdateMatrix(
        rect: RectF,
        matrix: Matrix,
        pointX: Float,
        pointY: Float
    ) {
        val dx = (pointX - startCornerPoint.x)
        val dy = (pointY - startCornerPoint.y)

        val width = rect.width()
        val height = rect.height()

        val newWidth: Float
        val newHeight: Float
        val pivotX: Float
        val pivotY: Float

        when (isTouchOnHandle(pointX, pointY, rect)) {
            Handle.TOP_LEFT -> {
                newWidth = width - dx
                newHeight = height - dy
                pivotX = rect.right
                pivotY = rect.bottom
            }

            Handle.TOP_RIGHT -> {
                newWidth = width + dx
                newHeight = height - dy
                pivotX = rect.left
                pivotY = rect.bottom
            }

            Handle.BOTTOM_LEFT -> {
                newWidth = width - dx
                newHeight = height + dy
                pivotX = rect.right
                pivotY = rect.top
            }

            Handle.BOTTOM_RIGHT -> {
                newWidth = width + dx
                newHeight = height + dy
                pivotX = rect.left
                pivotY = rect.top
            }

            Handle.TOP -> {
                newWidth = width
                newHeight = height - dy
                pivotX = (rect.left + rect.right) / 2 // Keep X coordinate unchanged
                pivotY = rect.bottom
            }

            Handle.BOTTOM -> {
                newWidth = width
                newHeight = height + dy
                pivotX = (rect.left + rect.right) / 2 // Keep X coordinate unchanged
                pivotY = rect.top
            }

            Handle.LEFT -> {
                newWidth = width - dx
                newHeight = height
                pivotX = rect.right
                pivotY = (rect.top + rect.bottom) / 2 // Keep Y coordinate unchanged
            }

            Handle.RIGHT -> {
                newWidth = width + dx
                newHeight = height
                pivotX = rect.left
                pivotY = (rect.top + rect.bottom) / 2 // Keep Y coordinate unchanged
            }

            else -> {
                newWidth = width.toFloat()
                newHeight = height.toFloat()
                pivotX = pointX
                pivotY = pointY
            }
        }


        if (newWidth > minWidth && newHeight > minHeight) {
            // Calculate the scaling factors based on the change in width and height
            val scaleX = newWidth / width
            val scaleY = newHeight / height

            // Apply scaling
            matrix.postScale(
                scaleX,
                scaleY,
                pivotX,
                pivotY
            )

            startCornerPoint.set(pointX, pointY)
        }
    }

    /**
     * Crop and set image new bitmap to view
     */
    fun cropAndSetImage() {
        val croppedBitmap = cropImage()
        setImageBitmap(croppedBitmap)

        val translationX = cropStartPoint.x
        val translationY = cropStartPoint.y

        currentMatrix = Matrix()

        // Apply the translation to the current matrix
        currentMatrix.postTranslate(translationX, translationY)

        // Set the adjusted matrix to the image view
        imageMatrix = currentMatrix

        cropMatrix.reset()
        invalidate()
        setMode(Mode.DRAG)
    }

    /**
     * calculate and crop area of image using updated
     * crop matrix
     */
    private fun cropImage(): Bitmap? {

        // Get the visible rectangle of the image
        val imageRect = calculateImageRect(currentMatrix)

        // Calculate the intersection between the crop area and the image bounds
        val cropRect = RectF(
            cropStartPoint.x.coerceIn(imageRect.left, imageRect.right),
            cropStartPoint.y.coerceIn(imageRect.top, imageRect.bottom),
            cropEndPoint.x.coerceIn(imageRect.left, imageRect.right),
            cropEndPoint.y.coerceIn(imageRect.top, imageRect.bottom)
        )

        // Check if the crop area has valid dimensions
        if (cropRect.width() < minWidth || cropRect.height() < minHeight) {
            // Invalid crop area
            return null
        }

        // Create a bitmap representing the current state of the image with transformations applied
        val drawable = drawable ?: return null
        val bitmap = (drawable as BitmapDrawable).bitmap
        val transformedBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, currentMatrix, true)

        // Calculate the dimensions of the crop area in bitmap coordinates
        val cropLeft =
            ((cropRect.left - imageRect.left) / imageRect.width() * transformedBitmap.width).toInt()
        val cropTop =
            ((cropRect.top - imageRect.top) / imageRect.height() * transformedBitmap.height).toInt()
        val cropWidth = (cropRect.width() / imageRect.width() * transformedBitmap.width).toInt()
        val cropHeight = (cropRect.height() / imageRect.height() * transformedBitmap.height).toInt()

        val croppedBitmap =
            Bitmap.createBitmap(transformedBitmap, cropLeft, cropTop, cropWidth, cropHeight)
        return croppedBitmap

    }

    /**
     *  this function flip the image matrix to horizontally
     *  to opposite direction that currently
     */
    fun flipHorizontally() {
        val matrix = Matrix(currentMatrix)
        val imageRect = calculateImageRect(currentMatrix)
        // Calculate the original center point of the image
        val originalCenterX = (imageRect.right + imageRect.left) / 2
        val originalCenterY = (imageRect.bottom + imageRect.top) / 2

        // Flip horizontally
        matrix.postScale(-1f, 1f, originalCenterX, originalCenterY)
        currentMatrix.set(matrix)
        imageMatrix = currentMatrix
    }

    private enum class Handle {
        TOP,
        LEFT,
        RIGHT,
        BOTTOM,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        NONE
    }

    /**
     * Draw crop or resize frame over image
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawable == null) {
            return
        }
        canvas.apply {
            if (mode == Mode.RESIZE) {
                val imageRect = calculateImageRect(currentMatrix)
                canvas.drawRect(imageRect, cropPaint)
                drawHandles(imageRect, paint)
            } else if (mode == Mode.CROP) {
                val cropRect = calculateImageRect(cropMatrix)
                canvas.drawRect(cropRect, cropPaint)
                drawHandles(cropRect, paint)
                drawGridLines(cropRect, gridPaint)
            }
        }
    }


    /**
     * Will identify if user picked corner and will return which handle is picked
     */
    private fun isTouchOnHandle(
        x: Float,
        y: Float,
        imageRect: RectF,
        handleLength: Float = 70f
    ): Handle {
        // Define the regions for each handle
        val topHandleRegion = RectF(
            (imageRect.left + imageRect.right - handleLength) / 2,
            imageRect.top - handleLength / 2,
            (imageRect.left + imageRect.right + handleLength) / 2,
            imageRect.top + handleLength / 2
        )

        val bottomHandleRegion = RectF(
            (imageRect.left + imageRect.right - handleLength) / 2,
            imageRect.bottom - handleLength / 2,
            (imageRect.left + imageRect.right + handleLength) / 2,
            imageRect.bottom + handleLength / 2
        )

        val leftHandleRegion = RectF(
            imageRect.left - handleLength / 2,
            (imageRect.top + imageRect.bottom - handleLength) / 2,
            imageRect.left + handleLength / 2,
            (imageRect.top + imageRect.bottom + handleLength) / 2
        )

        val rightHandleRegion = RectF(
            imageRect.right - handleLength / 2,
            (imageRect.top + imageRect.bottom - handleLength) / 2,
            imageRect.right + handleLength / 2,
            (imageRect.top + imageRect.bottom + handleLength) / 2
        )

        val leftTopCorner = RectF(
            imageRect.left - handleLength,
            imageRect.top - handleLength,
            imageRect.left + handleLength,
            imageRect.top + handleLength
        )
        val rightTopCorner = RectF(
            imageRect.right - handleLength,
            imageRect.top - handleLength,
            imageRect.right + handleLength,
            imageRect.top + handleLength
        )
        val leftBottomCorner = RectF(
            imageRect.left - handleLength,
            imageRect.bottom - handleLength,
            imageRect.left + handleLength,
            imageRect.bottom + handleLength
        )
        val rightBottomCorner = RectF(
            imageRect.right - handleLength,
            imageRect.bottom - handleLength,
            imageRect.right + handleLength,
            imageRect.bottom + handleLength
        )

        // Check if the touch coordinates lie within any handle region
        return when {
            leftTopCorner.contains(x, y) -> Handle.TOP_LEFT
            rightTopCorner.contains(x, y) -> Handle.TOP_RIGHT
            leftBottomCorner.contains(x, y) -> Handle.BOTTOM_LEFT
            rightBottomCorner.contains(x, y) -> Handle.BOTTOM_RIGHT
            topHandleRegion.contains(x, y) -> Handle.TOP
            bottomHandleRegion.contains(x, y) -> Handle.BOTTOM
            leftHandleRegion.contains(x, y) -> Handle.LEFT
            rightHandleRegion.contains(x, y) -> Handle.RIGHT
            else -> Handle.NONE
        }
    }

    /**
     * Calculate RecF bitmap image
     */
    private fun calculateImageRect(matrixValues: Matrix): RectF {
        val drawable = drawable
        val imageRect =
            RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        matrixValues.mapRect(imageRect)
        return imageRect
    }

    /**
     * Update current matrix with skew applied
     */
    fun skewImageUpdate() {
        currentMatrix.set(skewMatrix)
        imageMatrix = currentMatrix
        invalidate()
    }
}
 
 