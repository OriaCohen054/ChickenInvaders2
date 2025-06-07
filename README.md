# Chicken Invaders Mobile Game

**Course:** Mobile Application Development II (AFAkh 2025)  
**Assignment:** Homework 2 – Chicken Invaders Game

---

## Table of Contents
1. [Overview](#overview)  
2. [Features](#features)  
3. [Screens](#screens)  
4. [Architecture & Code Structure](#architecture--code-structure)  
5. [Dependencies](#dependencies)  
6. [Installation & Running](#installation--running)  
7. [Configuration & Firebase Setup](#configuration--firebase-setup)  
8. [Controls](#controls)  
9. [Notes & Todo](#notes--todo)  
10. [License](#license)

---

## Overview
Chicken Invaders is a tilt-controlled arcade-style game where chickens descend row by row, and the player must dodge them with a spaceship at the bottom. Bonus chickens (coins) grant points. Distance traveled is tracked as an odometer. High scores along with last game location are saved to Firebase Firestore.

## Features
- **Tilt Controls**:  
  - Left/right tilt moves the spaceship between columns.  
  - Forward/backward tilt speeds up or slows down the game (spawn rate and animation).
- **Lives & Collision**:  
  - 3 lives represented by hearts.  
  - Collision feedback with vibration and sound.
- **Scoring & Odometer**:  
  - Points awarded for catching bonus chickens.  
  - Distance tracked per second as a background “odometer.”
- **Sound & Music**:
  - Background music per screen.  
  - Sound effects for collisions and bonus collection.
- **High Scores**:  
  - Top 10 scores fetched from Firebase Firestore.  
  - Each record stores score, odometer value, latitude & longitude, timestamp.
- **High Scores Screen**:  
  - Table of top 10 records.  
  - Embedded Google Map showing the location of the selected record.
- **Splash Animation**:  
  - Lottie-based opening animation.
- **Persistent Data**:  
  - Firebase initialized in `MyApplication`.  
  - Firestore rules must allow read/write for simplicity in testing.

## Screens
1. **Splash Animation** (`AnimationActivity`)  
2. **Home Screen** (`HomeFragment`)  
3. **Game Screen** (`GameFragment`)  
4. **Finish Screen** (`FinishFragment`)  
5. **High Scores & Map** (`HighScoresParentFragment`) containing:  
   - `HighScoresFragment` (RecyclerView)  
   - `MapFragment` (Google Map)

## Architecture & Code Structure
- **MVVM Pattern**:
  - `ViewModel` classes manage game state and live data.
- **Fragments & Navigation**:
  - Single-activity architecture (`MainActivity` with `fragment_container`).
- **Handlers & Runnables**:
  - Game loop (`spawnHandler`) and distance loop (`distanceHandler`) properly cleaned up in `onPause`/`onCleared`.
- **Firebase Integration**:
  - `GameFragment` saves each finished game.
  - `HighScoresViewModel` queries top 10 from Firestore.
- **Location Permissions**:
  - `ACCESS_FINE_LOCATION` requested at game end to record last position.

## Dependencies
- AndroidX Core, AppCompat, ConstraintLayout, Lifecycle, Fragment, GridLayout  
- Material Design  
- Google Play Services Location  
- Firebase Firestore (BoM)  
- Lottie for animation  
- Google Maps SDK for Android  

## Installation & Running
1. Clone the repository.  
2. Configure your `google-services.json` in `app/`.  
3. Ensure Firebase project with Firestore enabled.  
4. Build and run on an Android device (min API 26).  
5. Grant location permission when prompted at game over.

## Configuration & Firebase Setup
- Add `google-services.json` from your Firebase console.  
- In `AndroidManifest.xml`, ensure `android:name=".MyApplication"`.  
- Firestore Security Rules (for testing):
  ```js
  service cloud.firestore {
    match /databases/{database}/documents {
      match /scores/{docId} {
        allow read, write: if true;
      }
    }
  }
  ```

## Controls
- Tilt phone left/right: move spaceship.  
- Tilt phone forward/backward: adjust game speed.  
- Touch icons/buttons for navigation.

## Notes & Todo
- Implement Firestore rules for production.  
- Polish Lottie splash timing.  
- Add authentication for personalized high scores.  
- Improve collision detection for smoother gameplay.

## License
MIT License. Feel free to use and modify for learning purposes.
