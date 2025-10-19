package com.dmd.tasky.feature.auth.data.remote

import com.dmd.tasky.feature.auth.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("x-api-key", BuildConfig.API_KEY)
            .build()
        return chain.proceed(request)

    }
}