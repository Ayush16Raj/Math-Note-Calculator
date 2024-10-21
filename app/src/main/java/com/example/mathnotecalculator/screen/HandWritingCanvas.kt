package com.example.mathnotecalculator.screen

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import com.example.mathnotecalculator.R
import com.example.mathnotecalculator.viewmodel.MathNoteViewModel

@Composable
fun HandwritingCanvas(
    viewModel: MathNoteViewModel, // Passed to handle business logic and manage state like strokes
    modifier: Modifier = Modifier
) {

    val path = remember { Path() } // Path used to draw lines representing user handwriting

    // Observe the list of strokes from the ViewModel
    val strokes = viewModel.strokes
    val expressionResults = viewModel.expressionResults

    // Maintain zoom scale
    val scale = remember { mutableStateOf(1f) }

    val strokeColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    // Clear and rebuild the path whenever strokes are updated
    path.reset()
    strokes.forEach { (strokePoints,result) -> // Loop through each stroke
        if (strokePoints.isNotEmpty()) {
            path.moveTo(
                strokePoints[0].x,
                strokePoints[0].y
            ) // Move to the first point of the stroke
            strokePoints.drop(1).forEach { point -> // Drop the first point and connect
                path.lineTo(point.x, point.y) // Connect each subsequent point through lineTo()
            }
        }
    }

        // Canvas with zoom feature
        Canvas(
            modifier = modifier
                // Detect pinch to zoom gesture
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        scale.value *= zoomChange // Adjust the zoom level based on pinch gesture
                    }
                }
                // Detect drag gestures for drawing
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.startNewStroke(
                                PointF(
                                    offset.x / scale.value,
                                    offset.y / scale.value
                                )
                            )
                        },
                        onDrag = { change, _ ->
                            viewModel.addPointToCurrentStroke(
                                PointF(
                                    change.position.x / scale.value,
                                    change.position.y / scale.value
                                )
                            )
                        },
                        onDragEnd = {
                            viewModel.completeCurrentStroke()
                        }
                    )
                }
                // Apply the scale (zoom) to the canvas
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value
                )
        ) {
            // Draw the handwriting
            drawPath(
                path,
                strokeColor,
                style = Stroke(width = 8f / scale.value)
            ) // Adjust stroke width based on scale


            // Draw the result near the last handwritten stroke
            expressionResults.forEach { result ->
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        result.result,
                        (result.position.x + 20) , // Position slightly to the right of the last stroke
                        (result.position.y) , //
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.RED // Color of the result text
                            textSize = 100f  // Adjust text size based on scale
                        }
                    )
                }
            }
        }
    Box{
        IconButton(onClick = {
            viewModel.clearCanvas()
        },
            modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(painter = painterResource(R.drawable.baseline_delete_24), contentDescription = "Delete",
                tint = strokeColor)

        }
    }

}
