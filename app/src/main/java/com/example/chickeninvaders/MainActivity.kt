package com.example.chickeninvaders

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // נטען את ה־HomeFragment לתוך ה־fragment_container ברגע הראשון
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment.newInstance())
                .commit()
        }
    }
}
