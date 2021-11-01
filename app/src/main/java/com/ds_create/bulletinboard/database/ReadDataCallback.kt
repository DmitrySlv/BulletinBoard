package com.ds_create.bulletinboard.database

import com.ds_create.bulletinboard.data.Ad

interface ReadDataCallback {
    fun readData(list: List<Ad>)
}