package com.sensoguard.hunter.global

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.sensoguard.hunter.classes.ImageStorageManager
import java.io.File
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
        context.resources.getString(com.sensoguard.hunter.R.string.share_title)
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
        if (stream != null) {
            saved = finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream!!)
        }
    }
    return saved
}

/**
 * save video
 */
fun saveVideoInGallery(context: Context, videoUrl: String): Boolean {
    try {
        val request = DownloadManager.Request(Uri.parse(videoUrl))
        request.setTitle("download")
        request.setDescription("your file is downloading ...")
        request.allowScanningByMediaScanner()

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val videoFileName = "video_" + System.currentTimeMillis() + ".mp4"

        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            videoFileName
        )

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val result = manager!!.enqueue(request)
        Log.d("testSaveVideo", "result:" + result)
    } catch (ex: Exception) {
        ex.printStackTrace()
        return false
    }
    return true
}


/**
 * save video for share
 */
fun saveVideoInForShare(context: Context, videoUrl: String): Long {
    var result = -1L
    try {
        val request = DownloadManager.Request(Uri.parse(videoUrl))
        request.setTitle("download")
        request.setDescription("your file is downloading ...")
        request.allowScanningByMediaScanner()

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val videoFileName = "video_" + System.currentTimeMillis() + ".mp4"

        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            videoFileName
        )

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        if (manager != null) {
            result = manager.enqueue(request)
        }
        Log.d("testSaveVideo", "result:" + result)

    } catch (ex: Exception) {
        ex.printStackTrace()
        return -1L
    }
    return result
}

/**
 * open downloaded file
 */
fun openDownloadedAttachment(context: Context, downloadId: Long) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val query = DownloadManager.Query()
    query.setFilterById(downloadId)
    val cursor: Cursor = downloadManager.query(query)
    if (cursor.moveToFirst()) {
        val downloadStatus: Int =
            cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        val downloadLocalUri: String =
            cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
        val downloadMimeType: String =
            cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE))
        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL && downloadLocalUri != null) {
            openDownloadedAttachment(context, Uri.parse(downloadLocalUri), downloadMimeType)
        }
    }
    cursor.close()
}

/**
 * attach video file
 */
private fun openDownloadedAttachment(
    context: Context,
    attachmentUri: Uri,
    attachmentMimeType: String
) {
    var attachmentUri: Uri? = attachmentUri
    if (attachmentUri != null) {
        // Get Content Uri.
        if (ContentResolver.SCHEME_FILE == attachmentUri.scheme) {
            // FileUri - Convert it to contentUri.
            val file = File(attachmentUri.path)
            attachmentUri = //Uri.fromFile(file)
                FileProvider.getUriForFile(context, "${context.packageName}.contentprovider", file)
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "video/mp4g"
        intent.putExtra(Intent.EXTRA_STREAM, attachmentUri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(intent, "Share video"))
    }
}

/**
 * attach video file
 */
private fun openDownloadedAttachment1(
    context: Context,
    attachmentUri: Uri,
    attachmentMimeType: String
) {
    var attachmentUri: Uri? = attachmentUri
    if (attachmentUri != null) {
        // Get Content Uri.
        if (ContentResolver.SCHEME_FILE == attachmentUri.scheme) {
            // FileUri - Convert it to contentUri.
            val file = File(attachmentUri.path)
            attachmentUri = //Uri.fromFile(file)
                FileProvider.getUriForFile(context, "${context.packageName}.contentprovider", file)
        }
        val openAttachmentIntent = Intent(Intent.ACTION_VIEW)
        openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType)
        openAttachmentIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(openAttachmentIntent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}

class SaveImageInGalleryTask(val finalBitmap: Bitmap, val context: Context, val imageName: String) :
    AsyncTask<Void, Void, Boolean>() {


    override fun doInBackground(vararg params: Void?): Boolean? {
        return saveImageInGallery(finalBitmap, context, imageName)
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        if (result != null && result) {
            showToast(
                context,
                context.resources.getString(com.sensoguard.hunter.R.string.save_file_success)
            )
        } else {
            showToast(
                context,
                context.resources.getString(com.sensoguard.hunter.R.string.save_file_failed)
            )
        }
    }
}