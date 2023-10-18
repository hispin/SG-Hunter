package com.sensoguard.hunter.classes

import com.google.gson.annotations.SerializedName

class AuthResult {
    @SerializedName("token")
    val token: String? = null

    @SerializedName("username")
    val username: String? = null

    @SerializedName("firstName")
    val firstName: String? = null

    @SerializedName("lastName")
    val lastName: String? = null

    @SerializedName("role")
    val role: Int? = null

    @SerializedName("roleName")
    val roleName: String? = null

    @SerializedName("success")
    val success: Boolean? = null

    @SerializedName("errors")
    val errors: String? = null

    @SerializedName("customer")
    val customer: Int? = null

    @SerializedName("watchCameras")
    val watchCameras: ArrayList<Int>? = null

    @SerializedName("language")
    val language: String? = null

    @SerializedName("userAppId")
    val userAppId: Int? = null

    @SerializedName("env")
    val env: String? = null

}