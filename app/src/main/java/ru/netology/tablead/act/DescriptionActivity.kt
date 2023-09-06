package ru.netology.tablead.act

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.tablead.R
import ru.netology.tablead.adapters.ImageAdapter
import ru.netology.tablead.databinding.ActivityDescriptionBinding
import ru.netology.tablead.model.Ad
import ru.netology.tablead.utils.ImageManager

class DescriptionActivity : AppCompatActivity() {
    companion object {
        const val AD = "ad"
    }

    private var ad: Ad? = null

    private lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        binding.fbTel.setOnClickListener { call() }
        binding.fbEmail.setOnClickListener { sendEmail() }
    }

    private fun init() {
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMainActivity()
        imageChangeCounter()
    }

    private fun getIntentFromMainActivity() {
        ad = intent.getSerializableExtra(AD) as Ad
        if (ad != null) {
            ImageManager.fillImageArray(ad!!,adapter)
            fillTextViews(ad!!)
        }
    }



    private fun fillTextViews(ad: Ad) = with(binding) {
        tvTitle.text = ad.title
        tvDescription.text = ad.description
        tvPrice.text = ad.price
        tvTel.text = ad.telephone
        tvEmail.text = ad.email
        tvCountry.text = ad.country
        tvCity.text = ad.city
        tvIndex.text = ad.index
        tvWithSend.text = if (ad.withSend) "Yes" else "No"

    }

    // формирование интента для запуска приложения звонка
    private fun call() {
        val callUri = "tel:${ad?.telephone}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data = callUri.toUri()
        startActivity(iCall)
    }

    private fun sendEmail() {
        val iSendEmail = Intent(Intent.ACTION_SEND)
        iSendEmail.type = "message/rfc822"
        iSendEmail.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.ad_my_ads))
            putExtra(Intent.EXTRA_TEXT, "Меня интересует ваше объявление")
        }
        startActivity(Intent.createChooser(iSendEmail, "Open with"))
        try {
        //    startActivity(Intent.createChooser(iSendEmail, "Open with"))
        } catch (e: ActivityNotFoundException) {
        }
    }

    private fun imageChangeCounter(){
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
                binding.tvImageCounter.text = imageCounter

            }
        })
    }
}