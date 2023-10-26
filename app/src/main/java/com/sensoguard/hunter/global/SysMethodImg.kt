package com.sensoguard.hunter.global

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.AsyncTask
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.ImageStorageManager
import java.io.OutputStream


//convert bitmap to bitmap discriptor
fun convertBitmapToBitmapDiscriptor(context: Context, resId: Int): BitmapDescriptor? {
    val bitmap = context.let { getBitmapFromVectorDrawable(it, resId) }
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

//convert resId to bitmap
fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
    var drawable =  AppCompatResources.getDrawable(context, drawableId)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = (drawable?.let { DrawableCompat.wrap(it) })?.mutate()
    }

    val bitmap = drawable?.intrinsicWidth?.let {
        Bitmap.createBitmap(
            it,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
    } ?: return null

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}

//share image from selected alarm
fun shareImage(bitmap: Bitmap, activity: Activity?) {
    //val uri = activity?.let { getImageUriByBitmap(it,bitmap) }

    //convert bitmap to Uri:save the image in external and then you can share it
    val uri = activity?.let { ImageStorageManager.getImageUriByBitmap(bitmap, it) }



    if (uri != null) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        activity.startActivity(Intent.createChooser(intent, "Share Image"))
    }
}

/**
 * share link of video
 */
fun shareVideo(imgPath: String, context: Context) {

    val intentToEmailFile = Intent(Intent.ACTION_SEND)
    intentToEmailFile.type = "text/plain"
    //intentToEmailFile.setPackage("com.google.android.gm")
    intentToEmailFile.putExtra(
        Intent.EXTRA_TEXT, imgPath
    )
    intentToEmailFile.putExtra(
        Intent.EXTRA_SUBJECT,
        context.resources.getString(R.string.share_title)
    )
    val chooserIntent: Intent =
        Intent.createChooser(intentToEmailFile, "Send email")
    context.startActivity(chooserIntent)

}

//save the picture in locally gallery
fun saveImageInGallery(finalBitmap: Bitmap, context: Context, imageName: String): Boolean {
    var saved: Boolean = false
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/PerracoLabs")
        }
    }

    var stream: OutputStream? = null
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        stream = resolver.openOutputStream(uri)
        saved = finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    }
    return saved
}

class SaveImageInGalleryTask(val finalBitmap: Bitmap, val context: Context, val imageName: String) :
    AsyncTask<Void, Void, Boolean>() {


    override fun doInBackground(vararg params: Void?): Boolean? {
        return saveImageInGallery(finalBitmap, context, imageName)
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        if (result != null && result) {
            showToast(context, context.resources.getString(R.string.save_file_success))
        } else {
            showToast(context, context.resources.getString(R.string.save_file_failed))
        }
    }
}