package com.example.taganroggo.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.fonts.FontFamily
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.example.taganroggo.FirebaseAPI
import com.example.taganroggo.Place
import com.example.taganroggo.R
import com.example.taganroggo.Users
import com.example.taganroggo.databinding.ComentBinding
import com.example.taganroggo.databinding.PlaceBinding
import com.example.taganroggo.databinding.RatingBinding
import com.google.android.material.internal.ContextUtils.getActivity
import com.squareup.picasso.Picasso


class RaitAdapter: RecyclerView.Adapter<RaitAdapter.RaitHolder>() {
    private var UserList=ArrayList<Users>()

    class RaitHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = RatingBinding.bind(item)
        fun bind(user: Users, position: Int) = with(binding){
            FirebaseAPI().getPicLogo(user.ava) {
                Picasso.get().load(it).into(imageView7)
            }
            textView13.text = user.name
            textView14.text = user.score.toString()
            textView18.text = (position + 1).toString()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun createElement(partner: Users){
        UserList.add(partner)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaitHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.rating,parent,false)
        return  RaitHolder(view)
    }

    override fun onBindViewHolder(holder: RaitHolder, position: Int) {
        holder.bind(UserList[position], position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun createAll(userList: MutableList<Users>){
        deleter()
        val userList2 = mutableListOf<Users>()
        userList2.forEach {
            userList2.add(it)
        }
        UserList = userList2 as ArrayList<Users>
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleter(){
        UserList.removeAll(UserList.toSet())
    }

    override fun getItemCount(): Int {
        return UserList.count()
    }
}