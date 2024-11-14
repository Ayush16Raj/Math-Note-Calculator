# Math Note Calculator

This project is a **handwriting recognition calculator app** that emulates iOS 18's functionality using Firebase ML Kit. The app is designed for users to handwrite mathematical expressions, recognize the text, and evaluate the result in real-time. It supports basic mathematical operations such as addition, subtraction, multiplication, division, and square roots.

## Features

- **Handwriting Recognition**: Allows users to write mathematical expressions with their fingers or stylus.
- **Real-Time Calculation**: Automatically evaluates the expression upon detecting a handwritten `=` symbol.
- **Basic Math Operations**: Supports calculations including:
  - Addition
  - Subtraction
  - Multiplication
  - Division
  - Square root
- **Orientation Handling**: Corrects the orientation of handwritten strokes to ensure proper recognition.
- **Firebase ML Kit Integration**: Uses Firebase's ML Kit for handwriting recognition and basic mathematical parsing.

## How It Works

1. **Handwriting Input**:
   - The user writes expressions directly on the screen.
   - Firebase ML Kit interprets and converts these handwritten strokes into text.

2. **Real-Time Evaluation**:
   - Once the handwriting is detected as a complete expression, the app evaluates it.
   - An `=` trigger in the handwriting signals the app to calculate the result.

3. **Orientation Correction**:
   - The app calculates the angle of the handwritten strokes and corrects any skewed orientation in the background, ensuring smooth and accurate recognition.

## Tech Stack

- **Kotlin**: Main programming language for the app.
- **Jetpack Compose**: UI framework used to build and render the app’s interface.
- **Firebase ML Kit**: Provides handwriting recognition capability for mathematical expressions.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/math-note-calculator.git
   
2. Open the project in Android Studio.
3. Set up Firebase for your project by following the Firebase ML Kit setup guide.
4. Build and run the app on an emulator or physical device.
   
## Usage
Write Expressions: Use your finger or stylus to write basic mathematical expressions.
Automatic Calculation: When you write =, the app calculates the result and displays it.
Supported Operations: Simple arithmetic and square root.

## Future Plans
While this README focuses on the current implementation, there are plans to enhance functionality, including adding advanced calculations, equation solving, and graph generation capabilities.
