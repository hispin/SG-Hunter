package com.sensoguard.hunter.classes

import com.google.gson.annotations.SerializedName

class UserInfoAmazon(
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("password2") val token_fcm: String?
)
