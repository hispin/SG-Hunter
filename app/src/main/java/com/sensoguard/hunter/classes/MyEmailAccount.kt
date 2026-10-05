package com.sensoguard.hunter.classes

class MyEmailAccount {
    var emailAddress: String? = null
    var password: String? = null
    var emailServer: String? = null
    var emailPort: String? = null
    var isUseSSL: Boolean = false

    // inspection fix: removed unused constructor (Gson uses the default one)
}