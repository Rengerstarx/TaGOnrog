package com.example.taganroggo

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


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
            .addValueEventListener(object :
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
    fun writeUserFB(name: String, mail: String, surname: String, completion: (Int) -> Unit) {
        FirebaseDatabase.getInstance().getReference("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val count = dataSnapshot.children.count() + 1
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Mail").setValue(mail)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Name").setValue(name)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Score").setValue(0)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Surname").setValue(surname)
                    val filenameA = "ava" + (1..5).random() + ".jpg"
                    val filenameF = "fon" + (1..5).random() + ".jpg"
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Photo").child("Avatarka").setValue(filenameA)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Photo").child("Fon").setValue(filenameF)
                    completion(count)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    //Обработка ошибки
                    println("Failed to read value: ${databaseError.toException()}")
                }
            })
    }
    fun getPicLogo(url: String, completion: (String) -> Unit){
        FirebaseStorage.getInstance().reference.child("$url").downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            completion(imageUrl)
        }
    }

    fun writeUser(name: String, mail: String, surname: String, completion: (Int) -> Unit) {
        val listOfPartners = mutableListOf<DataSnapshot>()
        FirebaseDatabase.getInstance().getReference("Users")
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val count = dataSnapshot.children.count()
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Mail").setValue(mail)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Name").setValue(name)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Score").setValue(0)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Surname").setValue(surname)
                    val filenameA = "ava" + (1..5).random() + ".jpg"
                    val filenameF = "fon" + (1..5).random() + ".jpg"
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Photo").child("Avatarka").setValue(filenameA)
                    FirebaseDatabase.getInstance().getReference("Users").child(count.toString()).child("Mail").child("Fon").setValue(filenameF)
                    completion(count)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    //Обработка ошибки
                    println("Failed to read value: ${databaseError.toException()}")
                }
            })
    }

}