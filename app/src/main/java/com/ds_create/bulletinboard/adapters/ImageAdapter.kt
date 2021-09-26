package com.ds_create.bulletinboard.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.ds_create.bulletinboard.R

class ImageAdapter: RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imItem : ImageView

        fun setData(bitmap: Bitmap) {
            imItem = itemView.findViewById(R.id.imItem)
            imItem.setImageBitmap(bitmap)
        }
    }

    fun update(newList: ArrayList<Bitmap>) {
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }
}