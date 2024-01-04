package com.example.taganroggo.Parsers

import com.example.taganroggo.Data.PlaceData
import com.example.taganroggo.Data.Users
import com.google.firebase.database.DataSnapshot

class ParceUsers {

    fun parsUser(dataSnapshot: DataSnapshot): Users {
        var visit = mutableListOf<PlaceData>()
        dataSnapshot.child("Place").children.forEach {
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