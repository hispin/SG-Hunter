package com.sensoguard.hunter.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.sensoguard.hunter.R
import com.sensoguard.hunter.global.USER_INFO_AMAZON_KEY
import com.sensoguard.hunter.global.getUserAmazonResultFromLocally


/**
 * A simple [Fragment] subclass.
 * Use the [WebFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WebFragment : Fragment() {

    private var webAlarms: WebView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_web, container, false)

        webAlarms = view.findViewById(R.id.webAlarms)

        loadWebAlarm()

        return view
    }

    private fun loadWebAlarm() {
        // Configure WebView


        webAlarms?.settings?.javaScriptEnabled = true


        webAlarms?.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {

                super.onPageStarted(view, url, favicon)

                if(activity!=null) {
                    // Inject values into localStorage
                    val userInfo=
                        getUserAmazonResultFromLocally(requireActivity(), USER_INFO_AMAZON_KEY)

//                    val js=
//                        "localStorage.setItem(loggedIn, true); localStorage.setItem(\"token\", `Bearer ${userInfo?.token}`);localStorage.setItem(\"imagesBaseUrl\", ${userInfo?.imagesBaseUrl});"

//                    val js=
//                        "localStorage.setItem(\"loggedIn\", \"true\"); localStorage.setItem(\"token\", `Bearer ${userInfo?.token}`);localStorage.setItem(\"imagesBaseUrl\", \"${userInfo?.imagesBaseUrl}\");"


//                    val js=
//                        "localStorage.setItem(\"loggedIn\", \"true\"); localStorage.setItem(\"token\", \"Bearer ${userInfo?.token}\");localStorage.setItem(\"imagesBaseUrl\", \"${userInfo?.imagesBaseUrl}\");"

                    val js=
                        "localStorage.setItem(\"loggedIn\", true); localStorage.setItem(\"token\", `Bearer ${userInfo?.token}`);localStorage.setItem(\"imagesBaseUrl\", ${userInfo?.imagesBaseUrl});"


                    Log.d("sendErez",js.toString())

                    webAlarms?.evaluateJavascript(js, null)
                }

            }

        }
        // Load your web application URL
        webAlarms?.loadUrl("https://outwatchpwa.sensoguard.com")

    }


}