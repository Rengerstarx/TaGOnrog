package com.example.taganroggo.Adapters.PlaceAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.taganroggo.Adapters.PlacePhotoAdapter
import com.example.taganroggo.Data.Place
import com.example.taganroggo.R
import com.example.taganroggo.databinding.PlaceBinding


class PlaceAdapter(val listener: Listener, val context: Context): RecyclerView.Adapter<PlaceAdapter.PlaceHolder>() {
    private var PlaceList=ArrayList<Place>()

    class PlaceHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = PlaceBinding.bind(item)
        @RequiresApi(Build.VERSION_CODES.Q)
        fun bind(place: Place, listener: Listener, context: Context) = with(binding){
            val adapterPager = PlacePhotoAdapter()
            adapterPager.addImage(place.photo)
            image.adapter = adapterPager
            textAdress.text = place.adress
            textName.text = place.name
            textTime.text = place.time
            PlaceTags().createTag(place, context, layoutTags)
            carder.setOnClickListener{
                listener.onClick(place)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.place,parent,false)
        return  PlaceHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.bind(PlaceList[position], listener, context)
    }

    override fun getItemCount(): Int {
        return PlaceList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun createElement(partner: Place){
        PlaceList.add(partner)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun createAll(partnerList: MutableList<Place>){
        deleter()
        val partnerList2 = mutableListOf<Place>()
        partnerList.forEach {
            partnerList2.add(it)
        }
        PlaceList = partnerList2 as ArrayList<Place>
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleter(){
        PlaceList.removeAll(PlaceList.toSet())
    }

    interface Listener{
        fun onClick(partner: Place)
    }
}