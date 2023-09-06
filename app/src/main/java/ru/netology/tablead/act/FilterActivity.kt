package ru.netology.tablead.act

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import ru.netology.tablead.R
import ru.netology.tablead.databinding.ActivityFilterBinding
import ru.netology.tablead.dialogs.DialogSpinnerhelper
import ru.netology.tablead.utils.CityHelper

class FilterActivity : AppCompatActivity() {

    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerhelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBarSettings()
        onClickSelectCountry()
        onClickSelectCity()
        onClickDone()
    }

    fun onClickSelectCountry() = with(binding) {

        //OnClicks
        tvCountry.setOnClickListener {
            val listCountry = CityHelper.getAllCoutries(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, binding.tvCountry)
            if (binding.tvCity.text.toString() != getString(R.string.select_city)) {
                binding.tvCity.text = getString(R.string.select_city)
            }
        }
    }

    fun onClickSelectCity() = with(binding) {
        tvCity.setOnClickListener {
            val county = binding.tvCountry.text.toString()
            if (county != getString(R.string.select_country)) {
                val listCity = CityHelper.getAllCity(county, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCity, binding.tvCity)
            } else {
                Toast.makeText(
                    this@FilterActivity,
                    getString(R.string.select_country),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    fun onClickDone() = with(binding) {
        btDone.setOnClickListener {
          Log.d("My Log", "Filter: ${createFilter()}")
        }

    }

    private fun createFilter(): String = with(binding) {
        val sBuilder = java.lang.StringBuilder()
        val arrayTempFilter =
            listOf(
                tvCountry.text,
                tvCity.text,
                edIndex.text,
                withSend.isChecked.toString()
            )
        for ((i, s) in arrayTempFilter.withIndex() ) {
            if (s != getString(R.string.select_country) && s != getString(R.string.select_city) && s.isNotEmpty() ) {
                sBuilder.append(s)
               if (i != arrayTempFilter.size - 1) sBuilder.append("_")
            }
        }
        return sBuilder.toString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    fun actionBarSettings() {
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)

    }
}