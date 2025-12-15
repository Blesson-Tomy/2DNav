package com.example.a2dnav.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.a2dnav.map.MapManager
import com.example.a2dnav.models.Position

class MapCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mapManager: MapManager? = null
    private var currentPath: List<Position>? = null
    private var currentPosition: Position? = null
    private var targetPosition: Position? = null

    private var cellSize = 10f
    private var offsetX = 20f
    private var offsetY = 20f

    // Callback for when position is changed by tapping
    var onPositionChanged: ((Position) -> Unit)? = null

    // Paint objects
    private val wallPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val walkablePaint = Paint().apply {
        color = Color.parseColor("#F5F5F5")
        style = Paint.Style.FILL
    }

    private val navigablePathPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val calculatedPathPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 6f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
    }

    private val gridPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val currentPositionPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    private val targetPositionPaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 24f
        isAntiAlias = true
    }

    private val roomLabelPaint = Paint().apply {
        color = Color.parseColor("#757575")
        textSize = 16f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    fun setMapManager(manager: MapManager) {
        mapManager = manager
        requestLayout()
        invalidate()
    }

    fun setPath(path: List<Position>?) {
        currentPath = path
        invalidate()
    }

    fun setCurrentPosition(position: Position?) {
        currentPosition = position
        invalidate()
    }

    fun setTargetPosition(position: Position?) {
        targetPosition = position
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val manager = mapManager ?: return false

            // Convert screen coordinates to grid coordinates
            val gridX = ((event.x - offsetX) / cellSize).toInt()
            val gridY = ((event.y - offsetY) / cellSize).toInt()

            // Check if the position is valid and walkable
            if (gridX in 0 until manager.width && gridY in 0 until manager.height) {
                val newPosition = Position(gridX, gridY)

                if (manager.isWalkable(newPosition)) {
                    currentPosition = newPosition
                    onPositionChanged?.invoke(newPosition)
                    invalidate()
                    return true
                } else {
                    // Position is not walkable (it's a wall)
                    return false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val manager = mapManager ?: return

        // White background
        canvas.drawColor(Color.WHITE)

        // Draw grid
        drawGrid(canvas, manager)

        // Draw walkable areas and walls
        drawMap(canvas, manager)

        // Draw room labels
        drawRoomLabels(canvas)

        // Draw navigable path
        drawNavigablePath(canvas, manager)

        // Draw calculated path (A to B)
        currentPath?.let { drawCalculatedPath(canvas, it) }

        // Draw current position (Point A)
        currentPosition?.let { drawMarker(canvas, it, currentPositionPaint, "A") }

        // Draw target position (Point B)
        targetPosition?.let { drawMarker(canvas, it, targetPositionPaint, "B") }

        // Draw legend
        drawLegend(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val manager = mapManager
        if (manager != null) {
            val width = (manager.width * cellSize + offsetX * 2).toInt()
            val height = (manager.height * cellSize + offsetY * 2).toInt()
            setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun drawGrid(canvas: Canvas, manager: MapManager) {
        for (x in 0..manager.width) {
            val startX = offsetX + x * cellSize
            canvas.drawLine(startX, offsetY, startX, offsetY + manager.height * cellSize, gridPaint)
        }
        for (y in 0..manager.height) {
            val startY = offsetY + y * cellSize
            canvas.drawLine(offsetX, startY, offsetX + manager.width * cellSize, startY, gridPaint)
        }
    }

    private fun drawMap(canvas: Canvas, manager: MapManager) {
        for (y in 0 until manager.height) {
            for (x in 0 until manager.width) {
                val cell = manager.getCell(x, y) ?: continue
                val left = offsetX + x * cellSize
                val top = offsetY + y * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                val paint = if (cell.isWalkable) walkablePaint else wallPaint
                canvas.drawRect(left, top, right, bottom, paint)
            }
        }
    }

    private fun drawRoomLabels(canvas: Canvas) {
        val rooms = mapOf(
            "Dining" to Pair(22, 22),
            "Living" to Pair(42, 57),
            "Patio" to Pair(45, 17),
            "Bedroom" to Pair(90, 22),
            "Bedroom" to Pair(90, 62),
            "Bath" to Pair(93, 40),
            "Study" to Pair(97, 82),
            "Foyer" to Pair(72, 85),
            "Garage" to Pair(20, 80)
        )

        rooms.forEach { (label, coords) ->
            val x = offsetX + coords.first * cellSize
            val y = offsetY + coords.second * cellSize
            canvas.drawText(label, x, y, roomLabelPaint)
        }
    }

    private fun drawNavigablePath(canvas: Canvas, manager: MapManager) {
        val pathCells = manager.getNavigablePathCells()

        for (i in 0 until pathCells.size - 1) {
            val current = pathCells[i]
            val next = pathCells[i + 1]

            val x1 = offsetX + current.x * cellSize + cellSize / 2
            val y1 = offsetY + current.y * cellSize + cellSize / 2
            val x2 = offsetX + next.x * cellSize + cellSize / 2
            val y2 = offsetY + next.y * cellSize + cellSize / 2

            canvas.drawLine(x1, y1, x2, y2, navigablePathPaint)
        }
    }

    private fun drawCalculatedPath(canvas: Canvas, path: List<Position>) {
        if (path.size < 2) return

        val pathObj = Path()
        val first = path[0]
        pathObj.moveTo(
            offsetX + first.x * cellSize + cellSize / 2,
            offsetY + first.y * cellSize + cellSize / 2
        )

        for (i in 1 until path.size) {
            val pos = path[i]
            pathObj.lineTo(
                offsetX + pos.x * cellSize + cellSize / 2,
                offsetY + pos.y * cellSize + cellSize / 2
            )
        }

        canvas.drawPath(pathObj, calculatedPathPaint)
    }

    private fun drawMarker(canvas: Canvas, position: Position, paint: Paint, label: String) {
        val x = offsetX + position.x * cellSize + cellSize / 2
        val y = offsetY + position.y * cellSize + cellSize / 2

        // Draw outer circle (border)
        val borderPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(x, y, 18f, borderPaint)

        // Draw inner circle
        canvas.drawCircle(x, y, 15f, paint)

        val labelPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(label, x, y + 8f, labelPaint)
    }

    private fun drawLegend(canvas: Canvas) {
        val legendX = width - 200f
        val legendY = 50f
        val lineHeight = 35f

        // Background
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(legendX - 10, legendY - 10, width - 10f, legendY + lineHeight * 5 + 10, bgPaint)

        // Border
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(legendX - 10, legendY - 10, width - 10f, legendY + lineHeight * 5 + 10, borderPaint)

        // Legend items
        drawLegendItem(canvas, legendX, legendY, wallPaint, "Wall")
        drawLegendItem(canvas, legendX, legendY + lineHeight, navigablePathPaint, "Red Path")
        drawLegendItem(canvas, legendX, legendY + lineHeight * 2, calculatedPathPaint, "A→B Path")
        drawLegendItem(canvas, legendX, legendY + lineHeight * 3, currentPositionPaint, "Point A")
        drawLegendItem(canvas, legendX, legendY + lineHeight * 4, targetPositionPaint, "Point B")
    }

    private fun drawLegendItem(canvas: Canvas, x: Float, y: Float, paint: Paint, label: String) {
        if (paint.style == Paint.Style.FILL) {
            canvas.drawRect(x, y - 10, x + 20, y + 10, paint)
        } else {
            canvas.drawLine(x, y, x + 20, y, paint)
        }
        canvas.drawText(label, x + 30, y + 5, textPaint)
    }
}