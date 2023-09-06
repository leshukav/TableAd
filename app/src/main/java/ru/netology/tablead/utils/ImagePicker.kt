package ru.netology.tablead.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.tablead.R
import ru.netology.tablead.act.EditAdsAct


object ImagePicker {
    const val MAX_IMAGE_COUNT = 3

    private fun getOptions(imageCount: Int): Options {

        val options = Options().apply {
            count = imageCount
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun getMultiImages(
        edActivity: EditAdsAct,
        imageCounter: Int
    ) {
        edActivity.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImages(edActivity, result.data)
                }
                PixEventCallback.Status.BACK_PRESSED -> {}
                else -> {}
            }
        }
    }

    fun addImages(
        edActivity: EditAdsAct,
        imageCounter: Int
    ) {
      //  val frag = edActivity.chooseImagwFrag
        edActivity.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
               //     edActivity.chooseImagwFrag = frag
                    openChooseImageFragment(edActivity)
                    edActivity.chooseImagwFrag?.updateAdapter(result.data as ArrayList<Uri>, edActivity)
                }
                PixEventCallback.Status.BACK_PRESSED -> {}
                else -> {}
            }
        }
    }

    fun getSinglImages(
        edActivity: EditAdsAct
    ) {
   //     val frag = edActivity.chooseImagwFrag
        edActivity.addPixToActivity(R.id.place_holder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
               //     edActivity.chooseImagwFrag = frag
                    openChooseImageFragment(edActivity)
                    singleImages(edActivity, result.data[0])
                }
                PixEventCallback.Status.BACK_PRESSED -> {}
                else -> {}
            }
        }
    }

    //открытие фрагмента ранее сохраненного
    private fun openChooseImageFragment(edActivity: EditAdsAct) {
        edActivity.supportFragmentManager.beginTransaction().replace(R.id.place_holder, edActivity.chooseImagwFrag!!)
            .commit()
    }

    private fun closePixFragment(edActivity: EditAdsAct) {
        val flist = edActivity.supportFragmentManager.fragments
        flist.forEach {
            if (it.isVisible) edActivity.supportFragmentManager.beginTransaction().remove(it)
                .commit()
        }
    }

    fun getMultiSelectImages(edActivity: EditAdsAct, uris: List<Uri>) {
        if (uris.size > 1 && edActivity.chooseImagwFrag == null) {

            edActivity.openChooseImageFragment(uris as ArrayList<Uri>)

        } else if (uris.size == 1) {
            CoroutineScope(Dispatchers.Main).launch {
                edActivity.binding.progressLoad.visibility = View.VISIBLE
                val bitmapArray =
                    ImageManager.imageResize(uris, edActivity) as ArrayList<Bitmap>
                edActivity.binding.progressLoad.visibility = View.GONE
                edActivity.imageAdapter.update(bitmapArray)
                closePixFragment(edActivity)
            }
        }
    }

    private fun singleImages(edActivity: EditAdsAct, uri: Uri) {
        edActivity.chooseImagwFrag?.setSingleImage(
            uri,
            edActivity.editImagePosition
        )
    }

    //   launcher callBack для одного фото
    /*  fun getLauncherForSingleImages(edActivity: EditAdsAct): ActivityResultLauncher<Intent> {
          return edActivity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
              if (result.resultCode == Activity.RESULT_OK) {
                  /*    if (result.data != null) {
                      val uris = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                      edActivity.chooseImagwFrag?.setSingleImage(
                          uris?.get(0)!!,
                          edActivity.editImagePosition
                      )
                  }
              }

               */
              }
          }
      }

     */

}
