package com.sensoguard.hunter.activities

import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.MonthDisplayHelper
import android.view.WindowManager
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.GeneralItemMenu
import com.sensoguard.hunter.classes.LanguageManager
import com.sensoguard.hunter.global.AMAZON
import com.sensoguard.hunter.global.AMAZON_PRECESS_WITH_USER_VALUE
import com.sensoguard.hunter.global.AZURE
import com.sensoguard.hunter.global.CURRENT_ITEM_TOP_MENU_KEY
import com.sensoguard.hunter.global.CURRENT_LANG_KEY_PREF
import com.sensoguard.hunter.global.LOGIN_TYPE_KEY
import com.sensoguard.hunter.global.ToastNotify
import com.sensoguard.hunter.global.USER_INFO_AMAZON_KEY
import com.sensoguard.hunter.global.USER_INFO_AZURE_KEY
import com.sensoguard.hunter.global.UserSession
import com.sensoguard.hunter.global.getAppLanguage
import com.sensoguard.hunter.global.getStringInPreference
import com.sensoguard.hunter.global.getTagsFromLocally
import com.sensoguard.hunter.global.getUserAmazonFromLocally
import com.sensoguard.hunter.global.getUserAzureFromLocally
import com.sensoguard.hunter.global.setAppLanguage

//import net.danlew.android.joda.JodaTimeAndroid


class MainActivity : LogInActivity() {

    private var dialog: AlertDialog? = null
    private val TAG = "MainActivity"
    private val CODE_REQUEST: Int = 1
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    //private var clickConsCamerasTable: ConstraintLayout? = null
    //private var clickConsMap: ConstraintLayout? = null
    private var clickConsConfiguration: ConstraintLayout? = null
    private var clickAlarmLog: ConstraintLayout? = null
    private var tvShowVer: TextView? = null

//    @Override
//    protected override fun attachBaseContext(newBase:Context) {
//        configurationLanguage()
//    }


    override fun onStart() {
        super.onStart()

        //setFilter()
        //show version name
        val verName = packageManager.getPackageInfo(packageName, 0).versionName
        val verTitle = "version:$verName"
        tvShowVer?.text = verTitle

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        //var year=Calendar.getInstance().get(Calendar.YEAR)
        //JodaTimeAndroid.init(this)

        var d: MonthDisplayHelper

        super.onCreate(savedInstanceState)

        configurationLanguage()



        setContentView(R.layout.activity_main)

        //clearAllNotifications()

        //hide unwanted badge of app icon
        hideBudgetNotification()

        //hide status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //check if the google play is installed in the device
        if (checkPlayServices()) {
            //check if the app is restricted and cannot accept notification in background
            checkBackgroundNotifRestrict()
        }


        //start repeated timeout to scan alarms from incoming emails
//        val isLoadApp = intent.getBooleanExtra(IS_LOAD_APP, false)
//        if (isLoadApp) {
//            //startServiceRepeat()
//            startJobServiceRepeat()
//        }
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
                initViews(false)
            }
            return false
        }
        return true
    }

    //check if the app has battery restriction of accepting notifications in background
    private fun checkBackgroundNotifRestrict() {

        //check if the system restrict accepting notifications in background
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val msg = activityManager.isBackgroundRestricted
            if (msg) {
                // "restricted"
                showBeforeDialog()
            } else {
                // "not restricted"
                initViews(true)
                //check if the device has google service
                if (checkPlayServices()) {
                    //do login AZURE or AMAZON
                    val loginType = getStringInPreference(this, LOGIN_TYPE_KEY, AZURE)
                    if (loginType.equals(AZURE)) {
                        isUserAzureForLoginExist()
                    } else if (loginType.equals(AMAZON)) {
                        isUserAmazonForLoginExist()
                    }
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
            openBatterySettings()
        }


        // Display a negative button on alert dialog
        builder.setNegativeButton(no) { dialog, which ->
            dialog.dismiss()
            initViews(true)
        }
        val alert = builder.create()
        alert.show()
    }

    //showing the settings of battery
    private fun openBatterySettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivityForResult(intent, CODE_REQUEST)
    }


    //after showing the settings of battery
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_REQUEST) {
            initViews(true)
            //do login AZURE or AMAZON
            val loginType = getStringInPreference(this, LOGIN_TYPE_KEY, AZURE)
            if (loginType.equals(AZURE)) {
                isUserAzureForLoginExist()
            } else if (loginType.equals(AMAZON)) {
                isUserAmazonForLoginExist()
            }
        }
    }

    //AZURE: open log in dialog if there is no tags or user&password
    private fun isUserAzureForLoginExist() {
        //check is has already tags
        val tags = getTagsFromLocally(this)
        val userInfo = getUserAzureFromLocally(this, USER_INFO_AZURE_KEY)
        if (tags == null || userInfo == null) {
            openLogInDialog()
            //sendPostHubs()
        } else {
            UserSession.instance.setTags(tags)
            UserSession.instance.setInstanceUserAzure(userInfo)
            registerTokenAndTagToHubs()
        }
    }

    //AMAZON : open log in dialog if there is no tags or user&password
    private fun isUserAmazonForLoginExist() {
        //check is has already tags
        val userInfo = getUserAmazonFromLocally(this, USER_INFO_AMAZON_KEY)
        if (userInfo == null) {
            openLogInDialog()
        } else {
            UserSession.instance.setInstanceUserAmazon(userInfo)
            loginAmazonFromDialog(AMAZON_PRECESS_WITH_USER_VALUE)
        }
    }


    override fun onResume() {
        super.onResume()
        clearAllNotifications()
    }

    //remove all notification of the app
    private fun clearAllNotifications() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }


    //hide unwanted badge of app icon
    private fun hideBudgetNotification() {
        val id = "my_channel_01"
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel =
                NotificationChannel(id, name, importance).apply {
                    description = descriptionText
                    setShowBadge(false)
                }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        } else {

        }

    }


    private fun setOnClickConfigTable() {
        clickConsConfiguration?.setOnClickListener {
            val inn = Intent(this, MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 0)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }

    private fun setOnClickAlarmLogTable() {
        clickAlarmLog?.setOnClickListener {
            val inn = Intent(this, MyScreensActivity::class.java)
            inn.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 1)
            inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(inn)
        }
    }

    private fun initViews(status: Boolean) {
        clickConsConfiguration = findViewById(R.id.clickConsConfiguration)
        clickConsConfiguration?.isEnabled = status
        clickAlarmLog = findViewById(R.id.clickAlarmLog)
        clickAlarmLog?.isEnabled = status
        tvShowVer = findViewById(R.id.tvShowVer)
        if (status) {
            setOnClickConfigTable()
            setOnClickAlarmLogTable()
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


}
