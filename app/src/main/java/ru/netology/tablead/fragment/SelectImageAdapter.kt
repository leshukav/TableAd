package ru.netology.tablead.fragment

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netology.tablead.R
import ru.netology.tablead.act.EditAdsAct
import ru.netology.tablead.databinding.SelectImageItemBinding
import ru.netology.tablead.utils.ImageManager
import ru.netology.tablead.utils.ImagePicker
import ru.netology.tablead.utils.ItemTouchMoveCallBack

class SelectImageAdapter(val adapterCallBack: AdapterCallBack) :
    RecyclerView.Adapter<SelectImageAdapter.ImageHolder>(), ItemTouchMoveCallBack.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectImageAdapter.ImageHolder {
        val view =
            SelectImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(view, parent.context, this)
    }

    override fun onBindViewHolder(holder: SelectImageAdapter.ImageHolder, position: Int) {
        holder.bind(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    class ImageHolder(
        val binding: SelectImageItemBinding,
        val context: Context,
        val adapter: SelectImageAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bitmap: Bitmap) {

            binding.imEditImage.setOnClickListener {
                ImagePicker.getSinglImages(context as EditAdsAct)
                context.editImagePosition = adapterPosition

            }
            binding.imDelete.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallBack.onItemDelete()
            }
            binding.textContent.text =
                context.resources.getStringArray(R.array.title_array)[adapterPosition]
            ImageManager.chooseScaleType(binding.imageContent, bitmap)
            binding.imageContent.setImageBitmap(bitmap)
        }
    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {
        if (needClear == true) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }
}