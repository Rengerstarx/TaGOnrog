package com.example.taganroggo.Adapters.PlaceAdapter

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.taganroggo.Data.Place
import com.example.taganroggo.R

class PlaceTags {

    fun createTag(place: Place, context: Context, layoutTags: LinearLayout) {
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
    }
}