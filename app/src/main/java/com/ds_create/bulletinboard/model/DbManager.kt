package com.ds_create.bulletinboard.model


import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {
    val db = Firebase.database.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishListener: FinishWorkListener) {
        auth.uid.let {
            db.child(ad.key ?: "empty")
                .child(auth.uid!!)
                .child(AD_NODE)
                .setValue(ad)
                .addOnCompleteListener {
                        finishListener.onFinish()
                }
        }

    }

    fun adViewed(ad: Ad) {
        var counter = ad.viewsCounter.toInt()
        counter++
        auth.uid.let {
            db.child(ad.key ?: "empty")
                .child(INFO_NODE)
                .setValue(InfoItem(counter.toString(), ad.emailCounter, ad.callsCounter))
        }
    }

    fun getMyAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }

    fun getAllAds(readDataCallback: ReadDataCallback?) {
        val query = db.orderByChild(auth.uid + "/ad/price")
        readDataFromDb(query, readDataCallback)
    }

    fun deleteAd(ad: Ad, listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) return
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            if (it.isSuccessful) listener.onFinish()
        }
    }

    private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {

                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) ad = it.child(AD_NODE).getValue(Ad::class.java)
                    }
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)

                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    ad?.let { adArray.add(it) }
                }
                readDataCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    interface ReadDataCallback {
        fun readData(list: ArrayList<Ad>)
    }

    interface FinishWorkListener {
        fun onFinish()
    }

    companion object {
        const val AD_NODE = "ad"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
    }
}