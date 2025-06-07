// MyApplication.kt
package com.example.chickeninvaders

import android.app.Application
import com.example.chickeninvaders.utils.ImageLoader
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // מתחילים את FirebaseApp ברגע שהאפליקציה עולה
       FirebaseApp.initializeApp(this)
        ImageLoader.init(this)
    }
}
