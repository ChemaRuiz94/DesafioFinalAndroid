package com.chema.eventoscompartidos.utils

import android.graphics.BitmapFactory

import android.graphics.Bitmap
import com.google.android.gms.common.util.Base64Utils.decode
import java.lang.Byte.decode
import java.security.spec.PSSParameterSpec.DEFAULT
import java.util.*
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
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


    fun ImageToString(bitmap: Bitmap):String?{
        val baos = ByteArrayOutputStream()
        //val bitmap : Bitmap = imgUsuarioPerfil.drawToBitmap()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        var imageString : String? = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return imageString
    }

    fun StringToBitMap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }
    }

    fun getBitmap(image: ByteArray):Bitmap?{
        return BitmapFactory.decodeByteArray(image,0,image.size)
    }

}