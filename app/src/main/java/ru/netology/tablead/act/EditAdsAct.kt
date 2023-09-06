package ru.netology.tablead.act

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.OnCompleteListener
import ru.netology.tablead.MainActivity
import ru.netology.tablead.R
import ru.netology.tablead.adapters.ImageAdapter
import ru.netology.tablead.model.Ad
import ru.netology.tablead.model.DbManager
import ru.netology.tablead.databinding.ActivityEditAdsBinding
import ru.netology.tablead.dialogs.DialogSpinnerhelper
import ru.netology.tablead.fragment.FragmentCloseInterface
import ru.netology.tablead.fragment.ImageListFrag
import ru.netology.tablead.utils.CityHelper
import ru.netology.tablead.utils.ImageManager
import ru.netology.tablead.utils.ImagePicker
import java.io.ByteArrayOutputStream


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {

    private val dbManager = DbManager()
    var chooseImagwFrag: ImageListFrag? = null
    lateinit var imageAdapter: ImageAdapter
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerhelper()
    var editImagePosition = 0
    private var isEditState: Boolean = false
    private var ad: Ad? = null
    private var imageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        imageChangeCounter()
    }

    private fun checkEditState() {
        isEditState = isEditState()
        ad = null
        if (isEditState) {
            ad = (intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad)
            if (ad != null) fillViews(ad!!)
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(binding) {
        tvCountry.text = ad.country
        tvCity.text = ad.city
        edTelephone.setText(ad.telephone)
        edIndex.setText(ad.index)
        withSend.isChecked = ad.withSend
        tvCategory.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
        ImageManager.fillImageArray(ad,imageAdapter)
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
        checkEditState()

    }

    fun onClickSelectCountry(view: View) {

        //OnClicks
        val listCountry = CityHelper.getAllCoutries(this)
        dialog.showSpinnerDialog(this, listCountry, binding.tvCountry)
        if (binding.tvCity.text.toString() != getString(R.string.select_city)) {
            binding.tvCity.text = getString(R.string.select_city)
        }

    }

    fun onClickSelectCity(view: View) {
        val county = binding.tvCountry.text.toString()
        if (county != getString(R.string.select_country)) {
            val listCity = CityHelper.getAllCity(county, this)
            dialog.showSpinnerDialog(this, listCity, binding.tvCity)
        } else {
            Toast.makeText(this, getString(R.string.select_country), Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCategory(view: View) {
        val listCategoru = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCategoru, binding.tvCategory)
    }

    fun onClickPublish(view: View) {
        ad = fillAd()
        if (isEditState) {
            ad?.copy(key = ad?.key)?.let { dbManager.publishAd(it, onPublishFinish()) }
        } else {
           // dbManager.publishAd(adTemp, onPublishFinish())
            uploadImages()
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish() {
                finish()
            }

        }
    }

    private fun fillAd(): Ad {
        val ad: Ad
        binding.apply {
            ad = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                edIndex.text.toString(),
                edTelephone.text.toString(),
                withSend.isChecked,
                tvCategory.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                edEmail.text.toString(),
                "empty",
                "empty",
                "empty",
                dbManager.db.push().key,
                dbManager.auth.uid,
                System.currentTimeMillis().toString(),
                "0",
                false,
            )
        }
        return ad
    }

    fun onClickGetimages(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this, 3)
        } else {
            openChooseImageFragment(null)
            chooseImagwFrag?.updateAdapterForEdit(imageAdapter.mainArray)
        }
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        binding.btPublish.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImagwFrag = null
    }

    fun openChooseImageFragment(newList: ArrayList<Uri>?) {
        chooseImagwFrag = ImageListFrag(this)
        if (newList != null) chooseImagwFrag?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImagwFrag!!)
        fm.commit()
    }

    private fun nextImage(uri: String){
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAd(uri: String){
        when (imageIndex){
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
        }
    }

    // публикация объявления с ссылкой на главную картинку с помощью интерфейса  OnCompleteListener<Uri>
    @SuppressLint("SuspiciousIndentation")
    private fun uploadImages() {
        if (imageAdapter.mainArray.size == imageIndex) {
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
      val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
        uploadImage(byteArray){
          //  dbManager.publishAd(ad!!, onPublishFinish())
            nextImage(it.result.toString())
        }
    }

    // подготовка  картинки bitmap в byteArray
    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray()
    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        // ссылка куда хотим сохранить image
        val imStorageRef = dbManager.dbStorage.child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        //загрузка картинки
        val upTask = imStorageRef.putBytes(byteArray)
        //ссылка в хранилище через интерфейс OnCompleteListener
        upTask.continueWithTask{
            task -> imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun imageChangeCounter(){
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.vpImages.adapter?.itemCount}"
                binding.tvimageCounter.text = imageCounter

            }
        })
    }

}