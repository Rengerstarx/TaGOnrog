package com.example.taganroggo.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taganroggo.FirebaseAPI
import com.example.taganroggo.Data.Place
import com.example.taganroggo.R
import com.example.taganroggo.Data.Users
import com.example.taganroggo.databinding.ComentBinding
import com.squareup.picasso.Picasso


class ComentAdapter: RecyclerView.Adapter<ComentAdapter.ComentHolder>() {
    private var PlaceList=ArrayList<String>()
    private var UserList=ArrayList<Users>()

    class ComentHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = ComentBinding.bind(item)
        fun bind(place: String, user: Users) = with(binding){
            FirebaseAPI().getPicLogo(user.ava) {
                Picasso.get().load(it).into(imageView7)
            }
            textView13.text = user.name
            textView14.text = place
        }
    }

    fun addComent(str: String) {
        PlaceList.add(str)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun createElement(partner: Users, str: String){
        PlaceList.add(str)
        UserList.add(partner)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.coment,parent,false)
        return  ComentHolder(view)
    }

    override fun onBindViewHolder(holder: ComentHolder, position: Int) {
        holder.bind(PlaceList[position], UserList[position])
    }

    override fun getItemCount(): Int {
        return PlaceList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun createAll(partnerList: MutableList<String>, userList: MutableList<Users>){
        deleter()
        val partnerList2 = mutableListOf<String>()
        val userList2 = mutableListOf<Users>()
        partnerList.forEach {
            partnerList2.add(it)
        }
        userList.forEach {
            userList2.add(it)
        }
        PlaceList = partnerList2 as ArrayList<String>
        UserList = userList2 as ArrayList<Users>
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleter(){
        PlaceList.removeAll(PlaceList.toSet())
        UserList.removeAll(UserList.toSet())
    }

    interface Listener{
        fun onClick(partner: Place)
    }
}