package com.sensoguard.hunter.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.UserInfoAmazon
import com.sensoguard.hunter.classes.UserInfoAzure
import com.sensoguard.hunter.controler.LoginViewModel
import com.sensoguard.hunter.global.AMAZON
import com.sensoguard.hunter.global.AMAZONE_POST_LOGIN_RESULT_FAILED
import com.sensoguard.hunter.global.AMAZONE_POST_LOGIN_RESULT_SUCCESS
import com.sensoguard.hunter.global.AMAZON_PRECESS_DIALOG_VALUE
import com.sensoguard.hunter.global.AZURA_POST_RESULT_ERROR_NO_DATA
import com.sensoguard.hunter.global.AZURA_POST_RESULT_NO_USER
import com.sensoguard.hunter.global.AZURA_POST_RESULT_OK
import com.sensoguard.hunter.global.AZURA_POST_RESULT_UNHUTHORIZED
import com.sensoguard.hunter.global.AZURA_POST_RESULT_USER_NO_ACTIVE
import com.sensoguard.hunter.global.AZURE
import com.sensoguard.hunter.global.LOGIN_TYPE_KEY
import com.sensoguard.hunter.global.REGISTER_ID_KEY
import com.sensoguard.hunter.global.ToastNotify
import com.sensoguard.hunter.global.USER_INFO_AMAZON_KEY
import com.sensoguard.hunter.global.USER_INFO_AZURE_KEY
import com.sensoguard.hunter.global.UserSession
import com.sensoguard.hunter.global.getStringInPreference
import com.sensoguard.hunter.global.getTagsFromLocally
import com.sensoguard.hunter.global.getUserAmazonResultFromLocally
import com.sensoguard.hunter.global.getUserAzureFromLocally
import com.sensoguard.hunter.global.removePreference
import com.sensoguard.hunter.global.setStringInPreference
import com.sensoguard.hunter.global.validIsEmpty
import com.sensoguard.hunter.services.RegistrationIntentService

open class LogInActivity : AppCompatActivity() {

    private var isLoginProcess: Boolean=false
    private var viewModel: LoginViewModel? = null
    private var dialog: AlertDialog? = null

    override fun onStart() {
        super.onStart()
        setFilter()
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        setListener()
    }

    /**
     * set observers
     */
    private fun setListener() {
        viewModel?.response?.observe(this) {
            when (it) {
                AMAZONE_POST_LOGIN_RESULT_SUCCESS -> {
                    if(isLoginProcess) {
                        ToastNotify(
                            resources.getString(R.string.verification_successfully),
                            this@LogInActivity
                        )
                        dialog?.dismiss()
                        pbValidation?.visibility=View.INVISIBLE
                        positiveButton?.isEnabled = true
                        isLoginProcess=false
                    }
                }

                AMAZONE_POST_LOGIN_RESULT_FAILED -> {
                    if(isLoginProcess) {
                        showErrorMsg(resources.getString(R.string.verification_failed))
                        positiveButton?.isEnabled = true
                        isLoginProcess=false
                    }
                }
            }
        }
    }

    private var tvError: TextView? = null
    private var pbValidation: ProgressBar? = null


    fun sendPostHubs() {
        // Start IntentService to register this application with FCM.
        val intent = Intent(this, RegistrationIntentService::class.java)
        intent.putExtra("actionType", "post")
        startService(intent)
    }

    //register token and tags to azura
    fun registerTokenAndTagToHubs() {

        val tags = getTagsFromLocally(this)
        val userInfo = getUserAzureFromLocally(this, USER_INFO_AZURE_KEY)

        if (tags != null && userInfo != null) {

            UserSession.instance.setTags(tags)
            UserSession.instance.setInstanceUserAzure(userInfo)

            // Start IntentService to register this application with FCM.
            val intent = Intent(this, RegistrationIntentService::class.java)
            intent.putExtra("actionType", "register")
            startService(intent)
        }
    }

    /**
     * start login amazon worker
     */
    fun loginAmazonFromDialog(processType: String) {
        viewModel?.requestLoginAmazon(processType)


    }


    var positiveButton: Button? = null

