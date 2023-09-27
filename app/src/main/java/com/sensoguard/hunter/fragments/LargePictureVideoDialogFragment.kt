package com.sensoguard.hunter.fragments

//haggay import com.halilibo.bvpkotlin.BetterVideoPlayer
//import kotlinx.android.synthetic.main.activity_my_screens.*
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.TouchImageView
import com.sensoguard.hunter.global.ACTION_PICTURE_KEY
import com.sensoguard.hunter.global.ACTION_TYPE_KEY
import com.sensoguard.hunter.global.ACTION_VIDEO_KEY
import com.sensoguard.hunter.global.IMAGE_PATH_KEY
import com.sensoguard.hunter.global.IMAGE_TIME_KEY
import com.sensoguard.hunter.global.SaveImageInGalleryTask
import com.sensoguard.hunter.global.shareImage
import com.sensoguard.hunter.global.showToast
import java.io.File


@UnstableApi
class LargePictureVideoDialogFragment : DialogFragment() {

    private var timeImage: String? = null
    private var imgPath: String? = null
    private var actionType: Int? = null
    private var ibClose: AppCompatImageButton? = null
    private var ivMyVideo: PlayerView? = null
    private var ivMyCaptureImage: TouchImageView? = null
    private var ibLargeImgShare: AppCompatImageButton? = null
    private var ibSaveLargeImgShare: AppCompatImageButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_large_picture_video,
            container,
            false
        )


        initViews(view)


        val bundle = arguments
        actionType = bundle?.getInt(ACTION_TYPE_KEY, -1)
        imgPath = bundle?.getString(IMAGE_PATH_KEY, null)
        timeImage = bundle?.getString(IMAGE_TIME_KEY, null)
        if (actionType == ACTION_PICTURE_KEY) {
            showPicture(imgPath)
        } else if (actionType == ACTION_VIDEO_KEY) {
            imgPath?.let { showVideo(it) }
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun showVideo(imgPath: String) {
        ivMyVideo?.visibility = View.VISIBLE
        ivMyCaptureImage?.visibility = View.GONE
        //val imgFile = File(context?.filesDir, imgPath)
        //ivMyVideo?.setSource(Uri.fromFile(imgFile))
        //val mediaItem = MediaItem.fromUri(Uri.fromFile(imgFile))
        //ivMyVideo.צק(mediaItem)
        //initializePlayer(imgFile)
        ivMyVideo?.player?.play()
    }

    ////////////// VIDEO configuration
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L
    private fun initializePlayer(imgFile: File) {
        val trackSelector = DefaultTrackSelector(requireActivity()).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(requireActivity())
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                ivMyVideo?.player = exoPlayer

                val mediaItem = MediaItem.Builder()
                    .setUri(Uri.fromFile(imgFile))
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentItem, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }

//    @SuppressLint("InlinedApi")
//    private fun hideSystemUi() {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        WindowInsetsControllerCompat(window, video_view).let { controller ->
//            controller.hide(WindowInsetsCompat.Type.systemBars())
//            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }
//    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            //Log.d(TAG, "changed state to $stateString")
        }

    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        //hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            val imgFile = imgPath?.let { File(context?.filesDir, it) }
            imgFile?.let { initializePlayer(it) }
        }
    }

    //////////////


    private fun showPicture(path: String?) {
        ivMyVideo?.visibility = View.GONE
        ivMyCaptureImage?.visibility = View.VISIBLE

        if (path == null)
            return

        showPicture(path, ivMyCaptureImage)


//        //from external storage
//        val imgFile = File(path)
//
//        //from internal storage
//        //val imgFile = File(context?.filesDir, path)
//
//        if (imgFile.exists()) {
//            //Picasso.get().load(File(imgFile.absolutePath)).into(ivMyCaptureImage)
//            showPicture(imgFile, ivMyCaptureImage)
//        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLayoutLandscape()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setLayoutPortrait()
        }
    }

    private fun setLayoutPortrait() {
    }

    private fun setLayoutLandscape() {
    }

    //show the picture by glide
    private fun showPicture(imgPath: String?, ivMyCaptureImage: AppCompatImageView?) {
        ivMyCaptureImage?.let {
            Glide.with(requireActivity()).load(imgPath).listener(object :
                RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Toast.makeText(context, "error loading image", Toast.LENGTH_LONG).show()
                    return false
                }

            }).into(it)
        }
    }


    private fun initViews(view: View?) {
        ibClose = view?.findViewById(R.id.ibClose)
        ibClose?.setOnClickListener {
            dismiss()
        }

        ivMyVideo = view?.findViewById(R.id.ivMyVideo)
        ivMyCaptureImage = view?.findViewById(R.id.ivMyCaptureImage)
        ibLargeImgShare = view?.findViewById(R.id.ibLargeImgShare)
        ibLargeImgShare?.setOnClickListener {
            val bitmap = (ivMyCaptureImage?.drawable as BitmapDrawable).bitmap
            try {
                bitmap?.let { shareImage(it, requireActivity()) }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
                showToast(activity, resources.getString(R.string.error))
            }
        }

        ibSaveLargeImgShare = view?.findViewById(R.id.ibSaveLargeImgShare)
        ibSaveLargeImgShare?.setOnClickListener {
            val bitmap = (ivMyCaptureImage?.drawable as BitmapDrawable).bitmap
            Thread {
                val res = timeImage?.let { it1 ->
                    //saveImageInGallery(bitmap,requireContext(), it1)
                    SaveImageInGalleryTask(bitmap, requireContext(), it1).execute()

                }
                Log.d("test_save", res.toString())
            }.start()

        }
    }

    private fun disableOrientation() {
        //haggay toolbar.visibility = View.VISIBLE
        //tab_layout.visibility=View.VISIBLE
        if (activity == null) {
            return
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setLayoutPortrait()
    }

    private fun enableOrientation() {
        //haggay toolbar.visibility = View.GONE
//        activity.findViewById<>(R.id.layout_table)
//        tab_layout.visibility=View.GONE
//        val layout =
//            view!!.findViewById(layout_table) as TableLayout // change id here
//
//        layout.visibility = View.GONE
        if (activity == null) {
            return
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onStop() {
        disableOrientation()
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onStart() {
        enableOrientation()

        //configuration the fragment dialog as full screen
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }

        super.onStart()
        if (Util.SDK_INT > 23) {
            val imgFile = imgPath?.let { File(context?.filesDir, it) }
            imgFile?.let { initializePlayer(it) }
        }
    }
}