# Search File IntelliJ Plugin

## Description
A custom IntelliJ IDEA plugin that allows users to search for text occurrences across files in a specified directory directly within the IDE. It provides a simple UI with fields for the directory path and search query, displays matching results in a list, and lets users navigate to the exact line in the corresponding file. The plugin supports cancelable, asynchronous searches using Kotlin coroutines and Flow, ensuring responsive performance even with large codebases.

## Key Features
- Search for text across multiple files in a directory.
- Display results with file name, line number, and character offset.
- Navigate to the exact occurrence in the IDE.
- Cancelable search operation to stop long-running searches.
- Responsive UI leveraging coroutines and Flow for asynchronous processing.
