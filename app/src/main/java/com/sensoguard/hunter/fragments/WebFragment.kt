package com.sensoguard.hunter.fragments

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.sensoguard.hunter.R
import com.sensoguard.hunter.global.LOGIN_COMPLETE_KEY
import com.sensoguard.hunter.global.PWA_URL
import com.sensoguard.hunter.global.USER_INFO_AMAZON_KEY
import com.sensoguard.hunter.global.getUserAmazonResultFromLocally
import com.sensoguard.hunter.global.savePictureUrlInGallery
import com.sensoguard.hunter.global.showToast
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * A simple [Fragment] subclass.
 * Use the [WebFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WebFragment : Fragment() {

     var webAlarms: WebView?=null
    private var ivBack:ImageView?=null
    private var ivSaveImg:ImageView?=null
    private var pbLoadWeb:ProgressBar?=null
    var currentUrl:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_web, container, false)

        webAlarms = view.findViewById(R.id.webAlarms)
        ivBack = view.findViewById(R.id.ivBack)
        ivSaveImg = view.findViewById(R.id.ivSaveImg)
        pbLoadWeb= view.findViewById(R.id.pbLoadWeb)
        pbLoadWeb?.visibility=View.VISIBLE

        ivBack?.setOnClickListener {
            if (webAlarms?.canGoBack() == true) {
                webAlarms?.goBack()
            }
        }

        ivSaveImg?.setOnClickListener {
            //save image in gallery
            val executor: ExecutorService=Executors.newSingleThreadExecutor()
            val handler=Handler(Looper.getMainLooper())

            executor.execute {

                  if(currentUrl!=null) {
                      val result:Boolean?=savePictureUrlInGallery(requireActivity(), currentUrl!!)


                      handler.post {
                          if (result != null && result) {
                              context?.resources?.getString(com.sensoguard.hunter.R.string.save_file_success)
                                  ?.let { it1 ->
                                      showToast(
                                          context, it1
                                      )
                                  }
                          } else {
                              context?.resources?.getString(com.sensoguard.hunter.R.string.save_file_failed)
                                  ?.let { it1 ->
                                      showToast(
                                          context, it1
                                      )
                                  }
                          }

                      }
                  }


            }
        }

        return view
    }

    private fun disableOrientation() {
        if (activity == null) {
            return
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onStop() {
        disableOrientation()
        super.onStop()
    }

    private fun enableOrientation() {
        if (activity == null) {
            return
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }


    override fun onStart() {
        enableOrientation()
        super.onStart()
        setFilter()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(receiver)
    }


    private fun loadWebAlarm() {
        webAlarms?.webViewClient = WebViewClient()
        webAlarms?.setWebChromeClient(WebChromeClient())
        webAlarms?.isScrollbarFadingEnabled = true
        webAlarms?.isHorizontalScrollBarEnabled = false
        webAlarms?.settings?.javaScriptEnabled = true
        //webAlarms?.settings?.javaScriptCanOpenWindowsAutomatically=true

        //prevent softkey
        webAlarms?.setFocusableInTouchMode(true)
        webAlarms?.isFocusable=true
        /////////////////

        //enable jQuery&Localhost
        webAlarms?.getSettings()?.domStorageEnabled=true
        webAlarms?.settings?.userAgentString = "First Webview"
        webAlarms?.settings?.loadWithOverviewMode = true
        webAlarms?.settings?.useWideViewPort = true

        /////////
        webAlarms?.settings?.javaScriptCanOpenWindowsAutomatically = true  // Important for popups
//        webAlarms?.settings?.setSupportMultipleWindows(true)
        webAlarms?.settings?.setGeolocationEnabled(true)
        webAlarms?.settings?.allowFileAccess = true
        webAlarms?.clearCache(true)
        //enable debug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        if(activity!=null) {
            // Add the interface to WebView
            webAlarms?.addJavascriptInterface(
                WebAppInterface(requireActivity(), webAlarms!!),
                "AndroidInterface"
            )
        }
        webAlarms?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadLocalHost()
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                pbLoadWeb?.visibility=View.GONE
                super.onPageFinished(view, url)
                if (url != null) {
                    if (url == "$PWA_URL/"){//"https://outwatchpwa.sensoguard.com/"){
                        ivBack?.visibility=View.GONE
                        ivSaveImg?.visibility=View.GONE
                    }else{
                        ivBack?.visibility=View.VISIBLE
                        ivSaveImg?.visibility=View.VISIBLE
                        currentUrl=url
                    }
                    Log.d("testUrl",url)
                }
                loadLocalHost()
            }
        }
        webAlarms?.loadUrl(PWA_URL)
        //enable zoom on image
        webAlarms?.getSettings()?.setSupportZoom(true)
        webAlarms?.getSettings()?.builtInZoomControls=true
    }



    /**
     * load localhost
     */
    private fun loadLocalHost() {
        if(activity!=null) {
            // Inject values into localStorage
            val userInfo=
                getUserAmazonResultFromLocally(requireActivity(), USER_INFO_AMAZON_KEY)

            val js="localStorage.setItem(\"loggedIn\", \"true\"); localStorage.setItem(\"token\", \"Bearer ${userInfo?.token}\");localStorage.setItem(\"imagesBaseUrl\", \"${userInfo?.imagesBaseUrl}\");localStorage.setItem(\"customVisionOnly\", \"כש\");localStorage.setItem(\"role\", \"${userInfo?.role}\");"


//            val js=
//                        "localStorage.setItem(\"loggedIn\", \"true\"); localStorage.setItem(\"token\", \"Bearer ${userInfo?.token}\");localStorage.setItem(\"imagesBaseUrl\", \"${userInfo?.imagesBaseUrl}\");"

            webAlarms?.evaluateJavascript(js, null)

        }
    }

    private fun setFilter() {
        val filter=IntentFilter(LOGIN_COMPLETE_KEY)
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Log.d("testAllAlarms", "registerReceiver")
                    requireActivity().registerReceiver(
                        receiver, filter, AppCompatActivity.RECEIVER_EXPORTED)
            } else {
                activity?.registerReceiver(receiver, filter)
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            //accept currentAlarm
            if (inn.action == LOGIN_COMPLETE_KEY) {
                Log.d("testAllAlarms", "accept login complete")
                loadWebAlarm()
            }
        }
    }

    /**
     * class for using in javascript
     */
    class WebAppInterface(private val activity: Activity,private val webAlarms:WebView) {

        //this function is called by javascript
        @JavascriptInterface
        fun getLocation(callback: String) {
            // Get location using Android's LocationManager or FusedLocationProvider
            activity.runOnUiThread {
                var fusedLocationProviderClient=
                    LocationServices.getFusedLocationProviderClient(activity)
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@runOnUiThread
                }
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        // Call back to JavaScript with the location
                        val js = "$callback({coords: {latitude: ${location.latitude}, longitude: ${location.longitude}}})"
                        webAlarms.evaluateJavascript(js, null)
                    }
                }
            }
        }
    }

}