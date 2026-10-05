package com.sensoguard.hunter.global

import android.app.Activity
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.sensoguard.hunter.classes.ImageStorageManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


//convert bitmap to bitmap descriptor (inspection fix: spelling)
//fun convertBitmapToBitmapDiscriptor(context: Context, resId: Int): BitmapDescriptor? {
//    val bitmap = context.let { getBitmapFromVectorDrawable(it, resId) }
//    return BitmapDescriptorFactory.fromBitmap(bitmap)
//}

// inspection fix: removed unused getBitmapFromVectorDrawable()

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

// inspection fix: removed unused shareVideo()

/////////////////////

/**
 * convert bitmap to file
 */
fun saveImageInGallery(bitmap: Bitmap, fileNameToSave: String): Boolean? { // File name like "image.png"
    //create a file to write bitmap data
    var file: File? // inspection fix: redundant initializer removed
    return try {
        file = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/" + Environment.DIRECTORY_DOWNLOADS + File.separator + fileNameToSave
        )
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
        val bitmapdata = bos.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false // it will return null
    }
}


/////////////////////


//save the picture in locally gallery
//fun saveImageInGallery(finalBitmap: Bitmap, context: Context, imageName: String): Boolean {
//    var tempDir=Environment.DIRECTORY_DOWNLOADS
//    tempDir=File(tempDir + "/.temp/")
//    tempDir.mkdir()
//    val tempFile=File.createTempFile(imageName, ".jpg", tempDir)
//    val bytes=ByteArrayOutputStream()
//    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//    val bitmapData=bytes.toByteArray()
//
//
//    //write the bytes in file
//    val fos=FileOutputStream(tempFile)
//    fos.write(bitmapData)
//    fos.flush()
//    fos.close()
//    return Uri.fromFile(tempFile)
//
////    var saved: Boolean = false
////    val resolver = context.contentResolver
////    val contentValues = ContentValues().apply {
////        put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
////        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
////            put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/PerracoLabs")
////        }
////    }
////
////    var stream: OutputStream? = null
////    val uri = resolver.insert(Environment.DIRECTORY_DOWNLOADS, contentValues)
////
////    uri?.let {
////        stream = resolver.openOutputStream(uri)
////        if (stream != null) {
////            saved = finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream!!)
////        }
////    }
////    return saved
//}

/**
 * save url of picture
 */
fun savePictureUrlInGallery(context: Context, url: String): Boolean {
    try {
        val request = DownloadManager.Request(url.toUri()) // lint fix: KTX toUri
        request.setTitle("download")
        request.setDescription("your file is downloading ...")
        request.allowScanningByMediaScanner()

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val videoFileName = "photo_" + System.currentTimeMillis() + ".png"

        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            videoFileName
        )

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val result = manager!!.enqueue(request)
        Log.d("testSavePic", "result:$result") // inspection fix: string template
    } catch (ex: Exception) {
        ex.printStackTrace()
        return false
    }
    return true
}
/**
 * save video
 */
fun saveVideoInGallery(context: Context, videoUrl: String): Boolean {
    try {
        val request = DownloadManager.Request(videoUrl.toUri()) // lint fix: KTX toUri
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
        Log.d("testSaveVideo", "result:$result") // inspection fix: string template
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
        val request = DownloadManager.Request(videoUrl.toUri()) // lint fix: KTX toUri
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
        Log.d("testSaveVideo", "result:$result") // inspection fix: string template

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
            openDownloadedAttachment(context, downloadLocalUri.toUri(), downloadMimeType) // lint fix: KTX toUri
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK // inspection fix: property access
        context.startActivity(Intent.createChooser(intent, "Share video"))
    }
}

// inspection fix: removed unused openDownloadedAttachment1()

