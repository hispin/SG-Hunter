package com.sensoguard.hunter.classes

import com.google.gson.annotations.SerializedName

data class UserInfoAzure(
    @SerializedName("name") val name: String?,
    @SerializedName("pw") val pw: String?
)