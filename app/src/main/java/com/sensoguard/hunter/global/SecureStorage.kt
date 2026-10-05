package com.sensoguard.hunter.global

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * encrypts values stored in preferences with a key kept in the Android Keystore,
 * used for user info that contains the password
 */
private const val KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "outwatch_user_info"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV_SIZE = 12
private const val TAG_BITS = 128
// marks encrypted values, so plain values saved by older versions can still be read
private const val ENC_PREFIX = "enc1:"

private fun getOrCreateKey(): SecretKey {
    val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
    (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

    val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
    generator.init(
        KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
    )
    return generator.generateKey()
}

private fun encrypt(plain: String): String {
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
    val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
    return ENC_PREFIX + Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
}

private fun decrypt(stored: String): String {
    val bytes = Base64.decode(stored.removePrefix(ENC_PREFIX), Base64.NO_WRAP)
    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(
        Cipher.DECRYPT_MODE,
        getOrCreateKey(),
        GCMParameterSpec(TAG_BITS, bytes, 0, IV_SIZE)
    )
    return String(cipher.doFinal(bytes, IV_SIZE, bytes.size - IV_SIZE), Charsets.UTF_8)
}

fun setSecureStringInPreference(context: Context?, key: String, value: String) {
    try {
        setStringInPreference(context, key, encrypt(value))
    } catch (e: Exception) {
        // never fall back to saving the value as plain text
        Log.e("SecureStorage", "failed to encrypt $key", e)
    }
}

/**
 * returns the decrypted value; a plain value from an older version is
 * encrypted in place, and a value that cannot be decrypted (e.g. restored
 * from backup on another device) is removed so the user logs in again
 */
fun getSecureStringInPreference(context: Context?, key: String): String? {
    val stored = getStringInPreference(context, key, null) ?: return null
    if (!stored.startsWith(ENC_PREFIX)) {
        setSecureStringInPreference(context, key, stored)
        return stored
    }
    return try {
        decrypt(stored)
    } catch (e: Exception) {
        Log.e("SecureStorage", "failed to decrypt $key", e)
        setStringInPreference(context, key, null)
        null
    }
}
