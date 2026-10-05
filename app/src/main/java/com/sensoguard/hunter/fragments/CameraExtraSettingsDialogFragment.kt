package com.sensoguard.hunter.fragments

// inspection fix: removed unused import(s)
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.FEATURE_NO_TITLE
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.ImageStorageManager
import com.sensoguard.hunter.global.CAMERA_KEY
import com.sensoguard.hunter.global.ERROR_RESULT_VALIDATION_EMAIL_ACTION
import com.sensoguard.hunter.global.ERROR_VALIDATION_EMAIL_MSG_KEY
import com.sensoguard.hunter.global.LAST_DATE_ALARM
import com.sensoguard.hunter.global.REQUEST_KEY
import com.sensoguard.hunter.global.RESULT_CODE
import com.sensoguard.hunter.global.RESULT_VALIDATION_EMAIL_ACTION
import com.sensoguard.hunter.global.VALIDATION_EMAIL_RESULT
import com.sensoguard.hunter.global.convertJsonToSensor
import com.sensoguard.hunter.global.convertToGson
import com.sensoguard.hunter.global.getScreenWidth
import com.sensoguard.hunter.global.removePreference


class CameraExtraSettingsDialogFragment : DialogFragment(), View.OnClickListener {

    //set as global in order to show it as big picture
    private var bitmap: Bitmap? = null
    private var btnSave: AppCompatButton? = null
    private var btnCancel: AppCompatButton? = null
    private var myCamera: Camera? = null
    private var ibTakePic: AppCompatImageButton? = null
    private var ibShowPicture: AppCompatImageButton? = null
    private var etEmailAddress: AppCompatEditText? = null
    private var etSysName: AppCompatEditText? = null
    private var etTelNum: AppCompatEditText? = null
    private var btnGetSnapshot: Button? = null
    private var btnDeleteAllImages: Button? = null
    private var btnGetBatteryStatus: Button? = null
    private var btnGetParameters: Button? = null
    private var btnArmCamera: Button? = null
    private var btnDisarmCamera: Button? = null
    private var btnSetEmailRecipients: Button? = null
    private var btnSetMmsRecipients: Button? = null
    private var btnSetAdmin: AppCompatButton? = null
    //    private var tvLastVisitValue: AppCompatTextView? = null
//    private var ibOpenDatePicker: AppCompatImageButton? = null
    private var spCameraType: AppCompatSpinner? = null
    private var pbValidationEmail: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_camera_extra_settings,
            container,
            false
        )
        //prevent click below
        //dialog?.setCanceledOnTouchOutside(true)
        isCancelable = false


        initViews(view)


        val bundle = arguments
        val cameraStr = bundle?.getString(CAMERA_KEY, null)
        cameraStr?.let {
            myCamera = convertJsonToSensor(cameraStr)
            populateMyFields()
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun initViews(view: View?) {
        btnSave = view?.findViewById(R.id.btnSave)
        btnSave?.setOnClickListener {
            populateMyCamera()
            saveLastVisitPicture()
            //remove last date and enable scan last date
            removePreference(activity, LAST_DATE_ALARM)
            sendResult()

        }

        btnCancel = view?.findViewById(R.id.btnCancel)
        btnCancel?.setOnClickListener {
            //(view.parent as ViewGroup).removeView(view)
            dismiss()
        }

        ibTakePic = view?.findViewById(R.id.ibTakePic)
        ibTakePic?.setOnClickListener {
            // inspection fix: removed unused myIntent
            pickCamera()
        }

        ibShowPicture = view?.findViewById(R.id.ibShowPicture)
        ibShowPicture?.setOnClickListener {
            openLargePictureDialog()
        }

        etEmailAddress = view?.findViewById(R.id.etMailAddress)
        etSysName = view?.findViewById(R.id.etSysName)
        etTelNum = view?.findViewById(R.id.etTelNum)


        btnGetSnapshot = view?.findViewById(R.id.btnGetSnapshot)
        btnGetSnapshot?.setOnClickListener(this)
        btnDeleteAllImages = view?.findViewById(R.id.btnDeleteAllImages)
        btnDeleteAllImages?.setOnClickListener(this)
        btnGetBatteryStatus = view?.findViewById(R.id.btnGetBatteryStatus)
        btnGetBatteryStatus?.setOnClickListener(this)
        btnGetParameters = view?.findViewById(R.id.btnGetParameters)
        btnGetParameters?.setOnClickListener(this)
        btnArmCamera = view?.findViewById(R.id.btnArmCamera)
        btnArmCamera?.setOnClickListener(this)
        btnDisarmCamera = view?.findViewById(R.id.btnDisarmCamera)
        btnDisarmCamera?.setOnClickListener(this)
        btnSetEmailRecipients = view?.findViewById(R.id.btnSetEmailRecipients)
        btnSetEmailRecipients?.setOnClickListener(this)
        btnSetMmsRecipients = view?.findViewById(R.id.btnSetMmsRecipients)
        btnSetMmsRecipients?.setOnClickListener(this)
        btnSetAdmin = view?.findViewById(R.id.btnSetAdmin)
        btnSetAdmin?.setOnClickListener(this)
//        tvLastVisitValue = view?.findViewById(R.id.tvLastVisitValue)
//        ibOpenDatePicker = view?.findViewById(R.id.ibOpenDatePicker)


//        ibOpenDatePicker?.setOnClickListener {
//            if (context != null) {
//                //open date picker to update the dates
//                openDatePicker()
//            }
//        }


        spCameraType = view?.findViewById(R.id.spCameraType)
        pbValidationEmail = view?.findViewById(R.id.pbValidationEmail)

    }

    // inspection fix: removed unused openDatePicker()

    //populate camera object to return it main screen
    private fun populateMyFields() {
        //etEmailServer?.setText(myCamera?.emailServer)
        //etEmailPort?.setText(myCamera?.emailPort)
        etEmailAddress?.setText(myCamera?.emailAddress)


        etSysName?.setText(myCamera?.getName())
        etTelNum?.setText(myCamera?.phoneNum)
        //tvLastVisitValue?.text = myCamera?.lastVisitDate
        myCamera?.cameraModelPosition?.let { spCameraType?.setSelection(it) }
        myCamera?.lastVisitPicturePath?.let {
            if (context != null) {
                bitmap = ImageStorageManager.getImageFromInternalStorage(
                    requireContext(),
                    "image" + myCamera?.getId()
                )
                bitmap?.let { ibShowPicture?.setImageBitmap(bitmap) }
            }
        }
    }


    //populate camera object to return it main screen
    private fun populateMyCamera() {
        myCamera?.emailAddress = etEmailAddress?.text.toString()
        myCamera?.setName(etSysName?.text.toString())
        myCamera?.phoneNum = etTelNum?.text.toString()
        //myCamera?.lastVisitDate = tvLastVisitValue?.text.toString()
        myCamera?.cameraModel = spCameraType?.selectedItem.toString()
        spCameraType?.selectedItemPosition?.let { myCamera?.cameraModelPosition = it }
    }

    private fun saveLastVisitPicture() {
        bitmap?.let {
            if (context != null) {
                myCamera?.lastVisitPicturePath = ImageStorageManager.saveToInternalStorage(
                    requireContext(),
                    it,
                    "image" + myCamera?.getId()
                )
            }
        }
    }

    //open large picture
    private fun openLargePictureDialog() {
        if (context != null && bitmap != null) {
            val settingsDialog = Dialog(requireContext())
            if (settingsDialog.window != null) {
                settingsDialog.window!!.requestFeature(FEATURE_NO_TITLE)
                // lint: no parent exists before setContentView, so null root is intended
                @SuppressLint("InflateParams")
                val view = layoutInflater.inflate(R.layout.fragment_large_picture_video, null)
                settingsDialog.setContentView(view)
                val ibClose = view.findViewById<AppCompatImageButton>(R.id.ibClose)
                ibClose.setOnClickListener {
                    settingsDialog.dismiss()
                }

                val width = (bitmap?.width) as Int
                val height = (bitmap?.height) as Int

                val dialogWidth = getScreenWidth(activity) * 5 / 6
                val dialogHeight = (height / width) * dialogWidth///dialogWidth* 4 / 3

                //val newBitmap=Bitmap.createScaledBitmap(bitmap,dialogWidth,dialogHeight, true)
                //val newBitmap=Bitmap.createScaledBitmap(bitmap,(bitmap?.width?.times(10))as Int, (bitmap?.height?.times(10))as Int, true)

                val ivMyCaptureImage = view.findViewById<AppCompatImageView>(R.id.ivMyCaptureImage)
                //resize the image view according to the size of the bitmap
                ivMyCaptureImage.layoutParams.height = dialogHeight
                ivMyCaptureImage.layoutParams.width = dialogWidth
                ivMyCaptureImage.requestLayout()
                ivMyCaptureImage?.setImageBitmap(bitmap)//newBitmap
                settingsDialog.show()
            }
        }
    }

    private fun sendResult() {
        if (parentFragmentManager == null) {
            return
        }
        val result =Bundle()
        result.putInt(RESULT_CODE, Activity.RESULT_OK)
        val cameraStr = myCamera?.let { convertToGson(it) }
        cameraStr?.let {
            result.putString(CAMERA_KEY, cameraStr)
            //intent.putExtras(bdl)
        }
        parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
        dismiss()
    }



    var uri: Uri?=null

    /**
     * define listener
     */
    var startCamera: ActivityResultLauncher<Intent> =
        registerForActivityResult(StartActivityForResult()) { result -> // inspection fix: lambda instead of object literal
                    if (result.resultCode == Activity.RESULT_OK) run {
                        ibShowPicture?.setImageURI(uri)
                    }

                }

    /**
     * take a picture
     */
    fun pickCamera() {
        val values=ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        uri=requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        val cameraIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startCamera.launch(cameraIntent)
    }



    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(reciever)
    }

    private fun setFilter() {
        val filter = IntentFilter(RESULT_VALIDATION_EMAIL_ACTION)
        filter.addAction(ERROR_RESULT_VALIDATION_EMAIL_ACTION)
        activity?.let {
            ContextCompat.registerReceiver(it, reciever, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
    }

    override fun onStart() {
        super.onStart()
        setFilter()
    }

    private val reciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == RESULT_VALIDATION_EMAIL_ACTION) {
                pbValidationEmail?.visibility = View.GONE
                val resultValidationEmail = intent.getBooleanExtra(VALIDATION_EMAIL_RESULT, false)
                if (resultValidationEmail) {
                    saveLastVisitPicture()
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.verification_successfully),
                        Toast.LENGTH_LONG
                    ).show()
                    sendResult()
                }
