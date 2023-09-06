package ru.netology.tablead.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.tablead.adapters.ImageAdapter
import ru.netology.tablead.model.Ad

object ImageManager {
    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGTH = 1

    fun getImageSize(uri: Uri, activity: Activity): List<Int> {
        val inStream = activity.contentResolver.openInputStream(uri)

            //сохранение файла из инпутСтрима по uri  ссылка content
     /*   val fTemp = File(activity.cacheDir, "temp.tmp")
        if (inStream != null) {
            fTemp.copyInStreamToFile(inStream)
        }
        */

        val option = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }



        BitmapFactory.decodeStream(inStream, null, option)
        return listOf(option.outWidth, option.outHeight)

    }

      // создание файла
  /*   private fun File.copyInStreamToFile(inStream: InputStream){
       this.outputStream().use {
           out -> inStream.copyTo(out)
       }
   }

   // определение на сколько повернуть изо
   */
  /*  private fun imageRotation(imageFile: File): Int {
        val rotation: Int
      //  val imageFile = File(uri)
        val exif = ExifInterface(imageFile.absolutePath)
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        rotation =
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                90
            } else 0
        return rotation
    }

   */

    fun chooseScaleType(im: ImageView, bitmap: Bitmap){
        if (bitmap.width > bitmap.height) {
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    suspend fun imageResize(uris: List<Uri>, activity: Activity): List<Bitmap> = withContext(Dispatchers.IO) {
        val tempList = ArrayList<List<Int>>()
        val bitmapList = ArrayList<Bitmap>()
        for (n in uris.indices) {
            val size = getImageSize(uris[n], activity)
            val imageRatio = size[WIDTH].toFloat() / size[HEIGTH].toFloat()

            if (imageRatio > 1) {
                if (size[WIDTH] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                } else {
                    tempList.add(listOf(size[WIDTH], size[HEIGTH]))
                }
            } else {
                if (size[HEIGTH] > MAX_IMAGE_SIZE) {
                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                } else {
                    tempList.add(listOf(size[WIDTH], size[HEIGTH]))
                }
            }
        }
        for (i in uris.indices) {
          kotlin.runCatching {
              bitmapList.add(
                  Picasso.get().load(uris[i]).resize(tempList[i][WIDTH], tempList[i][HEIGTH]).get()
              )
          }

        }

        return@withContext bitmapList
    }

    suspend fun getBitMapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO) {

        val bitmapList = ArrayList<Bitmap>()

        for (i in uris.indices) {
             kotlin.runCatching {
            bitmapList.add(Picasso.get().load(uris[i]).get())
              }

        }

        return@withContext bitmapList
    }

    fun fillImageArray(ad: Ad, adapter: ImageAdapter) {
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = ImageManager.getBitMapFromUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }

}