package ru.netology.tablead.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ru.netology.tablead.MainActivity
import ru.netology.tablead.R
import ru.netology.tablead.act.DescriptionActivity
import ru.netology.tablead.act.EditAdsAct
import ru.netology.tablead.model.Ad
import ru.netology.tablead.databinding.AdListItemBinding

class AdsRcAdapter(val activity: MainActivity) : RecyclerView.Adapter<AdHolder>() {
    val adArray = ArrayList<Ad>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdHolder {
        val viewBinding =
            AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdHolder(viewBinding, activity)
    }

    override fun getItemCount(): Int {
        return adArray.size
    }

    override fun onBindViewHolder(holder: AdHolder, position: Int) {
        holder.bind(adArray[position])
    }

    fun updateAdapter(newList: List<Ad>) {
        val tempArray = ArrayList<Ad>()
        tempArray.addAll(adArray)
        tempArray.addAll(newList)

        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, tempArray))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(tempArray)
    }

    fun updateWithClear(newList: List<Ad>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilHelper(adArray, newList))
        diffResult.dispatchUpdatesTo(this)
        adArray.clear()
        adArray.addAll(newList)
    }
}

class AdHolder(private val viewBinding: AdListItemBinding, val activity: MainActivity) :
    RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(ad: Ad) = with(viewBinding) {

        tvDescription.text = ad.description
        tvPrice.text = ad.price
        tvTitle.text = ad.category
        tvViewCounter.text = ad.viewCounter
        tvFavCounter.text = ad.favCounter
        Picasso.get().load(ad.mainImage).into(mainImage)

        isFav(ad)

        mainOnClick(ad)

        showEditPanel(isOwner(ad))


    }


    private fun mainOnClick(ad: Ad) = with(viewBinding) {

        ibFav.setOnClickListener {
            if (activity.myAuth.currentUser?.isAnonymous == false) {
                activity.onFavClicked(ad)
                Log.d("MyTag", "${activity.myAuth.uid}")
            } else {
                Log.d("MyTag", "${activity.myAuth.uid}")
            }
        }

        itemsView.setOnClickListener {
            activity.onAddViewed(ad)
        }
        ibEditAd.setOnClickListener(onClickEdit(ad))
        ibDeleteAd.setOnClickListener {
            activity.onDeleteItem(ad)
        }
    }

    private fun isFav(ad: Ad) {
        if (ad.isFav) {
            viewBinding.ibFav.setImageResource(R.drawable.fav_pressed)
        } else {
            viewBinding.ibFav.setImageResource(R.drawable.fav_normal)
        }
    }

    private fun onClickEdit(ad: Ad): View.OnClickListener {
        return View.OnClickListener {
            val editIntent = Intent(activity, EditAdsAct::class.java).apply {
                putExtra(MainActivity.EDIT_STATE, true)
                putExtra(MainActivity.ADS_DATA, ad)
            }
            activity.startActivity(editIntent)
        }
    }

    private fun isOwner(ad: Ad): Boolean {
        return ad.uid == activity.myAuth.uid
    }

    private fun showEditPanel(isOwner: Boolean) {
        if (isOwner) {
            viewBinding.editPanel.visibility = View.VISIBLE
        } else {
            viewBinding.editPanel.visibility = View.GONE
        }
    }

    interface ItemListener {
        fun onDeleteItem(ad: Ad)

        fun onAddViewed(ad: Ad)

        fun onFavClicked(ad: Ad)
    }
}
