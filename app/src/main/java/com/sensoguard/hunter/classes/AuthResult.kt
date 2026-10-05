package com.sensoguard.hunter.classes

import com.google.gson.annotations.SerializedName

class AuthResult {
    @SerializedName("token")
    val token: String? = null

    @SerializedName("username")
    val username: String? = null

    @SerializedName("firstName")
    // inspection: kept - (de)serialized by Gson
    @Suppress("unused")
    val firstName: String? = null

    @SerializedName("lastName")
    // inspection: kept - (de)serialized by Gson
    @Suppress("unused")
    val lastName: String? = null

    @SerializedName("role")
    val role: Int? = null

    @SerializedName("roleName")
    val roleName: String? = null

    @SerializedName("success")
    val success: Boolean? = null

    @SerializedName("errors")
    // inspection: kept - (de)serialized by Gson
    @Suppress("unused")
    val errors: String? = null

    @SerializedName("customer")
    val customer: Int? = null

    @SerializedName("watchCameras")
    // inspection: kept - (de)serialized by Gson
    @Suppress("unused")
    val watchCameras: ArrayList<Int>? = null

    @SerializedName("language")
    val language: String? = null

    @SerializedName("userAppId")
    val userAppId: Int? = null

    @SerializedName("env")
    val env: String? = null

    @SerializedName("imagesBaseUrl")
    val imagesBaseUrl: String? = null



}