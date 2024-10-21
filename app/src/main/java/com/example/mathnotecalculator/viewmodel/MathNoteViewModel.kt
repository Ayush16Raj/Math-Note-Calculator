package com.example.mathnotecalculator.viewmodel

import android.graphics.PointF
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.mathnotecalculator.data.ExpressionResult
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MathNoteViewModel : ViewModel() {

    // Store strokes for handwriting
    var strokes = mutableStateListOf<Pair<List<PointF>, String?>>() // A list of stroke where each stroke is
        private set                                             //list of pointF (x,y) coordinates


    var currentStroke = mutableListOf<PointF>()
                                           //temporary list that hold points of current stroke


    // List to track strokes for the current expression
    private var currentExpressionStrokes = mutableListOf<List<PointF>>()

    // List to store recognized expressions and their results
    var expressionResults = mutableStateListOf<ExpressionResult>()
        private set


    fun startNewStroke(startPoint: PointF) {
        // Start a new stroke and clear the previous one
        currentStroke = mutableListOf(startPoint)
        strokes.add(Pair(currentStroke.toList(),null))
    }

    fun addPointToCurrentStroke(point: PointF) {
        // Add points to the ongoing stroke
        currentStroke.add(point)
        strokes.add(Pair(currentStroke.toList(),null))

    }

    fun completeCurrentStroke() {
        // Add the stroke to the list of all strokes (for visual drawing)
        strokes.add(Pair(currentStroke.toList(),null))

        // Add the stroke to the current expression's strokes
        currentExpressionStrokes.add(currentStroke.toList())

        // Clear the current stroke for future input
        currentStroke.clear()

        // Trigger recognition
            applyRotationAndCalculate()

    }

    private val remoteModelManager = RemoteModelManager.getInstance()

    // Check if the model is downloaded
    private fun isModelDownloaded(model: DigitalInkRecognitionModel, onResult: (Boolean) -> Unit) {
        remoteModelManager.isModelDownloaded(model)
            .addOnSuccessListener { isDownloaded ->
                onResult(isDownloaded)
            }
            .addOnFailureListener { e ->
                Log.e("ML Kit", "Error checking if model is downloaded", e)
                onResult(false)
            }
    }

    // Download the model if not already downloaded
    private fun downloadModelIfNeeded(model: DigitalInkRecognitionModel, onComplete: () -> Unit) {
        isModelDownloaded(model) { isDownloaded ->
            if (!isDownloaded) {
                val conditions = DownloadConditions.Builder().build()
                remoteModelManager.download(model, conditions)
                    .addOnSuccessListener {
                        Log.i("ML Kit", "Model downloaded successfully")
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.e("ML Kit", "Error downloading the model", e)
                    }
            } else {
                onComplete() // Model is already downloaded, proceed
            }
        }
    }

    // Correct common mathematical symbols recognized incorrectly by the model
    fun correctMathSymbols(recognizedText: String): String {
        return recognizedText
            .replace("X", "*")  // Replace 'X' with multiplication
            .replace("x", "*")  // Handle lowercase 'x' for multiplication

    }



    // Handwriting recognition using Firebase Digital Ink
    private fun recognizeHandwriting(strokes: List<List<PointF>>, onResult: (String) -> Unit) {
        val inkBuilder = Ink.builder()
        strokes.forEach { strokePoints ->
            val strokeBuilder = Ink.Stroke.builder()
            strokePoints.forEach { point ->
                strokeBuilder.addPoint(Ink.Point.create(point.x, point.y))
            }
            inkBuilder.addStroke(strokeBuilder.build())
        }
        val ink = inkBuilder.build()

        val modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en")
        val model = DigitalInkRecognitionModel.builder(modelIdentifier!!).build()

        // Download the model first
        downloadModelIfNeeded(model) {
            // Model downloaded, now proceed with recognition
            val recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build()
            )

            recognizer.recognize(ink)
                .addOnSuccessListener { result ->
                    onResult(result.candidates[0].text)
                    Log.e("Recognised Text",result.candidates[0].text)
                }
                .addOnFailureListener { e ->
                    Log.e("ML Kit", "Handwriting recognition failed", e)
                }
        }
    }


    // Evaluate the mathematical expression using Exp4j
    fun evaluateMathExpression(expression: String): Double {
        return try {
            val exp = ExpressionBuilder(expression).build()
            exp.evaluate()
        } catch (e: Exception) {
            e.printStackTrace()
            0.0 // Handle error
        }
    }

    // Function to calculate the skew angle of strokes
    fun calculateOrientationAngle(strokes: List<List<PointF>>): Float {
        val firstPoint = strokes.flatten().first()
        val lastPoint = strokes.flatten().last()

        val deltaX = lastPoint.x - firstPoint.x
        val deltaY = lastPoint.y - firstPoint.y
        return atan2(deltaY.toDouble(), deltaX.toDouble()).toFloat()
    }

    // Rotate strokes based on calculated angle to correct orientation
    fun rotateStrokes(strokes: List<List<PointF>>, angle: Float): List<List<PointF>> {
        // Calculate the center point of all strokes
        val allPoints = strokes.flatten()
        val centerX = allPoints.map { it.x }.average().toFloat()
        val centerY = allPoints.map { it.y }.average().toFloat()

        val rotatedStrokes = mutableListOf<List<PointF>>()
        for (stroke in strokes) {
            val rotatedStroke = stroke.map { point ->
                val dx = point.x - centerX
                val dy = point.y - centerY
                PointF(
                    (dx * cos(angle.toDouble()) - dy * sin(angle.toDouble())).toFloat() + centerX,
                    (dx * sin(angle.toDouble()) + dy * cos(angle.toDouble())).toFloat() + centerY
                )
            }
            rotatedStrokes.add(rotatedStroke)
        }
        return rotatedStrokes
    }

    // Apply the rotation correction and recalculate
    fun applyRotationAndCalculate() {
        val angle = calculateOrientationAngle(currentExpressionStrokes)

        // Rotate a COPY of currentExpressionStrokes for recognition, not the visual strokes
        val rotatedStrokes = rotateStrokes(currentExpressionStrokes, -angle) // Rotate for recognition

        // Find the last point of the original strokes for placing the result
        val lastPoint = currentExpressionStrokes.flatten().lastOrNull() ?: PointF(0f, 0f)

        // Use the rotated strokes for recognition
        recognizeHandwriting(rotatedStrokes) { recognizedExpression ->
            val correctedExpression = correctMathSymbols(recognizedExpression)

            if (correctedExpression.contains("=")) {
                val expressionBeforeEqual = correctedExpression.split("=")[0].trim()
                val result = evaluateMathExpression(expressionBeforeEqual).toString()

                // Place the result near the last point of the original strokes
                expressionResults.add(
                    ExpressionResult(
                        expression = expressionBeforeEqual,
                        result = result,
                        position = lastPoint  // Position result at the last point of user’s input
                    )
                )

                // Update the last stroke with the result
                val lastStrokeIndex = strokes.size - 1
                val lastStrokePoints = strokes[lastStrokeIndex].first
                strokes[lastStrokeIndex] = Pair(lastStrokePoints, result)

                currentExpressionStrokes.clear()
            }
        }
    }

    //To clear canvas
    fun clearCanvas() {
        strokes.clear() // Clears all the strokes
        expressionResults.clear() // Clears the calculated results
    }


}
