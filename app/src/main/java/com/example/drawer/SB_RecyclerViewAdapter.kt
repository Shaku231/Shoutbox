package com.example.drawer

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.w3c.dom.Text

//import com.example.drawer.SB_RecyclerViewAdapter.MyViewHolder.itemView


class SB_RecyclerViewAdapter(private var models: ArrayList<Model>, private val context: Context) : RecyclerView.Adapter<SB_RecyclerViewAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener
    interface onItemClickListener{

        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_view_row, parent, false,)

        return MyViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvLogin.setText(models.get(position).login)
        holder.tvDate.setText(models.get(position).date)
        holder.tvMessage.setText(models.get(position).message)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    inner class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        var tvLogin: TextView
        var tvDate: TextView
        var tvMessage: TextView

        init{
            tvLogin = itemView.findViewById(R.id.textViewLogin)
            tvDate = itemView.findViewById(R.id.textViewDate)
            tvMessage = itemView.findViewById(R.id.textVievMsg)

            itemView.setOnClickListener(){
                if(checkInternetConnection()){
                    listener.onItemClick(adapterPosition)
                }else{
                    Snackbar.make(itemView, "No internet connection!", Snackbar.LENGTH_LONG).show()

                }

            }
        }
    }

    fun checkInternetConnection(): Boolean {
        val cm =  context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }
}
