package com.example.a2dnav.models

data class PathNode(
    val position: Position,
    var gCost: Double = Double.MAX_VALUE,
    var hCost: Double = 0.0,
    var parent: PathNode? = null
) {
    val fCost: Double
        get() = gCost + hCost
}