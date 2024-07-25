package com.sensoguard.hunter.activities

//import com.sensoguard.hunter.services.MediaService
import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.GeneralItemMenu
import com.sensoguard.hunter.classes.NonSwipeAbleViewPager
import com.sensoguard.hunter.fragments.AlarmLogFragment
import com.sensoguard.hunter.fragments.ConfigurationFragment
import com.sensoguard.hunter.fragments.WebFragment
import com.sensoguard.hunter.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.hunter.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.hunter.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.hunter.global.IS_MYSCREENACTIVITY_FOREGROUND
import com.sensoguard.hunter.global.MAIN_MENU_NUM_ITEM
import com.sensoguard.hunter.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.hunter.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.hunter.global.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.sensoguard.hunter.global.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
import com.sensoguard.hunter.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.hunter.global.USB_CONNECTION_FAILED
import com.sensoguard.hunter.global.getIntInPreference
import com.sensoguard.hunter.global.getLongInPreference
import com.sensoguard.hunter.global.getStringInPreference
import com.sensoguard.hunter.global.openDownloadedAttachment
import com.sensoguard.hunter.global.saveVideoInForShare
import com.sensoguard.hunter.global.setAppLanguage
import com.sensoguard.hunter.global.setBooleanInPreference
import com.sensoguard.hunter.global.setIntInPreference
import com.sensoguard.hunter.global.setLongInPreference
import com.sensoguard.hunter.global.setStringInPreference
import com.sensoguard.hunter.interfaces.OnFragmentListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference


class MyScreensActivity : LogInActivity(), OnFragmentListener {


    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var collectionPagerAdapter: CollectionPagerAdapter
    private lateinit var viewPager: ViewPager
    private var currentItemTopMenu = 0
    private var vPager: NonSwipeAbleViewPager?=null
    var videoFileId: Long?=null
    //private var togChangeStatus: ToggleButton? = null
    var pbLoadPhoto: ProgressBar?=null


    val TAG = "MyScreensActivity"


    // class to accept indication when saving video is completed
    class CheckDownloadComplete : BroadcastReceiver() {

        companion object Factory {

            var isComplete: MutableLiveData<Boolean> = MutableLiveData(false)
            fun create(): CheckDownloadComplete=CheckDownloadComplete()
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val action=intent.action
                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    isComplete.value=true
                }
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_my_screens)

        init()

        //store locally default values of configuration
        setConfigurationDefault()

        currentItemTopMenu = intent.getIntExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setLocationPermission()
        } else {
            init()
        }


    }


    /**
     * set observers
     */
    private fun setListener() {
        //set listener to download complete for sharing
        CheckDownloadComplete.isComplete.observe(this) {
            //Log.d("testDownload","accept complete")
            if (it) {
                pbLoadPhoto?.visibility=View.GONE
                if (videoFileId != null) {
                    openDownloadedAttachment(this, videoFileId!!)
                }
            }
        }
    }

    //store locally default values of configuration
    private fun setConfigurationDefault() {

        if (getLongInPreference(this, ALARM_FLICKERING_DURATION_KEY, -1L) == -1L) {
            //set the duration of flickering icon when accepted alarm
            setLongInPreference(this, ALARM_FLICKERING_DURATION_KEY, ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS)
        }

        if (getIntInPreference(this, MAP_SHOW_VIEW_TYPE_KEY, -1) == -1) {
            //set the type of ic_map_main
            setIntInPreference(this, MAP_SHOW_VIEW_TYPE_KEY, MAP_SHOW_SATELLITE_VALUE)
        }

        if (getStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, "-1").equals("-1")) {

            val uri=Uri.parse("android.resource://$packageName/raw/alarm_sound")

            setStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            when {
                arg1.action == USB_CONNECTION_FAILED -> {
                    editActionBar(false)
                }

            }
        }
    }

    private fun setFilter() {
        val filter = IntentFilter(USB_CONNECTION_FAILED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbReceiver, filter)
        }
    }


    private fun init() {

        vPager=findViewById(R.id.vPager)
        pbLoadPhoto=findViewById(R.id.pbLoadPhoto)

        configureActionBar()

        configTabs()

        setListener()

    }

    private fun editActionBar(state: Boolean) {
        //togChangeStatus?.isChecked = state
    }

    //TODO : the toggle will updated by the status changing
    private fun configureActionBar() {

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)//supportActionBar
        setSupportActionBar(toolbar)

    }

    override fun onPause() {
        super.onPause()
        setBooleanInPreference(this, IS_MYSCREENACTIVITY_FOREGROUND, false)
    }

    override fun onResume() {
        super.onResume()
        setBooleanInPreference(this, IS_MYSCREENACTIVITY_FOREGROUND, true)
    }

    private fun configTabs() {

        val tabs = findViewById<TabLayout>(R.id.tab_layout)

        viewPager = findViewById(R.id.vPager)
        collectionPagerAdapter = CollectionPagerAdapter(supportFragmentManager)
        viewPager.adapter = collectionPagerAdapter
        viewPager.setOnTouchListener(object : OnTouchListener {

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return true
            }
        })

        //relate the tab layout to viewpager because we need to add the icons
        tabs.setupWithViewPager(vPager)
        tabs.getTabAt(0)?.icon =
            ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_config_tab)
        tabs.getTabAt(1)?.icon =
            ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_alarm_log_tab)
        tabs.getTabAt(2)?.icon =
            ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_alarm_log_tab)
        viewPager.currentItem = currentItemTopMenu


    }


    override fun onStart() {
        super.onStart()
        setFilter()
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(usbReceiver)
        } catch (ex: Exception) {

        }
    }


    private fun setLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setExternalPermission()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                setExternalPermission()
            }
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init()
                }
            }
        }

    }

    private fun setExternalPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */

        val PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }

        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            init()
        } else {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS,
                PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )
        }
    }


