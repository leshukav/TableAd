package ru.netology.tablead.fragment

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.netology.tablead.R
import ru.netology.tablead.act.EditAdsAct
import ru.netology.tablead.databinding.ListImageFragBinding
import ru.netology.tablead.dialoghelper.ProgressDialog
import ru.netology.tablead.utils.ImageManager
import ru.netology.tablead.utils.ImagePicker
import ru.netology.tablead.utils.ImagePicker.MAX_IMAGE_COUNT
import ru.netology.tablead.utils.ItemTouchMoveCallBack

class ImageListFrag(val onFragClose: FragmentCloseInterface) :
    BaseAdsFragment(), AdapterCallBack {

    private val adapter = SelectImageAdapter(this)
    private lateinit var job: Job
    private var addImageItem: MenuItem? = null
    val dragCallback = ItemTouchMoveCallBack(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    lateinit var binding: ListImageFragBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ListImageFragBinding.inflate(layoutInflater)
        adView = binding.adView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolBar()
        with(binding) {
            touchHelper.attachToRecyclerView(rvImage)
            rvImage.layoutManager = LinearLayoutManager(activity)
            rvImage.adapter = adapter

        }
    }

    fun updateAdapterForEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true)
    }

    override fun onDetach() {
        super.onDetach()

   //     job?.cancel()
    }

    override fun onClose() {
        super.onClose()
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        onFragClose.onFragClose(adapter.mainArray)
    }

    private fun setUpToolBar() {
        binding.tb.inflateMenu(R.menu.menu_choose_image)
        val deleteItem = binding.tb.menu.findItem(R.id.delete)
        addImageItem = binding.tb.menu.findItem(R.id.ad_image)
        addImageItem?.isVisible = adapter.mainArray.size <= 2
        binding.tb.setNavigationOnClickListener {
            showInterAd()
        }

        deleteItem.setOnMenuItemClickListener {
            adapter.updateAdapter(ArrayList(), true)
            addImageItem?.isVisible = true
            true
        }

        addImageItem?.setOnMenuItemClickListener {
            val imageCount = MAX_IMAGE_COUNT - adapter.mainArray.size
            ImagePicker.addImages(activity as EditAdsAct,  imageCount)


            true
        }
    }

    fun updateAdapter(newList: ArrayList<Uri>, activity: Activity) {
        resizeSelectedImages(newList, false, activity )

    }

    fun setSingleImage(uri: Uri, pos: Int) {
        val pBar = binding.rvImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity)
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }

    }

    fun resizeSelectedImages(newList: ArrayList<Uri>, needClear: Boolean, activity: Activity) {
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createDialog(activity)
            val bitmapList = ImageManager.imageResize(newList, activity)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            addImageItem?.isVisible = adapter.mainArray.size <= 2
        }
    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }

}

interface AdapterCallBack {
    fun onItemDelete()
}

