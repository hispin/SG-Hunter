package com.sensoguard.hunter.activities

//import com.sensoguard.hunter.services.MediaService
import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import android.widget.ToggleButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.MutableLiveData
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.tabs.TabLayout
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.GeneralItemMenu
import com.sensoguard.hunter.classes.LanguageManager
import com.sensoguard.hunter.classes.NonSwipeAbleViewPager
import com.sensoguard.hunter.fragments.AlarmLogFragment
import com.sensoguard.hunter.fragments.ConfigurationFragment
import com.sensoguard.hunter.fragments.WebFragment
import com.sensoguard.hunter.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.hunter.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.hunter.global.AMAZON_PRECESS_WITH_USER_VALUE
import com.sensoguard.hunter.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.hunter.global.IS_MYSCREENACTIVITY_FOREGROUND
import com.sensoguard.hunter.global.IS_SETTINGS_NOTIFICATION_LAUNCHER
import com.sensoguard.hunter.global.MAIN_MENU_NUM_ITEM
import com.sensoguard.hunter.global.MAP_SHOW_SATELLITE_VALUE
import com.sensoguard.hunter.global.MAP_SHOW_VIEW_TYPE_KEY
import com.sensoguard.hunter.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.hunter.global.ToastNotify
import com.sensoguard.hunter.global.USB_CONNECTION_FAILED
import com.sensoguard.hunter.global.USER_INFO_AMAZON_KEY
import com.sensoguard.hunter.global.UserSession
import com.sensoguard.hunter.global.checkBackgroundNotifRestrict
import com.sensoguard.hunter.global.getAppLanguage
import com.sensoguard.hunter.global.getIntInPreference
import com.sensoguard.hunter.global.getLongInPreference
import com.sensoguard.hunter.global.getStringInPreference
import com.sensoguard.hunter.global.getUserAmazonResultFromLocally
import com.sensoguard.hunter.global.openDownloadedAttachment
import com.sensoguard.hunter.global.saveVideoInForShare
import com.sensoguard.hunter.global.setAppLanguage
import com.sensoguard.hunter.global.setBooleanInPreference
import com.sensoguard.hunter.global.setIntInPreference
import com.sensoguard.hunter.global.setLongInPreference
import com.sensoguard.hunter.global.setStringInPreference
import com.sensoguard.hunter.global.setupEdgeToEdge
import com.sensoguard.hunter.interfaces.OnFragmentListener


class MyScreensActivity : LogInActivity(), OnFragmentListener {


    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    private lateinit var collectionPagerAdapter: CollectionPagerAdapter
    private lateinit var viewPager: ViewPager
    private var currentItemTopMenu = 1
    private var vPager: NonSwipeAbleViewPager?=null
    var videoFileId: Long?=null
    //private var togChangeStatus: ToggleButton? = null
    var pbLoadPhoto: ProgressBar?=null
    private var consDisableNotification: ConstraintLayout?=null
    private var togChangeBackgroundRestrict: ToggleButton?=null

    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

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

    private lateinit var _openBatterySettings: ActivityResultLauncher<Intent>

    private val _requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            configureNotificationStatus()
        }

    private var isNotificationPermissionRequested = false

    private var isSettingsNotificationLauncher:Boolean?=null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setupEdgeToEdge() // edge-to-edge: draw behind system bars and pad the content (Play warning fix)

        configurationLanguage()

        setContentView(R.layout.activity_my_screens)

        /**
         * define listener for open battery settings
         */
        _openBatterySettings =
            registerForActivityResult(StartActivityForResult()) { _ -> // inspection fix: lambda instead of object literal
                        //check if the battery notification is enabled
                        configureNotificationStatus()
                    }

        //if the app is restart because change enabled notification from settings
        isSettingsNotificationLauncher = intent.getBooleanExtra(IS_SETTINGS_NOTIFICATION_LAUNCHER,false)
        if(isSettingsNotificationLauncher == true){
            openNotificationSettings()
        }


        initCheckingEnabledNotification()

            //create the back button
