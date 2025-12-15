package com.example.a2dnav.map

import com.example.a2dnav.models.GridCell
import com.example.a2dnav.models.Position

class MapManager(
    val width: Int = 120,  // Grid width
    val height: Int = 100  // Grid height
) {
    private val grid: Array<Array<GridCell>> = Array(height) { y ->
        Array(width) { x ->
            GridCell(x, y)
        }
    }

    fun initializeFromFloorPlan() {
        clearGrid()
        addWalls()
        addNavigablePath()
        defineRooms()
    }

    private fun clearGrid() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                grid[y][x] = GridCell(x, y, isWalkable = true)
            }
        }
    }

    private fun addWalls() {
        // OUTER PERIMETER
        for (x in 0 until width) {
            setCell(x, 0, isWalkable = false)
            setCell(x, height - 1, isWalkable = false)
        }
        for (y in 0 until height) {
            setCell(0, y, isWalkable = false)
            setCell(width - 1, y, isWalkable = false)
        }

        // LEFT SIDE - Garage wall
        for (y in 60..100) {
            setCell(30, y, isWalkable = false)
        }

        // Dining room left wall
        for (y in 10..35) {
            setCell(10, y, isWalkable = false)
        }

        // Kitchen/Pantry walls
        for (x in 10..25) {
            setCell(x, 35, isWalkable = false)
        }
        for (y in 35..50) {
            setCell(25, y, isWalkable = false)
        }

        // Storage room walls
        for (y in 50..65) {
            setCell(25, y, isWalkable = false)
        }
        for (x in 10..25) {
            setCell(x, 65, isWalkable = false)
        }

        // Living room to Foyer wall (vertical)
        for (y in 50..80) {
            setCell(60, y, isWalkable = false)
        }

        // Covered Patio walls
        for (x in 35..55) {
            setCell(x, 10, isWalkable = false)
        }
        for (y in 10..25) {
            setCell(55, y, isWalkable = false)
        }

        // Main Bedroom (top right) walls
        for (y in 10..35) {
            setCell(70, y, isWalkable = false)
        }
        for (x in 70..110) {
            setCell(x, 35, isWalkable = false)
        }

        // Bath walls (between bedrooms)
        for (x in 70..85) {
            setCell(x, 45, isWalkable = false)
        }
        for (y in 35..55) {
            setCell(85, y, isWalkable = false)
        }

        // Lower Bedroom walls
        for (y in 55..80) {
            setCell(70, y, isWalkable = false)
        }

        // Study Room walls
        for (y in 70..95) {
            setCell(85, y, isWalkable = false)
        }
        for (x in 85..110) {
            setCell(x, 70, isWalkable = false)
        }

        // Foyer/Porch area walls
        for (x in 60..75) {
            setCell(x, 80, isWalkable = false)
        }
    }

    private fun addNavigablePath() {
        // DINING ROOM - Horizontal path
        for (x in 12..38) {
            setCell(x, 20, isNavigablePath = true)
        }

        // COVERED PATIO connection
        for (x in 38..52) {
            setCell(x, 18, isNavigablePath = true)
        }

        // MAIN BEDROOM - Horizontal path
        for (x in 52..108) {
            setCell(x, 18, isNavigablePath = true)
        }

        // LIVING ROOM - Vertical path (main corridor)
        for (y in 20..62) {
            setCell(38, y, isNavigablePath = true)
        }

        // LOWER BEDROOM - Horizontal path to point B
        for (x in 38..95) {
            setCell(x, 62, isNavigablePath = true)
        }
    }

    private fun defineRooms() {
        // Dining Room
        setRoomArea("dining", 11, 11, 34, 34)

        // Living Room
        setRoomArea("living", 26, 36, 59, 79)

        // Covered Patio
        setRoomArea("patio", 36, 11, 54, 24)

        // Main Bedroom (top right)
        setRoomArea("bedroom_main", 71, 11, 109, 34)

        // Lower Bedroom (where point A is)
        setRoomArea("bedroom_lower", 71, 46, 109, 79)

        // Bath rooms
        setRoomArea("bath_upper", 86, 36, 100, 44)
        setRoomArea("bath_lower", 86, 46, 100, 54)

        // Study Room
        setRoomArea("study", 86, 71, 109, 94)

        // Foyer
        setRoomArea("foyer", 61, 71, 84, 94)

        // Pantry
        setRoomArea("pantry", 11, 36, 24, 49)

        // Storage
        setRoomArea("storage", 11, 51, 24, 64)

        // Garage
        setRoomArea("garage", 11, 66, 29, 94)
    }

    private fun setRoomArea(roomId: String, x1: Int, y1: Int, x2: Int, y2: Int) {
        for (y in y1..y2) {
            for (x in x1..x2) {
                if (x in 0 until width && y in 0 until height) {
                    val cell = grid[y][x]
                    grid[y][x] = cell.copy(roomId = roomId)
                }
            }
        }
    }

    fun setCell(x: Int, y: Int, isWalkable: Boolean? = null, isNavigablePath: Boolean? = null) {
        if (x in 0 until width && y in 0 until height) {
            val cell = grid[y][x]
            grid[y][x] = cell.copy(
                isWalkable = isWalkable ?: cell.isWalkable,
                isNavigablePath = isNavigablePath ?: cell.isNavigablePath
            )
        }
    }

    fun getCell(x: Int, y: Int): GridCell? {
        return if (x in 0 until width && y in 0 until height) {
            grid[y][x]
        } else null
    }

    fun isWalkable(position: Position): Boolean {
        return getCell(position.x, position.y)?.isWalkable ?: false
    }

    fun isNavigablePath(position: Position): Boolean {
        return getCell(position.x, position.y)?.isNavigablePath ?: false
    }

    fun getNavigablePathCells(): List<Position> {
        val pathCells = mutableListOf<Position>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (grid[y][x].isNavigablePath) {
                    pathCells.add(Position(x, y))
                }
            }
        }
        return pathCells
    }

    fun getNeighbors(position: Position): List<Position> {
        val neighbors = mutableListOf<Position>()
        val directions = listOf(
            Position(0, -1), Position(1, 0),
            Position(0, 1), Position(-1, 0),
            Position(1, -1), Position(1, 1),
            Position(-1, 1), Position(-1, -1)
        )

        for (dir in directions) {
            val newPos = Position(position.x + dir.x, position.y + dir.y)
            if (isWalkable(newPos)) {
                neighbors.add(newPos)
            }
        }
        return neighbors
    }
}