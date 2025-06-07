package com.example.chickeninvaders

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.chickeninvaders.databinding.ActivityAnimationBinding  // שימו לב: המיקום של ה־Binding
import com.google.firebase.FirebaseApp

class AnimationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // חשוב מאוד ש־ViewBinding יהיה פעיל ב־build.gradle
        binding = ActivityAnimationBinding.inflate(layoutInflater)

        // אם רוצים שה־Activity יתפרס מתחת לבר השמאלי/ימני/של מערכת (edge-to-edge)
        enableEdgeToEdge()
        setContentView(binding.root)

        // וודא שיש לך ב־activity_animation.xml אלמנט עם id="main"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // מתחילים את האנימציה של ה־Lottie
        startAnimation(binding.animLOTTIELottie)
    }

    private fun startAnimation(lottie: LottieAnimationView) {
        // אם הגדרת ב־XML: app:lottie_autoPlay="true", הפעולה הבאה אינה הכרחית
        lottie.resumeAnimation()
        lottie.repeatCount = 0
        lottie.playAnimation()
        // מאזינים לסיום האנימציה
        lottie.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // אפשר למשוך דאטה מהאינטרנט

            }

            override fun onAnimationEnd(animation: Animator) {
                // עברנו לסיום – נעבור למסך הראשי (MainActivity)
                transactToMainActivity()
            }

            override fun onAnimationCancel(animation: Animator) {
                // ניקוי אשר נחוץ במקרה שהאנימציה בוטלה
            }

            override fun onAnimationRepeat(animation: Animator) {
                // אם רוצים לבדוק האם דטה הושלמה ואז לבטל חזרה (repeat) אינסופית
            }
        })
    }

    private fun transactToMainActivity() {
        startActivity(Intent(this@AnimationActivity, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.animLOTTIELottie.cancelAnimation()
    }
}
