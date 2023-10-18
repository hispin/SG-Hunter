package com.sensoguard.hunter.activities

//import com.crashlytics.android.Crashlytics
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.CryptoHandler
import com.sensoguard.hunter.classes.MyExceptionHandler
import com.sensoguard.hunter.global.ACTIVATION_CODE_KEY
import com.sensoguard.hunter.global.IMEI_KEY
import com.sensoguard.hunter.global.IS_LOAD_APP
import com.sensoguard.hunter.global.NO_DATA
import com.sensoguard.hunter.global.PERMISSIONS_REQUEST_READ_PHONE_STATE
import com.sensoguard.hunter.global.getStringInPreference

//import io.fabric.sdk.android.Fabric

class InitAppActivity : AppCompatActivity() {

    private var myImei: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureGeneralCatch()
        //Fabric.with(this, Crashlytics())

       setContentView(R.layout.activity_init_app)
       //configureActivation()
       setReadPhoneStatePermission()
   }

    private fun configureGeneralCatch() {
        Thread.setDefaultUncaughtExceptionHandler(MyExceptionHandler(this))
    }


    //get the IMEI of the device and check it with the locally
    private fun configureActivation() {

        myImei = getDeviceIMEI()
        //tvImei?.text = myImei

        val localActivateCode = getStringInPreference(this, ACTIVATION_CODE_KEY, NO_DATA)
        if (!localActivateCode.equals(NO_DATA)) {
            val myActivateCode = CryptoHandler.getInstance().encrypt(myImei)
            if (localActivateCode != null && myActivateCode.startsWith(localActivateCode)) {
                val inn = Intent(this, MainActivity::class.java)
                inn.putExtra(IS_LOAD_APP, true)
                inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(inn)
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.wrong_activate_code),
                    Toast.LENGTH_SHORT
                ).show()
                openActivation()
            }
        } else {
            openActivation()
        }
    }

    //open activation screen
    private fun openActivation() {
        val inn = Intent(this, ActivationActivity::class.java)
        inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        inn.putExtra(IMEI_KEY,myImei)
        startActivity(Intent(inn))
    }


    //get the device IMEI
    private fun getDeviceIMEI(): String? {
        var deviceUniqueIdentifier: String? = null
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (null != tm) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                deviceUniqueIdentifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    tm.imei
                } else {
                    tm.deviceId
                }
            }
        }
        if (null == deviceUniqueIdentifier || deviceUniqueIdentifier.isEmpty()) {
            deviceUniqueIdentifier =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        }
        return deviceUniqueIdentifier
    }

    override fun onStart() {
        super.onStart()
        checkCrash()
    }

    //check if there was restart after crash
    private fun checkCrash() {
        fun sendEmail(msg: String) {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "message/rfc822"
            i.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf("hag.swead@gmail.com", "tomer@sensoguard.com")
            )
            i.putExtra(Intent.EXTRA_SUBJECT, "SensoGuard app has been crashed")
            i.putExtra(Intent.EXTRA_TEXT, msg)
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                ex.printStackTrace()
            }
        }
        //check if there was restart after crash
        val msgError = intent.getStringExtra("stacktrace")
        if (msgError != null) {
            sendEmail(msgError)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    configureActivation()
                }
            }
        }

    }

    //set permission of READ_PHONE_STATE
    private fun setReadPhoneStatePermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            configureActivation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSIONS_REQUEST_READ_PHONE_STATE
            )
        }
    }
}
