package com.example.drawer

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.concurrent.thread

class MessageActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val intent = getIntent()
        val spLogin: String? = intent.getStringExtra("splogin")
        val messageLogin: String? = intent.getStringExtra("login")
        val message: String? = intent.getStringExtra("message")
        val id: String? = intent.getStringExtra("id")

        val tvLLoggedAs = findViewById<TextView>(R.id.textViewLoggedAs)
        val tvMessageOwner = findViewById<TextView>(R.id.textViewMsgOwner)
        val etMessage = findViewById<EditText>(R.id.editTextTextMessage)
        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val buttonDelete = findViewById<Button>(R.id.buttonDelete)
        val buttonGoBack = findViewById<Button>(R.id.buttonGoBack)
        val snackbarView = findViewById<LinearLayout>(R.id.snackbarLayout)

        tvLLoggedAs.text = "Logged as $spLogin"
        tvMessageOwner.text = "This message was sent by $messageLogin"
        etMessage.setText(message)


        buttonSave.setOnClickListener(){
            if(checkInternetConnection()){
                val newMessage: String = etMessage.text.toString()
                if(spLogin==messageLogin){
                    Toast.makeText(this,"Message saved",Toast.LENGTH_SHORT).show()
                    thread {
                        httpPutRequest(newMessage, spLogin.toString(), id.toString())
                    }
                    toMainActivity()
                }else{
                    Snackbar.make(snackbarView, "This is not your message!", Snackbar.LENGTH_LONG).setAnchorView(snackbarView).show()
                }
            }else{
                Snackbar.make(snackbarView, "No internet connection!", Snackbar.LENGTH_LONG).setAnchorView(snackbarView).show()
            }
        }

        buttonDelete.setOnClickListener(){
            if(checkInternetConnection()){
                if(spLogin==messageLogin){
                    Toast.makeText(this,"Message deleted",Toast.LENGTH_SHORT).show()
                    thread {
                        httpDeleteRequest(id.toString())
                    }
                    toMainActivity()
                }else{
                    Snackbar.make(snackbarView, "This is not your message!", Snackbar.LENGTH_LONG).setAnchorView(snackbarView).show()
                }
            }else{
                    Snackbar.make(snackbarView, "No internet connection!", Snackbar.LENGTH_LONG).setAnchorView(snackbarView).show()
            }
        }

        buttonGoBack.setOnClickListener(){
            toMainActivity()
        }

    }

    fun httpPutRequest(newMessage :String, login: String, id: String){

        val json = """
        {
            "content": "$newMessage",
            "login": "$login"
        }
        """
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://tgryl.pl/shoutbox/message/$id")
            .put(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            println(response.body!!.string())
            Log.d("response",response.message)
        }
    }

    fun httpDeleteRequest(id: String) {

        val request = Request.Builder()
            .url("http://tgryl.pl/shoutbox/message/$id")
            .delete()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            println(response.body!!.string())
            Log.d("response", response.message)
        }
    }

    fun toMainActivity(){
        val intent = Intent(this@MessageActivity, MainActivity::class.java)
        startActivity(intent)
    }

    fun checkInternetConnection(): Boolean {
        val cm =  baseContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}