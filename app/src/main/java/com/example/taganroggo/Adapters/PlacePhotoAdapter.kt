package com.example.taganroggo.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.taganroggo.FirebaseAPI
import com.example.taganroggo.R
import com.squareup.picasso.Picasso

class PlacePhotoAdapter: RecyclerView.Adapter<PlacePhotoAdapter.ImageViewHolder>() {

    private var images: MutableList<String> = arrayListOf<String>() as MutableList<String>

    /**Добавляем список картинок*/
    fun addImage(imageResId: MutableList<String>) {
        images = imageResId
    }

    /**Создание холдера*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_smena, parent, false)
        return ImageViewHolder(view)
    }

    /**Подстановка новое картинки в случае конца таймера или ручной смены*/
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageRes = images[position]
        FirebaseAPI().getPicLogo(imageRes) {
            Picasso.get().load(it).into(holder.imageView)
        }
    }

    /**Возвращение рааззмера текущеего ViewPager2*/
    override fun getItemCount(): Int {
        return images.size
    }

    /**Находим необходимый нам ImageView*/
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView4)
    }

}