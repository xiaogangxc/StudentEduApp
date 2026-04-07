package com.student.education.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES加密工具类
 */
object EncryptionUtil {
    
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_LENGTH = 32 // 256 bits
    
    /**
     * 加密数据
     * @param plainText 明文
     * @param password 密码
     * @return 加密后的Base64字符串
     */
    fun encrypt(plainText: String, password: String): String {
        try {
            val key = generateKey(password)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            // 生成随机IV
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // 将IV和密文拼接
            val combined = iv + encrypted
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("加密失败: ${e.message}")
        }
    }
    
    /**
     * 解密数据
     * @param encryptedText 加密后的Base64字符串
     * @param password 密码
     * @return 明文
     */
    fun decrypt(encryptedText: String, password: String): String {
        try {
            val key = generateKey(password)
            val combined = Base64.decode(encryptedText, Base64.DEFAULT)
            
            // 分离IV和密文
            val iv = combined.copyOfRange(0, 16)
            val encrypted = combined.copyOfRange(16, combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            
            val decrypted = cipher.doFinal(encrypted)
            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("解密失败: ${e.message}")
        }
    }
    
    /**
     * 从密码生成密钥
     */
    private fun generateKey(password: String): SecretKeySpec {
        // 将密码填充或截断到32字节
        val keyBytes = password.toByteArray(Charsets.UTF_8)
        val paddedKey = ByteArray(KEY_LENGTH)
        
        System.arraycopy(keyBytes, 0, paddedKey, 0, minOf(keyBytes.size, KEY_LENGTH))
        
        return SecretKeySpec(paddedKey, ALGORITHM)
    }
}