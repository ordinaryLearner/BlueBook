package com.czcz.myapp.Api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 火山引擎机器翻译 REST 客户端（V4 签名）。
 * 不依赖官方 Java SDK，避免 Apache HttpClient / Netty 与 Android 系统的类冲突。
 */
object VolcTranslateClient {

    private const val ACCESS_KEY = "填你的AK"
    private const val SECRET_KEY = "填你的SK"

    private const val HOST = "open.volcengineapi.com"
    private const val REGION = "cn-north-1"
    private const val SERVICE = "translate"
    private const val VERSION = "2020-06-01"

    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /** 翻译；target 目标语言，source 传 "auto" 由服务端自动识别 */
    fun translate(text: String, target: String = "zh", source: String = "auto"): String {
        val bodyJson = JSONObject().apply {
            put("TargetLanguage", target)
            put("SourceLanguage", source)
            put("TextList", JSONArray().put(text))
        }.toString()

        val now = Date()
        val amzDate = iso8601(now)
        val dateStamp = amzDate.substring(0, 8)
        val payloadHash = sha256Hex(bodyJson)

        val canonicalQuery = "Action=TranslateText&Version=$VERSION"
        val canonicalHeaders =
            "content-type:application/json\n" +
                    "host:$HOST\n" +
                    "x-content-sha256:$payloadHash\n" +
                    "x-date:$amzDate\n"
        val signedHeaders = "content-type;host;x-content-sha256;x-date"
        val canonicalRequest = listOf(
            "POST", "/", canonicalQuery,
            canonicalHeaders, signedHeaders, payloadHash
        ).joinToString("\n")

        val credentialScope = "$dateStamp/$REGION/$SERVICE/request"
        val stringToSign = listOf(
            "HMAC-SHA256", amzDate, credentialScope, sha256Hex(canonicalRequest)
        ).joinToString("\n")

        val signingKey = signatureKey(dateStamp)
        val signature = hmacHex(signingKey, stringToSign)
        val authorization =
            "HMAC-SHA256 Credential=$ACCESS_KEY/$credentialScope, " +
                    "SignedHeaders=$signedHeaders, Signature=$signature"

        val request = Request.Builder()
            .url("https://$HOST/?$canonicalQuery")
            .post(bodyJson.toRequestBody(jsonMediaType))
            .addHeader("Host", HOST)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Date", amzDate)
            .addHeader("X-Content-Sha256", payloadHash)
            .addHeader("Authorization", authorization)
            .build()

        client.newCall(request).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return ""
            // 响应结构：{"TranslationList":[{"Translation":"..."}]}
            val list = JSONObject(raw).optJSONArray("TranslationList") ?: return ""
            return if (list.length() > 0) list.getJSONObject(0).optString("Translation") else ""
        }
    }

    private fun signatureKey(dateStamp: String): ByteArray {
        val kDate = hmac(("VOLC" + SECRET_KEY).toByteArray(), dateStamp)
        val kRegion = hmac(kDate, REGION)
        val kService = hmac(kRegion, SERVICE)
        return hmac(kService, "request")
    }

    private fun iso8601(date: Date): String {
        val fmt = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(date)
    }

    private fun sha256Hex(s: String): String = hex(MessageDigest.getInstance("SHA-256").digest(s.toByteArray()))

    private fun hmac(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray())
    }

    private fun hmacHex(key: ByteArray, data: String): String = hex(hmac(key, data))

    private fun hex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }
}