//    private fun setExternalPermission() {
//        /*
//     * Request location permission, so that we can get the location of the
//     * device. The result of the permission request is handled by a callback,
//     * onRequestPermissionsResult.
//     */
//        if (ContextCompat.checkSelfPermission(
//                this.applicationContext,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            init()
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
//            )
//        }
//    }


    // Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
    inner class CollectionPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        override fun getCount(): Int = MAIN_MENU_NUM_ITEM

        override fun getItem(position: Int): Fragment {

            var fragment: Fragment? = null
            //set event of click ic_on top menu
            when (position) {
//                0 -> {
//                    fragment = SensorsFragment()
//                    fragment.arguments = Bundle().apply {
//                        // Our object is just an integer :-P
//                        putInt("ARG_OBJECT", position + 1)
//                    }
//                }
//                0 -> {
//                    fragment = MapSensorsFragment()
//                    fragment.arguments = Bundle().apply {
//                        // Our object is just an integer :-P
//                        putInt("ARG_OBJECT", position + 1)
//                    }
//                }
                0 -> {
                    fragment = ConfigurationFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
                1 -> {
                    fragment = AlarmLogFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }
                2 -> {
                    fragment = WebFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", position + 1)
                    }
                }


            }
            return fragment!!

        }

        override fun getPageTitle(position: Int): CharSequence {

            //set the title text of top menu
            return when (position) {
                //0 -> resources.getString(R.string.camera_title)
                //0 -> resources.getString(R.string.map_title)
                0 -> resources.getString(R.string.config_title)
                1 -> resources.getString(R.string.alarm_log_title)
                2 -> resources.getString(R.string.all_alarms)
                else -> "nothing"
            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        //start activity for loading new language if it has been changed
        startActivity(Intent(this,MainActivity::class.java))
    }

    //set the language of the app (calling  from activity)
    override fun updateLanguage() {
        setAppLanguage(this, GeneralItemMenu.selectedItem)
        this.finish()
        this.startActivity(intent)
    }


    override fun onSaveForShareVideo(alarm: Alarm) {
        pbLoadPhoto?.visibility=View.VISIBLE
        alarm.imgsPath?.let { it1 ->
            Thread {
                //save video file for sharing
                videoFileId=saveVideoInForShare(this, it1)
            }.start()
        }
    }

    override fun onSaveForShareVideo(imgPath: String) {
        pbLoadPhoto?.visibility=View.VISIBLE
        Thread {
            //save video file for sharing
            videoFileId=saveVideoInForShare(this, imgPath)
        }.start()

    }

    override fun onBack() {
        onBackPressed()
    }
}

//save the custom alarm file in alarms system
    class SaveCustomAlarmSoundAsync() : AsyncTask<Void, Void, Void>() {

        val LOG_TAG: String= "saveCustomAlarm"

        // Weak references will still allow the Activity to be garbage-collected
        private var weakActivity: WeakReference<Activity>?=null

        constructor(myActivity: Activity) : this() {
            weakActivity = WeakReference(myActivity)
        }


        override fun doInBackground(vararg params: Void?): Void? {
            saveCustomAlarmSound()
            return null
        }

        private fun saveCustomAlarmSound() {
            Environment.DIRECTORY_ALARMS

            // Get the directory for the app's private files directory.
            val dirTarget = File(
                weakActivity?.get()?.getExternalFilesDir(
                    Environment.DIRECTORY_ALARMS
                ), "alarm_sound.mp3"
            )

            if (!dirTarget.mkdirs()) {
                Log.e(LOG_TAG, "Directory not created")
            } else {

                val fileTarget = File(dirTarget.path + File.separator + "alarm_sound.mp3")
                val inputFileAlarm = weakActivity?.get()?.resources?.openRawResource(R.raw.alarm_sound)

                val byteArray = inputFileAlarm?.readBytes()


                try {
                    if(byteArray!=null) {
                        val fileOutput = FileOutputStream(fileTarget.absolutePath)
                        fileOutput.write(byteArray)
                        fileOutput.close()
                    }
                    //display file saved message
                    Log.e("Exception", "File saved successfully")

                } catch (e: IOException) {
                    Log.e("Exception", "File write failed: $e")
                }

            }

        }

    }



