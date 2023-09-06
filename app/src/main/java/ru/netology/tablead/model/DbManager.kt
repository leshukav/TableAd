package ru.netology.tablead.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage


class DbManager() {

    companion object {
        const val AD = "ad"
        const val MAIN = "main"
        const val INFO = "info"
        const val FAVS = "favs"
        const val ADS_LIMIT = 2
        const val FILTER = "filter"
    }

    val db = Firebase.database.getReference(MAIN)
    val dbStorage = Firebase.storage.getReference(MAIN)
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishPublish: FinishWorkListener) {
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(auth.uid!!).child(AD)
            .setValue(ad)
            .addOnCompleteListener {
                val adFilter = AdFilter(ad.time, "${ad.category}_${ad.time}")
                db.child(ad.key ?: "empty")
                    .child(FILTER)
                    .setValue(adFilter).addOnCompleteListener {

                        if (it.isSuccessful) finishPublish.onFinish()
                    }
            }
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener) {
        if (ad.isFav) {
            removeFavs(ad, listener)
        } else {
            addToFavs(ad, listener)
        }
    }

    private fun addToFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let { key ->
            auth.uid?.let { uid ->
                db.child(key).child(FAVS).child(uid).setValue(uid).addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish()
                }
            }
        }
    }

    private fun removeFavs(ad: Ad, listener: FinishWorkListener) {
        ad.key?.let { key ->
            auth.uid?.let { uid ->
                db.child(key).child(FAVS).child(uid).removeValue().addOnCompleteListener {
                    if (it.isSuccessful) listener.onFinish()
                }
            }
        }
    }

    fun adViewed(ad: Ad) {
        var counter = ad.viewCounter.toInt()
        counter++
        if (auth.uid != null) db.child(ad.key ?: "empty")
            .child(INFO).setValue(InfoItem(counter.toString(), ad.emailsCounter, ad.callsCounter))
    }

    fun deleteAd(ad: Ad, listener: FinishWorkListener) {
        if (ad.key == null || ad.uid == null) return
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener {
            listener.onFinish()
        }
    }

    fun getMyAds(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallBack)
    }

    fun getMyFav(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild("/favs/${auth.uid}").equalTo(auth.uid)
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFirstPage(readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild("/filter/time").limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsNextPage(time: String, readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild("/filter/time").endBefore(time).limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFromCatFirstPage(cat: String, readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild( "/filter/catTime").startAt(cat).endAt(cat + "_\uf8ff")
            .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallBack)
    }

    fun getAllAdsFromCatNextPage(catTime: String, readDataCallBack: ReadDataCallBack?) {
        val query = db.orderByChild( "/filter/catTime").endBefore(catTime)
            .limitToLast(ADS_LIMIT)
        readDataFromDb(query, readDataCallBack)
    }

    private fun readDataFromDb(query: Query, readDataCallBack: ReadDataCallBack?) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for (item in snapshot.children) {
                    var ad: Ad? = null
                    item.children.forEach {
                        if (ad == null) ad = it.child(AD).getValue(Ad::class.java)
                    }
                    val infoItem = item.child(INFO).getValue(InfoItem::class.java)
                    val favCounter = item.child(FAVS).childrenCount
                    val isFav =
                        auth.uid?.let { item.child(FAVS).child(it).getValue(String::class.java) }
                    // val ad = item.children.iterator().next().child(AD).getValue(Ad::class.java)
                    if (ad != null) {
                        ad?.isFav = isFav != null
                        ad?.favCounter = favCounter.toString()
                        ad?.viewCounter = infoItem?.viewCounter ?: "0"
                        ad?.emailsCounter = infoItem?.emailsCounter ?: "0"
                        ad?.callsCounter = infoItem?.callsCounter ?: "0"
                        adArray.add(ad!!)
                    }

                }
                readDataCallBack?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    interface ReadDataCallBack {
        fun readData(list: ArrayList<Ad>)
    }

    interface FinishWorkListener {
        fun onFinish()
    }
}

