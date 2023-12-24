package com.example.taganroggo

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

class ParceUsers {

    fun parsUser(dataSnapshot: DataSnapshot): Users {
        var visit = mutableListOf<PlaceData>()
        println("------------------------------------------------------")
        println(dataSnapshot.child("Place").children.count())
        dataSnapshot.child("Place").children.forEach {
            println(dataSnapshot)
            visit.add(PlaceData(it.key.toString().toInt(), it.child("Count").value.toString().toInt(), it.child("Data").value.toString(), it.child("Coment").value.toString().toBoolean()))
        }
        val user = Users(
            mail = dataSnapshot.child("Mail").value.toString(),
            name = dataSnapshot.child("Name").value.toString(),
            surname = dataSnapshot.child("Surname").value.toString(),
            score = dataSnapshot.child("Score").value?.toString()?.toInt() ?: 0,
            ava = dataSnapshot.child("Photo").child("Avatarka").value.toString(),
            visits = visit,
            fon = dataSnapshot.child("Photo").child("Fon").value.toString()
        )
        return user
    }

}