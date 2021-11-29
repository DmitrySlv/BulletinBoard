package com.ds_create.bulletinboard.utils

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ds_create.bulletinboard.R
import com.ds_create.bulletinboard.act.EditAdsAct
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object ImagePicker {

    const val MAX_IMAGE_COUNT = 3
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_SINGLE_IMAGE = 998

   private fun getOptions(imageCounter : Int): Options {
        val options = Options().apply{
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun launcher(edAct: EditAdsAct, launcher: ActivityResultLauncher<Intent>?, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                 val fList = edAct.supportFragmentManager.fragments
                    fList.forEach{
                        if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
                    }
                }
                  //  PixEventCallback.Status.BACK_PRESSED -> // back pressed called
            }
        }
    }


    fun getLauncherForMultiSelectImages(edAct: EditAdsAct): ActivityResultLauncher<Intent> {
        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
//            if (result.resultCode == AppCompatActivity.RESULT_OK) {
//                if (result.data != null) {
//                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
//                    if (returnValues?.size!! > 1 && edAct.chooseImageFrag == null) {
//                        edAct.openChooseImageFrag(returnValues)
//                    } else if (edAct.chooseImageFrag != null) {
//                        edAct.chooseImageFrag?.updateAdapter(returnValues)
//                    } else if (returnValues.size == 1 && edAct.chooseImageFrag == null) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            edAct.rootElement.pBarLoad.visibility = View.VISIBLE
//                            val bitmapArray = ImageManager.imageResize(returnValues) as ArrayList<Bitmap>
//                            edAct.rootElement.pBarLoad.visibility = View.GONE
//                            edAct.imageAdapter.update(bitmapArray)
//                        }
//                    }
//                }
//            }
        }
    }

    fun getLauncherForSingleImage(edAct: EditAdsAct): ActivityResultLauncher<Intent> {
        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
//            if (result.resultCode == AppCompatActivity.RESULT_OK) {
//                if (result.data != null) {
//                    val uris = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
//                    edAct.chooseImageFrag?.setSingleImage(uris?.get(0)!!, edAct.editImagePos)
//                }
//            }
        }
    }
}