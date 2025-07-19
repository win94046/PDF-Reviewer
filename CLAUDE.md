# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application built with Kotlin and Jetpack Compose following strict clean architecture and MVVM patterns. The project uses single-activity architecture and adheres to specific coding standards defined in .cursorrules.

**Key Technologies:**
- Kotlin 1.9.0 (official code style)
- Android API 24-34 (minSdk 24, targetSdk 34)
- Jetpack Compose with Material 1 (androidx.compose.material)
- Gradle with Kotlin DSL and version catalogs
- Hilt for dependency injection

## Build Commands

### Development
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Clean build
./gradlew clean
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.test.myapplication.ExampleUnitTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific instrumented test
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.test.myapplication.ExampleInstrumentedTest
```

### Code Quality
```bash
# Check for linting issues
./gradlew lint

# Check for linting issues with detailed report
./gradlew lint --continue
```

## Architecture & Structure

### Mandatory Architecture (per .cursorrules)
**Clean Architecture:** Repository pattern for data persistence, MVVM pattern for state management
**Single Activity:** Only MainActivity as entry point - no fragments or XML layouts
**UI Framework:** Jetpack Compose exclusively with Material 1 (androidx.compose.material)
**Navigation:** NavController/NavHost from androidx.navigation.compose
**Dependency Injection:** Hilt for ViewModel, Repository, and UseCase

### Required Screen Flow
```
SplashScreen (entry route)
├── Authentication Flow
│   ├── Login
│   ├── Register  
│   ├── Forgot Password
│   └── Verify Email
└── Main App (BottomNavigation)
    ├── Home
    ├── Profile
    ├── Settings
    ├── Patients
    └── Appointments
```

### Mandatory File Organization
```
ui/
├── screens/           # Screen-based folder structure
│   ├── splash/
│   ├── auth/         # login, register, forgot_password, verify_email
│   ├── home/
│   ├── profile/
│   ├── settings/
│   ├── patients/
│   └── appointments/
├── components/        # Reusable UI components
├── theme/            # Theme configuration
└── navigation/       # NavGraph with sealed class Screen(route: String)
```

### Required Component Structure (per feature)
Each feature module must include:
- Screen Composable (@Composable function)
- ViewModel (exposes StateFlow or MutableState)
- UiState (sealed class)
- UiEvent (sealed class) 
- UiEffect (sealed class)
- onEvent(event: UiEvent) handler

### Layout Requirements
- Use Box, Column, Row, Scaffold, LazyColumn exclusively
- NO ConstraintLayout, XML layouts, or ViewBinding
- NO fragments whatsoever

## Strict Development Guidelines (from .cursorrules)

### Mandatory Kotlin Standards
**Type Safety:**
- Always declare type of each variable and function (parameters and return value)
- Avoid using `any` - create necessary types instead
- Use data classes for data, avoid primitive type abuse

**Naming Conventions:**
- PascalCase for classes
- camelCase for variables, functions, methods
- underscores_case for file and directory names  
- UPPERCASE for environment variables
- Boolean variables: isLoading, hasError, canDelete, etc.
- Functions start with verbs, use complete words (no abbreviations except standard ones)

**Function Requirements:**
- Write short functions with single purpose (< 20 instructions)
- No blank lines within functions
- Avoid nesting blocks with early returns and utility functions
- Use higher-order functions (map, filter, reduce) to avoid nesting
- Single level of abstraction per function
- Default parameter values instead of null checks

**Class Requirements:**
- Follow SOLID principles strictly
- Prefer composition over inheritance
- Declare interfaces to define contracts
- Small classes: < 200 instructions, < 10 public methods, < 10 properties

### State Management Pattern (Mandatory)
**ViewModel Pattern:**
- ViewModel exposes StateFlow or MutableState for UI state
- Use SharedFlow for effects (Toast, Navigation)
- UI observes state with collectAsState()
- Handle user actions with onEvent(event: UiEvent)

**State Classes (Required):**
```kotlin
sealed class UiState
sealed class UiEvent  
sealed class UiEffect
```

**State Hoisting:**
- Hoist UI state to enable Composable reusability
- Use rememberSaveable for local UI state

### Testing Requirements (from .cursorrules)
**Unit Testing:**
- Follow Arrange-Act-Assert convention
- Test variables: inputX, mockX, actualX, expectedX
- Write unit tests for each public function
- Use test doubles to simulate dependencies
- Test all ViewModel state and event logic with fake repositories and test dispatchers

**UI Testing:**
- Use Compose UI testing from androidx.compose.ui.test
- Use HiltAndroidRule for instrumented tests
- Write integration tests for each feature module

**Acceptance Testing:**
- Follow Given-When-Then convention for acceptance tests

### Exception Handling (from .cursorrules)
**Exception Strategy:**
- Use exceptions only for errors you don't expect
- Catch exceptions only to fix expected problems or add context
- Otherwise use global handler
- Never suppress exceptions silently

### Data Management (from .cursorrules)
**Data Principles:**
- Prefer immutability for data
- Use readonly for data that doesn't change
- Use val for literals that don't change
- Avoid data validations in functions - use classes with internal validation

## Current State

This is a fresh Android project template that needs to be restructured according to .cursorrules requirements. Current implementation contains basic MainActivity with Compose setup but requires:

**Missing Architecture:**
- Clean architecture layers (data, domain, presentation)
- Repository pattern implementation
- Hilt dependency injection setup
- Required screen flow (Splash → Auth → Main with BottomNavigation)
- Proper package structure with screen-based organization

**Required Next Steps:**
- Implement mandatory screen structure per .cursorrules
- Add Hilt dependencies and setup
- Create sealed classes for navigation routes
- Implement MVVM pattern with proper state management
- Add required screens: Splash, Auth flow, Main app with BottomNavigation