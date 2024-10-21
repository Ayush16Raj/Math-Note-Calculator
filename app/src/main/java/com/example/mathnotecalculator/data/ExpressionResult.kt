package com.example.mathnotecalculator.data

import android.graphics.PointF

data class ExpressionResult(
    val expression: String,
    val result: String,
    val position: PointF
)
