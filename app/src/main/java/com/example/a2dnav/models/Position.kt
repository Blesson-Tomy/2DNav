package com.example.a2dnav.models

data class Position(
    val x: Int,
    val y: Int
) {
    fun distanceTo(other: Position): Double {
        val dx = x - other.x
        val dy = y - other.y
        return Math.sqrt((dx * dx + dy * dy).toDouble())
    }
}