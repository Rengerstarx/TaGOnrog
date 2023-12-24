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
import com.example.taganroggo.databinding.PlaceBinding
import com.google.android.material.internal.ContextUtils.getActivity
import com.squareup.picasso.Picasso


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
            var allTags = 0
            layoutTags.removeAllViews()
            for (str in place.tags) {
                val cardView = CardView(context)
                val cardParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                cardParams.setMargins(5, 5, 5, 5)
                cardView.layoutParams = cardParams
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.mainBlue)) // Устанавливаем цвет фона
                cardView.radius = 40f // Устанавливаем радиус скругления углов

                val textView = TextView(context)
                val textParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textView.layoutParams = textParams
                textView.text = (" " + str + " ").toString()
                textView.textSize = 14f // Устанавливаем размер текста
                textView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Устанавливаем цвет текста
                textView.setPadding(10, 10, 10, 10) // Устанавливаем отступы
                cardView.addView(textView) // Добавляем TextView в CardView
                allTags++

                layoutTags.addView(cardView)
                if (allTags > 3) {
                    textView.text = "+${place.tags.size - 3}"
                    break
                }
            }
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
        println(partnerList2)
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