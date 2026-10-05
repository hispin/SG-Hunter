package com.sensoguard.hunter.activities

//import com.crashlytics.android.Crashlytics
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.CryptoHandler
import com.sensoguard.hunter.classes.MyExceptionHandler
import com.sensoguard.hunter.global.ACTIVATION_CODE_KEY
import com.sensoguard.hunter.global.IMEI_KEY
import com.sensoguard.hunter.global.IS_LOAD_APP
import com.sensoguard.hunter.global.NO_DATA
import com.sensoguard.hunter.global.getStringInPreference
import com.sensoguard.hunter.global.setupEdgeToEdge

//import io.fabric.sdk.android.Fabric

class InitAppActivity : AppCompatActivity() {

    private var myImei: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge() // edge-to-edge: draw behind system bars and pad the content (Play warning fix)

        configureGeneralCatch()
        //Fabric.with(this, Crashlytics())

       setContentView(R.layout.activity_init_app)
       //configureActivation()
       // lint fix: READ_PHONE_STATE is no longer needed (ANDROID_ID is used)
       configureActivation()
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


    //get the device identifier
    // lint fix: IMEI is not readable by apps since Android 10 (and the old check was inverted),
    // so use the app-scoped ANDROID_ID, which was already the fallback
    @SuppressLint("HardwareIds")
    private fun getDeviceIMEI(): String? {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
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

}
