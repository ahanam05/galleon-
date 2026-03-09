# Galleon

A comprehensive expense tracking application for Android that helps users monitor and manage their daily, weekly, and monthly spending.

## About

Galleon is a modern expense tracking application designed to provide users with detailed insights into their spending patterns. The app enables users to record expenses, set monthly budgets, and visualize their financial data across different time periods. Built with Jetpack Compose and Firebase, Galleon offers a clean, intuitive interface for managing personal finances with real-time synchronization across devices.

## Features

- **Multi-period Expense Tracking**: View and manage expenses by day, week, or month with intuitive navigation controls
- **Budget Management**: Set monthly budgets and track spending against targets with visual indicators
- **Expense Analytics**: Analyze spending patterns with daily breakdowns, weekly averages, and monthly comparisons
- **Category Organization**: Categorize expenses for better financial insights and identify top spending categories
- **Firebase Authentication**: Secure user authentication with Google Sign-In support
- **Cloud Synchronization**: Real-time expense data synchronization across devices using Cloud Firestore
- **Interactive Date Selection**: Navigate through time periods or use the date picker for quick access to specific dates

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture Components**: ViewModel, StateFlow, Coroutines
- **Dependency Injection**: Hilt (Dagger)
- **Backend Services**: Firebase Authentication, Cloud Firestore
- **Build System**: Gradle with Kotlin DSL
- **Asynchronous Programming**: Kotlin Coroutines and Flow

## Architecture

Galleon follows the **MVVM (Model-View-ViewModel)** architecture pattern with a clear separation of concerns:

- **UI Layer**: Jetpack Compose screens and components with state management
- **ViewModel Layer**: Business logic and UI state handling using StateFlow
- **Repository Layer**: Data access abstraction for Firebase Firestore operations
- **Data Layer**: Domain models and data aggregation logic

The app uses **Unidirectional Data Flow** where the UI observes state from ViewModels, user actions trigger ViewModel methods, and ViewModels update the state which flows back to the UI.
