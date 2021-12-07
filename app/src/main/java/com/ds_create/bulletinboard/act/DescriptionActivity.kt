package com.ds_create.bulletinboard.act

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ds_create.bulletinboard.R
import com.ds_create.bulletinboard.adapters.ImageAdapter
import com.ds_create.bulletinboard.databinding.ActivityDescriptionBinding
import com.ds_create.bulletinboard.model.Ad
import com.ds_create.bulletinboard.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        adapter = ImageAdapter()
        binding.apply {
            viewPager.adapter = adapter
        }
        getIntentFromMainAct()
    }

    private fun getIntentFromMainAct() {
        val ad = intent.getSerializableExtra(AD) as Ad
        updateUI(ad)
    }

    private fun updateUI(ad: Ad) {
        fillImageArray(ad)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: Ad) = with(binding) {
        tvTitle.text = ad.title
        tvDescription.text = ad.description
        tvPrice.text = ad.price
        tvTel.text = ad.tel
        tvCountry.text = ad.country
        tvCity.text = ad.city
        tvIndex.text = ad.index
        tvWithSend.text = isWithSend(ad.withSend.toBoolean())
    }

    private fun isWithSend(withSend: Boolean): String {
        return if (withSend) getString(R.string.desc_act_with_send)
        else getString(R.string.desc_act_without_send_boolean)
    }

    private fun fillImageArray(ad: Ad) {
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3)
        CoroutineScope(Dispatchers.Main).launch {
            val bitmapList = ImageManager.getBitmapFromUris(listUris)
            adapter.update(bitmapList as ArrayList<Bitmap>)
        }
    }

    companion object {
        const val AD = "ad"
    }
}