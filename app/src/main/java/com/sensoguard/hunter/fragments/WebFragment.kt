package com.sensoguard.hunter.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sensoguard.hunter.R
import com.sensoguard.hunter.global.LOGIN_COMPLETE_KEY
import com.sensoguard.hunter.global.USER_INFO_AMAZON_KEY
import com.sensoguard.hunter.global.getUserAmazonResultFromLocally


/**
 * A simple [Fragment] subclass.
 * Use the [WebFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WebFragment : Fragment() {

    private var webAlarms: WebView?=null
    private var ivBack:ImageView?=null
    private var pbLoadWeb:ProgressBar?=null

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
        pbLoadWeb= view.findViewById(R.id.pbLoadWeb)
        pbLoadWeb?.visibility=View.VISIBLE

        ivBack?.setOnClickListener {
            if (webAlarms?.canGoBack() == true) {
                webAlarms?.goBack()
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

        //prevent softkey
        webAlarms?.setFocusableInTouchMode(false)
        webAlarms?.isFocusable=false
        /////////////////

        //enable jQuery&Localhost
        webAlarms?.getSettings()?.domStorageEnabled=true
        webAlarms?.settings?.userAgentString = "First Webview"
        webAlarms?.settings?.loadWithOverviewMode = true
        webAlarms?.settings?.useWideViewPort = true

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
                    if (url == "https://outwatchpwa.sensoguard.com/"){
                        ivBack?.visibility=View.GONE
                    }else{
                        ivBack?.visibility=View.VISIBLE
                    }
                    Log.d("testUrl",url)
                }
                loadLocalHost()
            }
        }
        webAlarms?.loadUrl("https://outwatchpwa.sensoguard.com")
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

                val js=
                        "localStorage.setItem(\"loggedIn\", \"true\"); localStorage.setItem(\"token\", \"Bearer ${userInfo?.token}\");localStorage.setItem(\"imagesBaseUrl\", \"${userInfo?.imagesBaseUrl}\");"

                webAlarms?.evaluateJavascript(js, null)

        }
    }

    private fun setFilter() {
        val filter = IntentFilter(LOGIN_COMPLETE_KEY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.registerReceiver(receiver, filter, AppCompatActivity.RECEIVER_NOT_EXPORTED)
        } else {
            activity?.registerReceiver(receiver, filter)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, inn: Intent) {
            //accept currentAlarm
            if (inn.action == LOGIN_COMPLETE_KEY) {
                //Log.d("testAllAlarms", "accept alarm")
                loadWebAlarm()            }
        }
    }


}