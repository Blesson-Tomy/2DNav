package com.example.a2dnav.navigation

import com.example.a2dnav.map.MapManager
import com.example.a2dnav.models.Position
import com.example.a2dnav.pathfinding.AStarPathfinder

class NavigationManager(private val mapManager: MapManager) {
    private val pathfinder = AStarPathfinder(mapManager)
    private var currentPath: List<Position>? = null

    fun findNearestNavigablePoint(currentPosition: Position): Position? {
        val navigableCells = mapManager.getNavigablePathCells()
        return navigableCells.minByOrNull { currentPosition.distanceTo(it) }
    }

    fun generatePathToNavigablePath(currentPosition: Position): List<Position>? {
        val nearestPathPoint = findNearestNavigablePoint(currentPosition) ?: return null
        currentPath = pathfinder.findPath(currentPosition, nearestPathPoint)
        return currentPath
    }

    fun generateCompletePath(currentPosition: Position, destination: Position): List<Position>? {
        val pathToNavigable = generatePathToNavigablePath(currentPosition) ?: return null
        val lastPointOnPath = pathToNavigable.last()
        val pathToDestination = pathfinder.findPath(lastPointOnPath, destination) ?: return null
        return pathToNavigable + pathToDestination.drop(1)
    }

    fun getCurrentPath() = currentPath
}