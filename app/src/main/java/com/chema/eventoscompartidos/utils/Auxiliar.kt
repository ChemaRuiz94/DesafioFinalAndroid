package com.chema.eventoscompartidos.utils

import android.graphics.BitmapFactory

import android.graphics.Bitmap
import com.google.android.gms.common.util.Base64Utils.decode
import java.lang.Byte.decode
import java.security.spec.PSSParameterSpec.DEFAULT
import java.util.*
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.lang.Exception


object Auxiliar {


    fun getBytes(imageView: ImageView): ByteArray? {
        return try {
            val bitmap = (imageView.getDrawable() as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bytesData: ByteArray = stream.toByteArray()
            stream.close()
            bytesData
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return null
    }
}