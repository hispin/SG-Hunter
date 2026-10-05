package com.sensoguard.hunter.classes

class UserInfoAmazonResult(
    val email: String?,
    val password: String?,
    // inspection: kept - (de)serialized by Gson
    @Suppress("unused")
    val token_fcm: String?,
    val token: String?,
    val imagesBaseUrl: String?,
    val role: Int?,
    val userAppId: Int?,
    val username: String?,
    val roleName: String?,
    val customer: Int?,
    val language: String?,
    val env: String?
)