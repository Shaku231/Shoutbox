package com.example.drawer

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.drawer.ui.settings.SettingsActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        supportActionBar?.hide()
        val login = getLoginFromSharedPreferences()
        Handler().postDelayed({
            if ((login.isNullOrEmpty())){
                val intent = Intent(this@SplashScreen, SettingsActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this@SplashScreen, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            },3000
        )
    }

    fun getLoginFromSharedPreferences(): String? {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)

        return sharedPreferences.getString("login", null)
    }
}