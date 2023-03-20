package com.sensoguard.hunter.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.UserInfo
import com.sensoguard.hunter.global.*
import com.sensoguard.hunter.services.RegistrationIntentService

open class LogInActivity : AppCompatActivity() {

    private var dialog: AlertDialog? = null

    override fun onStart() {
        super.onStart()
        setFilter()

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
        val userInfo = getUserFromLocally(this)

        if (tags != null && userInfo != null) {

            UserSession.instance.setTags(tags)
            UserSession.instance.setInstanceUser(userInfo)

            // Start IntentService to register this application with FCM.
            val intent = Intent(this, RegistrationIntentService::class.java)
            intent.putExtra("actionType", "register")
            startService(intent)
        }
    }

    var positiveButton: Button? = null

    //open dialog for log in
    fun openLogInDialog() {

        val li: LayoutInflater = LayoutInflater.from(this)
        val promptsView: View = li.inflate(R.layout.user_password_dialog, null)

        val userInput: EditText = promptsView
            .findViewById(R.id.editTextDialogUserInput) as EditText

        val userName = UserSession.instance.getUser()?.name
        userName?.let { userInput.setText(it) }

        val pwInput: EditText = promptsView
            .findViewById(R.id.editTextDialogPasswordInput) as EditText

        val pw = UserSession.instance.getUser()?.pw
        pw?.let { pwInput.setText(it) }

        tvError = promptsView.findViewById<TextView>(R.id.tvError)
        tvError?.visibility = View.INVISIBLE

        pbValidation = promptsView.findViewById<ProgressBar>(R.id.pbValidation)

        dialog = AlertDialog.Builder(this)
            .setView(promptsView)
            .setCancelable(false)
            .show()

        positiveButton = promptsView.findViewById(R.id.btnSubmit) as Button
        //dialog?.getButton(AlertDialog.BUTTON_POSITIVE)!!
        positiveButton?.setOnClickListener(View.OnClickListener {

            if (validIsEmpty(userInput, this)
                && validIsEmpty(pwInput, this)
            ) {

                positiveButton?.isEnabled = false
                tvError?.visibility = View.INVISIBLE

                pbValidation?.visibility = View.VISIBLE

                UserSession.instance.setInstanceUser(
                    UserInfo(
                        userInput.text.toString(),
                        pwInput.text.toString()
                    )
                )
                //send post
                sendPostHubs()
            }

            //     Toast.makeText(SysManagerActivity.this, "dialog is open", Toast.LENGTH_SHORT).show();
        })
        val negativeButton: Button = promptsView.findViewById(R.id.btnCancel) as Button
        //dialog?.getButton(AlertDialog.BUTTON_POSITIVE)!!
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
        registerReceiver(usbReceiver, filter)
    }

    //define class broadcast
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(arg0: Context, arg1: Intent) {
            when {
                arg1.action == AZURA_POST_RESULT_OK -> {
                    ToastNotify(
                        resources.getString(R.string.validation_successfully),
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