//                else{
//                    Toast.makeText(activity,resources.getString(R.string.validation_error),Toast.LENGTH_LONG).show()
//                }
            } else if (intent?.action == ERROR_RESULT_VALIDATION_EMAIL_ACTION) {
                val errorMsg = intent.getStringExtra(ERROR_VALIDATION_EMAIL_MSG_KEY)
                errorMsg?.let { Toast.makeText(activity, it, Toast.LENGTH_LONG).show() }
            }
        }

    }

    override fun onClick(v: View?) {

        if (myCamera == null || myCamera?.phoneNum == null) {
            Toast.makeText(
                activity,
                resources.getString(R.string.missing_phone_number),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (myCamera?.cameraModel.equals("MG-983G-30M") ||
            myCamera?.cameraModel.equals("MG-984G-36M") ||
            myCamera?.cameraModel.equals("MG-984G-30M") ||
            myCamera?.cameraModel.equals("MG-983G-36M")
        ) {

            // inspection fix: cascade 'if' replaced with 'when', commands sent directly
            when (v?.id) {
                R.id.btnGetSnapshot -> sendSMS("#T#E#")
                R.id.btnDeleteAllImages -> sendSMS("#F#")
                R.id.btnGetBatteryStatus -> sendSMS("#C#")
                R.id.btnGetParameters -> sendSMS("#L#")
                R.id.btnArmCamera -> sendSMS("#A#")
                R.id.btnDisarmCamera -> sendSMS("#D#")
                R.id.btnSetEmailRecipients -> showEmailsDialog()
                R.id.btnSetMmsRecipients -> showPhoneNumDialog()
                R.id.btnSetAdmin -> showSetAdminDialog()
            }


        }
    }

    private fun sendSMS(command: String?) {
        val uri = ("smsto:" + myCamera?.phoneNum).toUri() // lint fix: KTX toUri
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", command)
        startActivity(intent)
    }

    //show dialog with emails fields
    private fun showEmailsDialog() {

        if (this@CameraExtraSettingsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraExtraSettingsDialogFragment.requireContext())
            dialog.setContentView(R.layout.custom_dialog_emails_commands)

            dialog.setCancelable(true)

            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)
            val etField2 = dialog.findViewById<AppCompatEditText>(R.id.etField2)
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etField3)
            val etField4 = dialog.findViewById<AppCompatEditText>(R.id.etField4)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "#R#"
                //insert "#" also if the there is no email
                //delete "-"
                command += etField1.text?.toString() + "#"
                command += etField2.text?.toString() + "#"
                command += etField3.text?.toString() + "#"
                command += etField4.text?.toString() + "#"
//                command = addEmailToCommand(command, etField1)
//                command = addEmailToCommand(command, etField2)
//                command = addEmailToCommand(command, etField3)
//                command = addEmailToCommand(command, etField4)
//                if (command == "#R#") {
//                    Toast.makeText(
//                        activity,
//                        resources.getString(R.string.you_need_at_least_one_email),
//                        Toast.LENGTH_LONG
//                    ).show()
//                } else {
                sendSMS(command)
                dialog.dismiss()
//                }

            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }


    }

    //add phone number to command if it is not empty
    private fun addPhoneNumToCommand(
        command: String,
        etField: AppCompatEditText
    ): String {

        var myCommand = command
        if (etField.text.toString().startsWith("000")) {
            myCommand += "#"
        } else {
            //delete "-"
            var phnum = etField.text?.toString()
            phnum = phnum?.replace("-", "")
            myCommand += "$phnum#"
        }
//        if (etField.text != null
//            && etField.text.toString().isNotEmpty()
//            && !etField.text.toString().startsWith("000")
//        ) {
//            myCommand += etField.text?.toString() + "#"
//        }
        return myCommand
    }

    // inspection fix: removed unused addEmailToCommand()

    //show dialog with phone numbers fields
    private fun showPhoneNumDialog() {

        if (this@CameraExtraSettingsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraExtraSettingsDialogFragment.requireContext())
            dialog.setContentView(R.layout.custom_dialog_phonenum_command)

            dialog.setCancelable(true)

            val tvCommandTitle = dialog.findViewById<AppCompatTextView>(R.id.tvCommandTitle)
            tvCommandTitle.text = resources.getString(R.string.phone_num_title)
            val etField1 = dialog.findViewById<AppCompatEditText>(R.id.etField1)
            val etField2 = dialog.findViewById<AppCompatEditText>(R.id.etField2)
            val etField3 = dialog.findViewById<AppCompatEditText>(R.id.etField3)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                var command = "#N#"
                command = addPhoneNumToCommand(command, etField1)
                command = addPhoneNumToCommand(command, etField2)
                command = addPhoneNumToCommand(command, etField3)
                sendSMS(command)
                dialog.dismiss()
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    //show dialog to set admin
    private fun showSetAdminDialog() {

        if (this@CameraExtraSettingsDialogFragment.context != null) {
            val dialog = Dialog(this@CameraExtraSettingsDialogFragment.requireContext())
            dialog.setContentView(R.layout.custom_dialog_set_admin_command)

            dialog.setCancelable(true)


            val etAdminPhone = dialog.findViewById<AppCompatEditText>(R.id.etAdminPhone)
            val etPassword = dialog.findViewById<AppCompatEditText>(R.id.etPassword)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)
            btnSendCommand.setOnClickListener {
                if (!etAdminPhone.text.toString().startsWith("000")
                    && validIsEmpty(etPassword)
                ) {
                    var command = "#" + myCamera?.cameraModel + "#"
                    command += etPassword.text.toString() + "#"
                    var phAdmin = etAdminPhone.text.toString()
                    phAdmin = phAdmin.replace("-", "")
                    command += "$phAdmin#"
                    sendSMS(command)
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        activity,
                        resources.getString(R.string.incorrect_phone_number_or_password),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun validIsEmpty(editText: EditText): Boolean {
        var isValid = true

        if (editText.text.isNullOrBlank()) {
            editText.error =
                resources.getString(R.string.empty_field_error)
            isValid = false
        }

        return isValid
    }


}