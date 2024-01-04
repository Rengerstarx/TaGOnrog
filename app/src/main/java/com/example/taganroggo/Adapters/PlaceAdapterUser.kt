package com.example.taganroggo.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taganroggo.*
import com.example.taganroggo.Data.PlaceData
import com.example.taganroggo.Parsers.ParserPLace
import com.example.taganroggo.databinding.Place2Binding
import com.google.firebase.database.DataSnapshot


class PlaceAdapterUser(val context: Context, val listener: Listener): RecyclerView.Adapter<PlaceAdapterUser.PlaceUserHolder>() {
    private var PlaceList=ArrayList<PlaceData>()
    private var PlaceData = ArrayList<DataSnapshot>()

    class PlaceUserHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = Place2Binding.bind(item)
        fun bind(place: PlaceData, data: DataSnapshot, listener: Listener) = with(binding) {
            val placeOne = ParserPLace().parsPalce(data)
            val adapterPager = PlacePhotoAdapter()
            adapterPager.addImage(placeOne.photo)
            image.adapter = adapterPager
            textView6.text = "Количество посещений: ${place.allPos}"
            textName.text = placeOne.name
            textView7.text = "Дата последнего посещения: ${place.lastPos}"
            liner.setOnClickListener {
                listener.onClick(place, data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceUserHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.place2,parent,false)
        return  PlaceUserHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceUserHolder, position: Int) {
        holder.bind(PlaceList[position], PlaceData[position], listener)
    }

    override fun getItemCount(): Int {
        return PlaceList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun createElement(partner: PlaceData, data: DataSnapshot){
        PlaceList.add(partner)
        PlaceData.add(data)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun createAll(partnerList: MutableList<PlaceData>, data: ArrayList<DataSnapshot>){
        deleter()
        val partnerList2 = mutableListOf<PlaceData>()
        val data2 = mutableListOf<DataSnapshot>()
        partnerList.forEach {
            partnerList2.add(it)
        }
        data.forEach {
            data2.add(it)
        }
        PlaceList = partnerList2 as ArrayList<PlaceData>
        PlaceData = data2 as ArrayList<DataSnapshot>
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleter(){
        PlaceList.removeAll(PlaceList.toSet())
        PlaceData.removeAll(PlaceData.toSet())
    }

    interface Listener{
        fun onClick(placeData: PlaceData, data: DataSnapshot)
    }
}