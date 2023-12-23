package com.example.taganroggo

import com.google.firebase.database.DataSnapshot

class ParceUsers {

    fun parsUser(dataSnapshot: DataSnapshot): User {
        var visit = mutableMapOf<String, Boolean>()
        for (partnerSnapshot in dataSnapshot.child("Places").children) {
            visit[dataSnapshot.key.toString()] = partnerSnapshot.value.toString().toBoolean()
        }
        val user = User(
            mail = dataSnapshot.child("Mail").value.toString(),
            name = dataSnapshot.child("Name").value.toString(),
            surname = dataSnapshot.child("Surname").value.toString(),
            score = dataSnapshot.child("Score").value.toString().toInt(),
            ava = dataSnapshot.child("Photo").child("Avatarka").value.toString(),
            visits = visit,
            fon = dataSnapshot.child("Photo").child("Fon").value.toString()
        )
        return user
    }

}