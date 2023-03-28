package com.example.drawer.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.drawer.MainActivity
import com.example.drawer.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val loginInput = findViewById<TextInputEditText>(R.id.TextInputLogin)
        val setLogin = findViewById<Button>(R.id.buttonSetLogin)
        var savedLogin: String

        setLogin.setOnClickListener {
            savedLogin = loginInput.text.toString()
            if(savedLogin.isNullOrEmpty()){
                val snack = Snackbar.make(it,"This is a simple Snackbar",Snackbar.LENGTH_LONG)
                snack.show()
            }else{
                Log.d("savedlogin",savedLogin)
                saveLoginToSharedPreferences(savedLogin)
                checkLoginAndGoToNewActivity()
            }
        }
    }

    fun saveLoginToSharedPreferences(savedLogin:String){
        val sharedPref = getSharedPreferences("shared preferences",Context.MODE_PRIVATE)
        sharedPref.edit().putString("login", savedLogin).apply()
    }

    fun getLoginFromSharedPreferences(): String? {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)

        return sharedPreferences.getString("login", null)
    }

    fun checkLoginAndGoToNewActivity() {
        val login = getLoginFromSharedPreferences()

        if (!(login.isNullOrEmpty())) {
            Log.d("login", login.toString())
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}