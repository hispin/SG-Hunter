package com.sensoguard.hunter.classes

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("name") val name: String?,
    @SerializedName("pw") val pw: String?

//    @SerializedName("user_id") val userId: Int?,
//    @SerializedName("user_name") val userName: String?,
//    @SerializedName("user_email") val userEmail: String?,
//    @SerializedName("user_age") val userAge: String?,
//    @SerializedName("user_uid") val userUid: String?
)