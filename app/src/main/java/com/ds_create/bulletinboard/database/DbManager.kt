package com.ds_create.bulletinboard.database

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    val db = Firebase.database.getReference("main")

    fun publishAd() {
        db.setValue("Hi")
    }
}