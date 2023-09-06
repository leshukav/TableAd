package ru.netology.tablead.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ru.netology.tablead.R

class ImageAdapter: RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_view_pager, parent,false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageAdapter.ImageHolder, position: Int) {
        holder.bind(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imItem: ImageView
        fun bind(bitmap: Bitmap){

           imItem = itemView.findViewById<ImageView>(R.id.imageView)
            imItem.setImageBitmap(bitmap)

        }
    }

    fun update(newList: ArrayList<Bitmap>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

}