package ru.netology.tablead.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.tablead.model.Ad
import ru.netology.tablead.model.DbManager

class FireBaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    val liveAdsData = MutableLiveData<ArrayList<Ad>?>()

    fun loadAllAdsFirstPage() {
        dbManager.getAllAdsFirstPage(object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }

        })
    }

    fun loadAllAdsNextPage(time: String) {
        dbManager.getAllAdsNextPage(time, object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }

        })
    }

    fun loadAllAdsFromCat(cat: String) {
        dbManager.getAllAdsFromCatFirstPage(cat, object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCatNextPage(catTime: String) {
        dbManager.getAllAdsFromCatNextPage(catTime, object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }
        })
    }
    fun loadMyAds() {
        dbManager.getMyAds(object : DbManager.ReadDataCallBack {
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }

        })
    }

    fun loadMyFavs(){
        dbManager.getMyFav(object : DbManager.ReadDataCallBack{
            override fun readData(list: ArrayList<Ad>) {
                liveAdsData.value = list
            }

        })
    }

    fun onFavClick(ad: Ad, ){
        dbManager.onFavClick(ad, object : DbManager.FinishWorkListener{
            override fun onFinish() {
                val updateList = liveAdsData.value
                val position = updateList?.indexOf(ad)
                if (position != -1) {
                    position?.let {
                        val favCounter = if (ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                        updateList[position] = updateList[position].copy(favCounter = favCounter.toString(), isFav = !ad.isFav)
                    }
                }
                liveAdsData.postValue(updateList)
            }
        })
    }

    fun adViewed(ad: Ad){
        dbManager.adViewed(ad)
    }

    fun deleteItem(ad: Ad) {
        dbManager.deleteAd(ad, object : DbManager.FinishWorkListener {
            override fun onFinish() {
                val updateList = liveAdsData.value
                updateList?.remove(ad)
                liveAdsData.postValue(updateList)
            }
        })
    }
}