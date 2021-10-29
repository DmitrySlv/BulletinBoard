package com.ds_create.bulletinboard.database

import com.ds_create.bulletinboard.data.Ad
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    val db = Firebase.database.getReference("main")
    val auth = Firebase.auth

    fun publishAd(ad: Ad) {
        if (auth.uid != null)db.child(ad.key ?: "empty")
            .child(auth.uid!!)
            .child("ad")
                .setValue(ad)
    }
}