//        onBackPressedDispatcher.addCallback( this /* lifecycle owner */, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//
//            }
//        })

            //check if the google play is installed in the device
        if (checkPlayServices()) {
            //check if the app is restricted and cannot accept notification in background
            checkBackgroundNotifRestrict()
        }
    }

    private fun configurationLanguage() {
        LanguageManager.setLanguageList()
        val currentLanguage = getStringInPreference(this, CURRENT_LANG_KEY_PREF, "-1")
        if (currentLanguage != "-1") {
            GeneralItemMenu.selectedItem = currentLanguage
            setAppLanguage(this, GeneralItemMenu.selectedItem)
        } else {
            val deviceLang = getAppLanguage()
            if (LanguageManager.isExistLang(deviceLang)) {
                GeneralItemMenu.selectedItem = deviceLang
                setAppLanguage(this, GeneralItemMenu.selectedItem)
            }
        }
    }

    /**
     * init checking enabled notification
     */
    private fun initCheckingEnabledNotification() {
        consDisableNotification=findViewById(R.id.consDisableNotification)
        togChangeBackgroundRestrict=findViewById(R.id.togChangeBackgroundRestrict)
        togChangeBackgroundRestrict?.setOnCheckedChangeListener { buttonView, isChecked ->
            openNotificationSettings()
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
                pbLoadPhoto?.visibility=GONE // inspection fix: redundant qualifier removed
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

            val uri="android.resource://$packageName/raw/alarm_sound".toUri() // lint fix: KTX toUri

            setStringInPreference(this, SELECTED_NOTIFICATION_SOUND_KEY, uri.toString())
        }
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            when {
                arg1.action == USB_CONNECTION_FAILED -> {
                    // inspection fix: editActionBar() was empty and was removed
                }

            }
        }
    }

    private fun setFilter() {
        val filter = IntentFilter(USB_CONNECTION_FAILED)

        ContextCompat.registerReceiver(this, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }


    private fun init() {

        vPager=findViewById(R.id.vPager)
        pbLoadPhoto=findViewById(R.id.pbLoadPhoto)

        configureActionBar()

        configTabs()

        setListener()

    }

    // inspection fix: removed empty editActionBar()

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
        // lint fix: removed the touch listener - NonSwipeAbleViewPager already blocks swiping

        //relate the tab layout to viewpager because we need to add the icons
        tabs.setupWithViewPager(vPager)
        tabs.getTabAt(0)?.icon =
            ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_config_tab)
        tabs.getTabAt(1)?.icon =
            ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_verified_alarms_tab)
        tabs.getTabAt(2)?.icon =
            ContextCompat.getDrawable(this@MyScreensActivity, R.drawable.selected_all_alarms_tab)
        viewPager.currentItem = currentItemTopMenu


    }


    override fun onStart() {
        super.onStart()
        if(isSettingsNotificationLauncher==false) {
            configureNotificationStatus()
        }else{
            isSettingsNotificationLauncher=false
        }
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
            _requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val _requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            setExternalPermission()
        }

    private val _requestStoragePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[storagePermissions().first()] == true) {
                init()
            }
        }

    private fun storagePermissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    private fun setExternalPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */

        val permissions = storagePermissions()

        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                permissions.first()
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            init()
        } else {
            _requestStoragePermissions.launch(permissions)
        }
    }


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
                0 -> {
                    Log.d("testTabs", "ConfigurationFragment")
                    fragment = ConfigurationFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", 1) // inspection fix: position is always 0 in this branch
                    }
                }
                1 -> {
                    fragment = AlarmLogFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", 2) // inspection fix: constant for this tab
                    }
                }
                2 -> {
                    Log.d("testTabs", "WebFragment")
                    //make automatic login before open web all alarms
                    isUserAmazonForLoginExist()
                    fragment = WebFragment()
                    fragment.arguments = Bundle().apply {
                        // Our object is just an integer :-P
                        putInt("ARG_OBJECT", 3) // inspection fix: constant for this tab
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


    //set the language of the app (calling  from activity)
    override fun updateLanguage() {
        setAppLanguage(this, GeneralItemMenu.selectedItem)
        this.finish()
        // lint fix: restart with a new explicit intent instead of relaunching the incoming one
        this.startActivity(Intent(this, MyScreensActivity::class.java))
    }


    override fun onSaveForShareVideo(alarm: Alarm) {
        pbLoadPhoto?.visibility=VISIBLE // inspection fix: redundant qualifier removed
        alarm.imgsPath?.let { it1 ->
            Thread {
                //save video file for sharing
                videoFileId=saveVideoInForShare(this, it1)
            }.start()
        }
    }

    override fun onSaveForShareVideo(imgPath: String) {
        pbLoadPhoto?.visibility=VISIBLE // inspection fix: redundant qualifier removed
        Thread {
            //save video file for sharing
            videoFileId=saveVideoInForShare(this, imgPath)
        }.start()

    }

    override fun onBack() {
        refresh()
    }

    private fun refresh() {
        configureNotificationStatus()
    }

    /**
     * check is has already log in user
     */
    private fun isUserAmazonForLoginExist() {
        //check if tags already exist
        val userInfo = getUserAmazonResultFromLocally(this, USER_INFO_AMAZON_KEY)
        if (userInfo == null) {
            openLogInDialog(true)
        } else {
            UserSession.instance.setInstanceUserAmazonResult(userInfo)
            loginAmazon(AMAZON_PRECESS_WITH_USER_VALUE,true)
        }
    }


    /**
     * showing the settings of application
     */
    private fun openNotificationSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        _openBatterySettings.launch(intent)
    }

    /**
     * if notification is disable then show warning and disable activities
     */
    private fun configureNotificationStatus() {
        if(checkBackgroundNotifRestrict(this)){
            consDisableNotification?.visibility=GONE
            //store locally default values of configuration
            setConfigurationDefault()
            setLocationPermission()
        }else{
            consDisableNotification?.visibility=VISIBLE
            requestNotificationPermission()
        }
    }

    /**
     * from android 13 notifications are off until the user grants POST_NOTIFICATIONS,
     * so show the system dialog once before falling back to the settings banner
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && !isNotificationPermissionRequested
            && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            isNotificationPermissionRequested = true
            _requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog box that enables  users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)?.show()
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.")
                ToastNotify("This device is not supported by Google Play Services.", this)
                //initViews(false)
            }
            return false
        }
        return true
    }

    //check if the app has battery restriction of accepting notifications in background
    private fun checkBackgroundNotifRestrict() {

        //check if the system restrict accepting notifications in background
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager // inspection fix: redundant qualifier removed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val msg = activityManager.isBackgroundRestricted
            if (msg) {
                // "restricted"
                showBeforeDialog()
            } else {
                // "not restricted"
                //check if the device has Google Play services
                if (checkPlayServices()) {
                    //check if a logged-in user already exists
                    isUserAmazonForLoginExist()
                }
            }
        }
    }
    //show dialog to cancel the notifications in background battery restriction
    private fun showBeforeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.not_allow_background_activity))
        val yes = resources.getString(R.string.yes)
        val no = resources.getString(R.string.no)
        builder.setMessage(resources.getString(R.string.do_you_want_to_open_setting_battery))
            .setCancelable(false)
        builder.setPositiveButton(yes) { dialog, which ->
            dialog.dismiss()
            openNotificationSettings()
        }


        // Display a negative button on alert dialog
        builder.setNegativeButton(no) { dialog, which ->
            dialog.dismiss()
            //initViews(true)
        }
        val alert = builder.create()
        alert.show()
    }
}