    //open dialog for log in
    fun openLogInDialog() {

        //show user info from locally if exist
        fun showUserIfExist(userInfoKey: String, userInput: EditText, pwInput: EditText) {
            val userInfo = getUserAmazonResultFromLocally(
                this,
                userInfoKey
            )
            userInfo?.password?.let { Log.d("testUser", it) }
            if (userInfo != null) {
                userInput.setText(userInfo.email)
                pwInput.setText(userInfo.password)
            }
        }


        val li: LayoutInflater = LayoutInflater.from(this)
        val promptsView: View = li.inflate(R.layout.user_password_dialog, null)

        val userInput: EditText = promptsView
            .findViewById(R.id.editTextDialogUserInput) as EditText

        /**
         * cancel error message before text change
         */
        userInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                tvError?.visibility = View.INVISIBLE
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
            }
        })

        val pwInput: EditText = promptsView
            .findViewById(R.id.editTextDialogPasswordInput) as EditText

        /**
         * cancel error message before text change
         */
        pwInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                tvError?.visibility = View.INVISIBLE
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
            }
        })

        var loginType = getStringInPreference(this, LOGIN_TYPE_KEY, AZURE)
        //select AMAZON or AZURE
        val rgLoginType: RadioGroup? = promptsView.findViewById(R.id.rgLoginType)
        rgLoginType?.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.rbV1) {
                loginType = AZURE
                setStringInPreference(this, LOGIN_TYPE_KEY, AZURE)
                showUserIfExist(USER_INFO_AZURE_KEY, userInput, pwInput)
            } else if (checkedId == R.id.rbV2) {
                loginType = AMAZON
                setStringInPreference(this, LOGIN_TYPE_KEY, AMAZON)
                showUserIfExist(USER_INFO_AMAZON_KEY, userInput, pwInput)
            }
        }
        if (loginType.equals(AZURE)) {
            rgLoginType?.check(R.id.rbV1)
            showUserIfExist(USER_INFO_AZURE_KEY, userInput, pwInput)
        } else if (loginType.equals(AMAZON)) {
            rgLoginType?.check(R.id.rbV2)
            showUserIfExist(USER_INFO_AMAZON_KEY, userInput, pwInput)
        }


        val userName = UserSession.instance.getUserAzure()?.name
        userName?.let { userInput.setText(it) }

        val pw = UserSession.instance.getUserAzure()?.pw
        pw?.let { pwInput.setText(it) }

        tvError = promptsView.findViewById<TextView>(R.id.tvError)
        tvError?.visibility = View.INVISIBLE

        pbValidation = promptsView.findViewById<ProgressBar>(R.id.pbValidation)

        dialog = AlertDialog.Builder(this)
            .setView(promptsView)
            .setCancelable(false)
            .show()

        positiveButton = promptsView.findViewById(R.id.btnSubmit) as Button
        positiveButton?.setOnClickListener(View.OnClickListener {

            if (validIsEmpty(userInput, this)
                && validIsEmpty(pwInput, this)
            ) {

                positiveButton?.isEnabled = false
                tvError?.visibility = View.INVISIBLE

                pbValidation?.visibility = View.VISIBLE

                isLoginProcess = true

                if (loginType.equals(AZURE)) {

                    UserSession.instance.setInstanceUserAzure(
                        UserInfoAzure(
                            userInput.text.toString(),
                            pwInput.text.toString()
                        )
                    )
                    //send post
                    sendPostHubs()
                } else if (loginType.equals(AMAZON)) {
                    UserSession.instance.setInstanceUserAmazon(
                        UserInfoAmazon(
                            userInput.text.toString(),
                            pwInput.text.toString(),
                            null
                        )
                    )
                    //send post
                    loginAmazonFromDialog(AMAZON_PRECESS_DIALOG_VALUE)
                }
            }
        })
        val negativeButton: Button = promptsView.findViewById(R.id.btnCancel) as Button
        negativeButton.setOnClickListener(View.OnClickListener {
            dialog?.dismiss()
        })
    }


    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(usbReceiver)
        } catch (ex: Exception) {

        }
    }

    //define actions broadcast
    private fun setFilter() {
        val filter = IntentFilter(AZURA_POST_RESULT_OK)
        filter.addAction(AZURA_POST_RESULT_UNHUTHORIZED)
        filter.addAction(AZURA_POST_RESULT_NO_USER)
        filter.addAction(AZURA_POST_RESULT_USER_NO_ACTIVE)
        filter.addAction(AZURA_POST_RESULT_ERROR_NO_DATA)
        filter.addAction(AMAZONE_POST_LOGIN_RESULT_SUCCESS)
        filter.addAction(AMAZONE_POST_LOGIN_RESULT_FAILED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(usbReceiver, filter)
        }
    }

    //define class broadcast
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            when {
                arg1.action == AZURA_POST_RESULT_OK -> {
                    ToastNotify(
                        resources.getString(R.string.verification_successfully),
                        this@LogInActivity
                    )
                    dialog?.dismiss()
                    pbValidation?.visibility = View.INVISIBLE
                    //remove it in order to force renew the token registeration
                    removePreference(this@LogInActivity, REGISTER_ID_KEY)
                    registerTokenAndTagToHubs()
                }
                arg1.action == AZURA_POST_RESULT_UNHUTHORIZED -> {
                    showErrorMsg(resources.getString(R.string.validation_error))
                }

                arg1.action == AZURA_POST_RESULT_NO_USER -> {
                    showErrorMsg(resources.getString(R.string.no_user_found))
                }

                arg1.action == AZURA_POST_RESULT_USER_NO_ACTIVE -> {
                    showErrorMsg(resources.getString(R.string.no_active_user_found))
                }

                arg1.action == AZURA_POST_RESULT_ERROR_NO_DATA -> {
                    showErrorMsg(resources.getString(R.string.error_no_data))
                }

                arg1.action == AZURA_POST_RESULT_UNHUTHORIZED -> {
                    showErrorMsg(resources.getString(R.string.validation_error))
                }

                arg1.action == AMAZONE_POST_LOGIN_RESULT_SUCCESS -> {
                    ToastNotify(
                        resources.getString(R.string.verification_successfully),
                        this@LogInActivity
                    )
                    dialog?.dismiss()
                    pbValidation?.visibility = View.INVISIBLE
                }

                arg1.action == AMAZONE_POST_LOGIN_RESULT_FAILED -> {
                    showErrorMsg(resources.getString(R.string.verification_failed))
                }
            }
            positiveButton?.isEnabled = true
        }
    }


    //show error message when log in
    private fun showErrorMsg(msg: String) {
        tvError?.visibility = View.VISIBLE
        tvError?.text = msg
        pbValidation?.visibility = View.INVISIBLE
    }
}