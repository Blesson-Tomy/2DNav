package com.example.a2dnav.models

data class GridCell(
    val x: Int,
    val y: Int,
    val isWalkable: Boolean = true,
    val isNavigablePath: Boolean = false,
    val roomId: String? = null
)