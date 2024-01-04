package com.example.taganroggo

import android.util.Log
import com.example.taganroggo.Parsers.ParceUsers
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*


/**
 * Класс для работы с Firebase Realtime Database
 * @author Rengerstar <vip.bekezin@mail.ru>
 * */

class FirebaseAPI {

    /**
     * Метод возвращающий список всех dataSnapshot переданного референса
     * @param referenceName - имя референса
     * @return listOfPartners - список всех dataSnapshot
     * */

    fun takeAll(referenceName: String, completion: (MutableList<DataSnapshot>) -> Unit) {
        val listOfPartners = mutableListOf<DataSnapshot>()
        FirebaseDatabase.getInstance().getReference(referenceName)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var count = 1
                    for (partnerSnapshot in dataSnapshot.children) {
                        listOfPartners.add(dataSnapshot.child(count.toString()))
                        count++
                    }
                    completion(listOfPartners)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //Обработка ошибки
                    println("Failed to read value: ${databaseError.toException()}")
                }
            })
    }

    /**
     * Метод возвращающий один конкретный dataSnapshot по переданному референсу и id
     * @param referenceName - имя референса
     * @param uid - id нужного элемента
     * @return dataSnapshot - нужный dataSnapshot
     * */
    fun takeOne(referenceName: String, uid: Int, completion: (DataSnapshot) -> Unit) {
        FirebaseDatabase.getInstance().getReference(referenceName).child(uid.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    completion(dataSnapshot)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Обработка ошибки
                    Log.e("FirebaseError", "Failed to read the value", databaseError.toException())
                }
            })
    }

    /**
     * Метод регистрирующий пользователя и возвращающий его айдишник
     * @param name - имя пользователя
     * @param mail - почта пользователя
     * @param surname - фамилия пользователя
     * @return uid - айди пользователя
     * */
    fun writeUserFB(name: String, mail: String, surname: String, completion: (Int) -> Unit) {
        FirebaseDatabase.getInstance().getReference("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val count = dataSnapshot.children.count() + 1
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString())
                        .child("Mail").setValue(mail)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString())
                        .child("Name").setValue(name)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString())
                        .child("Score").setValue(0)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString())
                        .child("Surname").setValue(surname)
                    val filenameA = "ava" + (1..5).random() + ".jpg"
                    val filenameF = "fon" + (1..5).random() + ".jpg"
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString())
                        .child("Photo").child("Avatarka").setValue(filenameA)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString())
                        .child("Photo").child("Fon").setValue(filenameF)
                    completion(count)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //Обработка ошибки
                    println("Failed to read value: ${databaseError.toException()}")
                }
            })
    }

    fun getPicLogo(url: String, completion: (String) -> Unit) {
        FirebaseStorage.getInstance().reference.child("$url").downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            completion(imageUrl)
        }
    }

    /**
     * Метод регистрирующий посещение ползователя
     * @param uid - айди пользователя
     * @param uidP - айди места
     * */
    fun addVisitByUid(uid: Int, uidP: Int) {
        takeOne("Users", uid) { it5 ->
            val user = ParceUsers().parsUser(it5)
            val time = Calendar.getInstance().time
            val currentTime = "${time.hours}:${time.minutes} ${time.date}.${time.month + 1}.${time.year - 100}"
            val scr = user.score + 5
            FirebaseDatabase.getInstance().getReference("Users").child(uid.toString())
                .child("Score").setValue(scr)
            FirebaseDatabase.getInstance().getReference("Users").child(uid.toString())
                .child("Place").child(uidP.toString()).child("Data")
                .setValue(currentTime)
            var marker = true
            for (vis in user.visits) {
                if (vis.placeID == uidP) {
                    val pos = vis.allPos!! + 1
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(uid.toString()).child("Place").child(uidP.toString())
                        .child("Count").setValue(pos)
                    marker = false
                }
            }
            if (marker) {
                FirebaseDatabase.getInstance().getReference("Users")
                    .child(uid.toString()).child("Place").child(uidP.toString())
                    .child("Count").setValue(1)
            }
        }
    }

    fun addComent(placeID: Int, uid: Int, coment: String, rait: Int) {
        FirebaseDatabase.getInstance().getReference("Places").child(placeID.toString()).child("Visitors").child(uid.toString()).child("Com").setValue(coment)
        FirebaseDatabase.getInstance().getReference("Places").child(placeID.toString()).child("Visitors").child(uid.toString()).child("Rait").setValue(rait)
        FirebaseDatabase.getInstance().getReference("Users").child(uid.toString()).child("Place").child(placeID.toString()).child("Coment").setValue(true)
    }
}