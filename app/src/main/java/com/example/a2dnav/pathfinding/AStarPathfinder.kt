package com.example.a2dnav.pathfinding

import com.example.a2dnav.map.MapManager
import com.example.a2dnav.models.PathNode
import com.example.a2dnav.models.Position
import java.util.PriorityQueue

class AStarPathfinder(private val mapManager: MapManager) {

    fun findPath(start: Position, goal: Position): List<Position>? {
        val openSet = PriorityQueue<PathNode>(compareBy { it.fCost })
        val closedSet = mutableSetOf<Position>()
        val allNodes = mutableMapOf<Position, PathNode>()

        val startNode = PathNode(start, gCost = 0.0, hCost = heuristic(start, goal))
        allNodes[start] = startNode
        openSet.add(startNode)

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            if (current.position == goal) {
                return reconstructPath(current)
            }

            closedSet.add(current.position)

            for (neighborPos in mapManager.getNeighbors(current.position)) {
                if (neighborPos in closedSet) continue

                val moveCost = if (isDiagonal(current.position, neighborPos)) 1.414 else 1.0
                val tentativeGCost = current.gCost + moveCost

                val neighborNode = allNodes.getOrPut(neighborPos) {
                    PathNode(neighborPos, hCost = heuristic(neighborPos, goal))
                }

                if (tentativeGCost < neighborNode.gCost) {
                    neighborNode.parent = current
                    neighborNode.gCost = tentativeGCost

                    if (neighborNode !in openSet) {
                        openSet.add(neighborNode)
                    }
                }
            }
        }

        return null
    }

    private fun isDiagonal(from: Position, to: Position): Boolean {
        return from.x != to.x && from.y != to.y
    }

    private fun heuristic(from: Position, to: Position): Double {
        return from.distanceTo(to)
    }

    private fun reconstructPath(endNode: PathNode): List<Position> {
        val path = mutableListOf<Position>()
        var current: PathNode? = endNode

        while (current != null) {
            path.add(current.position)
            current = current.parent
        }

        return path.reversed()
    }
}