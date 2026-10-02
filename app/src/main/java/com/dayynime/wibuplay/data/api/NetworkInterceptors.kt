package com.dayynime.wibuplay.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Referer", Constants.REFERER)
            .header("User-Agent", Constants.USER_AGENT)
            .build()
        return chain.proceed(request)
    }
}

class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            return chain.proceed(request)
        } catch (e: IOException) {
            // GET yang gagal karena IOException retry 1x (bukan untuk timeout)
            if (request.method.equals("GET", ignoreCase = true) && e !is SocketTimeoutException) {
                return chain.proceed(request)
            }
            throw e
        }
    }
}
