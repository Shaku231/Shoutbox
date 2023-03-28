package com.example.drawer

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.drawer.databinding.ActivityMainBinding
import com.example.drawer.ui.SwipeToDeleteCallback
import com.example.drawer.ui.settings.SettingsActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<SB_RecyclerViewAdapter.MyViewHolder>? = null
    val arrayList = ArrayList<Model>()
    var context : Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_settings
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val login = getLoginFromSharedPreferences()
        if(login == null){
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        context = baseContext

        val messageInput = findViewById<TextInputEditText>(R.id.messageInput)
        val sendMessageButton = findViewById<AppCompatButton>(R.id.buttonSendMsg)
        val recyclerView = findViewById<RecyclerView>(R.id.mRecyclerView)
        sendMessageButton.setOnClickListener(){
            if(checkInternetConnection()){
                val message: String = messageInput.text.toString()
                if(message.isNullOrEmpty()){
                    val toast = Toast.makeText(applicationContext,"Message cannot be empty",Toast.LENGTH_SHORT)
                    toast.show()
                }else{
                    Thread {
                        httpPostRequest(message,login.toString())
                    }.start()

                    val toast = Toast.makeText(applicationContext,"Message sent",Toast.LENGTH_SHORT)
                    toast.show()
                }
                sendMessageButton.onEditorAction(EditorInfo.IME_ACTION_DONE)
                messageInput.setText("")
            }else{
                Snackbar.make(recyclerView, "No internet connection!", Snackbar.LENGTH_LONG).show()
            }
        }

        val swipeToDeleteCallback = object : SwipeToDeleteCallback(applicationContext){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(checkInternetConnection()){
                    val position = viewHolder.adapterPosition
                    Log.d("position", position.toString())
                    if(login == arrayList[position].login){
                        thread {
                            httpDeleteRequest(arrayList[position].id)
                        }
                        val handler = Handler()
                        handler.postDelayed({
                            arrayList.removeAt(position)
                            recyclerView.adapter?.notifyDataSetChanged()
                            Snackbar.make(recyclerView, "Message deleted!", Snackbar.LENGTH_LONG).show()
                        }, 250)
                    }else{
                        recyclerView.adapter?.notifyDataSetChanged()
                        Snackbar.make(recyclerView, "This is not your message!", Snackbar.LENGTH_LONG).show()
                    }
                }else{
                    recyclerView.adapter?.notifyDataSetChanged()
                    Snackbar.make(recyclerView, "No internet connection!", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)

        itemTouchHelper.attachToRecyclerView(recyclerView)

        thread {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    if(checkInternetConnection()){
                        httpGetRequest()
                    }else{
                        Snackbar.make(recyclerView, "No internet connection!", Snackbar.LENGTH_LONG).show()
                    }
                    Thread.sleep(20000)
                }
            }catch (e: InterruptedException){
                e.printStackTrace()
            }
        }

        refreshApp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun refreshApp(){
        val stf = findViewById<SwipeRefreshLayout>(R.id.swipeToRefresh)

        stf.setOnRefreshListener(){
            httpGetRequest()
            val recyclerView = findViewById<RecyclerView>(R.id.mRecyclerView)
            recyclerView.adapter?.notifyDataSetChanged()
            Toast.makeText(this,"Refreshed",Toast.LENGTH_SHORT).show()
            stf.isRefreshing = false
        }
    }

    fun httpGetRequest(){
        val request = Request.Builder()
            .url("http://tgryl.pl/shoutbox/messages")
            .build()
        arrayList.clear()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    var rs = response.body?.string()

                    try{
                        var tabJ = JSONArray(rs)
                        Log.d("tabJ", tabJ[tabJ.length() -1].toString())
                        for(i in tabJ.length()-1 downTo 0)         {
                            arrayList.add(Model(
                                tabJ.getJSONObject(i).getString("login"),
                                tabJ.getJSONObject(i).getString("date"),
                                tabJ.getJSONObject(i).getString("content"),
                                tabJ.getJSONObject(i).getString("id")
                            ))
                        }
                    }catch (e: JSONException){
                        e.printStackTrace()
                    }
                    runOnUiThread {
                        layoutManager = LinearLayoutManager(context)
                        val recyclerView = findViewById<RecyclerView>(R.id.mRecyclerView)
                        recyclerView.layoutManager = layoutManager
                        adapter = SB_RecyclerViewAdapter(arrayList, context!!)
                        recyclerView.adapter = adapter
                        (adapter as SB_RecyclerViewAdapter).setOnItemClickListener(object : SB_RecyclerViewAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                val intent = Intent(this@MainActivity,MessageActivity::class.java)
                                intent.putExtra("login",arrayList[position].login)
                                intent.putExtra("message",arrayList[position].message)
                                intent.putExtra("splogin",getLoginFromSharedPreferences())
                                intent.putExtra("id",arrayList[position].id)
                                startActivity(intent)
                            }
                        })
                    }
                }
            }
        })
    }

    fun httpPostRequest(message :String, login: String){

        val json = """
        {
            "content": "$message",
            "login": "$login"
        }
        """
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("http://tgryl.pl/shoutbox/message")
            .post(requestBody)
            .build()


        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            println(response.body!!.string())
        }
    }

    fun getLoginFromSharedPreferences(): String? {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)

        return sharedPreferences.getString("login", null)
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
    fun checkInternetConnection(): Boolean {
        val cm =  context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}