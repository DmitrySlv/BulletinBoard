package com.ds_create.bulletinboard.act


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.ds_create.bulletinboard.MainActivity
import com.ds_create.bulletinboard.R
import com.ds_create.bulletinboard.adapters.ImageAdapter
import com.ds_create.bulletinboard.model.Ad
import com.ds_create.bulletinboard.model.DbManager
import com.ds_create.bulletinboard.databinding.ActivityEditAdsBinding
import com.ds_create.bulletinboard.dialogs.DialogSpinnerHelper
import com.ds_create.bulletinboard.fragments.FragmentCloseInterface
import com.ds_create.bulletinboard.fragments.ImageListFrag
import com.ds_create.bulletinboard.utils.CityHelper
import com.ds_create.bulletinboard.utils.ImagePicker

class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {

    var chooseImageFrag: ImageListFrag? = null
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var editImagePos = 0
    var launcherMultiSelectImage: ActivityResultLauncher<Intent>? = null
    var launcherSingleSelectImage: ActivityResultLauncher<Intent>? = null
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)
        setContentView(rootElement.root)
        init()
        checkEditState()
    }

    private fun checkEditState() {
        if (isEditState()) {
            isEditState = true
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            ad?.let {
                fillViews(it)
            }
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(rootElement) {
        tvCountry.text = ad.country
        tvCity.text = ad.city
        editTel.setText(ad.tel)
        edIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        rootElement.vpImages.adapter = imageAdapter
        launcherMultiSelectImage = ImagePicker.getLauncherForMultiSelectImages(this)
        launcherSingleSelectImage = ImagePicker.getLauncherForSingleImage(this)
    }

    //OnClicks
    fun onClickSelectCountry(view: View) {
        val listCountry = CityHelper.getAllCountries(this)
        dialog.showSpinnerDialog(this, listCountry, rootElement.tvCountry)
        if (rootElement.tvCity.text.toString() != getString(R.string.select_city)) {
            rootElement.tvCity.text = getString(R.string.select_city)
        }
    }

    fun onClickSelectCity(view: View) {
        val selectedCountry = rootElement.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.select_country)) {
            val listCity = CityHelper.getAllCities(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, rootElement.tvCity)
        } else {
            Toast.makeText(this, getString(R.string.no_country_selected), Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCat(view: View) {
        val listCity = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCity, rootElement.tvCat)
    }

    fun onClickGetImages(view: View) {

        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.launcher(this, launcherMultiSelectImage, 3)
        } else {
            openChooseImageFrag(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View) {
        val adTemp = fillAd()
        if (isEditState) {
            dbManager.publishAd(adTemp.copy(key = ad?.key), onPublishFinish())
        } else {
            dbManager.publishAd(adTemp, onPublishFinish())
        }
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object: DbManager.FinishWorkListener {
            override fun onFinish() {
                finish()
            }
        }
    }

    private fun fillAd(): Ad {
        val ad: Ad
        rootElement.apply {
            ad = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                editTel.text.toString(),
                edIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(), dbManager.db.push().key, "0", dbManager.auth.uid)
        }
        return ad
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        rootElement.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFrag = null
    }

    fun openChooseImageFrag(newList: ArrayList<String>?) {

        chooseImageFrag = ImageListFrag(this, newList)
        rootElement.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }
}