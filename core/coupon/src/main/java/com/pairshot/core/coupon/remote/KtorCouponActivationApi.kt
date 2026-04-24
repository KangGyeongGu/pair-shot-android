package com.pairshot.core.coupon.remote

import com.pairshot.core.coupon.config.CouponApiConfig
import com.pairshot.core.coupon.remote.dto.ActivateRequestDto
import com.pairshot.core.coupon.remote.dto.ActivateResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorCouponActivationApi
    @Inject
    constructor(
        private val apiConfig: CouponApiConfig,
    ) : CouponActivationApi {
        private val client: HttpClient by lazy {
            HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            encodeDefaults = false
                        },
                    )
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = apiConfig.timeoutMillis
                    connectTimeoutMillis = apiConfig.timeoutMillis
                    socketTimeoutMillis = apiConfig.timeoutMillis
                }
            }
        }

        override suspend fun activate(request: ActivateRequestDto): ActivationApiResult {
            if (apiConfig.baseUrl.isBlank()) {
                Timber.w("Coupon API base URL is blank — activation disabled")
                return ActivationApiResult.NetworkError
            }
            val response = postActivate(request) ?: return ActivationApiResult.NetworkError
            return mapResponse(response)
        }

        private suspend fun postActivate(request: ActivateRequestDto): HttpResponse? {
            val url = apiConfig.baseUrl.trimEnd('/') + apiConfig.activatePath
            return try {
                client.post(url) {
                    contentType(ContentType.Application.Json)
                    apiConfig.authHeaderName?.let { name ->
                        apiConfig.authHeaderValue?.let { value -> header(name, value) }
                    }
                    header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    setBody(request)
                }
            } catch (t: java.io.IOException) {
                Timber.w(t, "Coupon activation network I/O error")
                null
            } catch (t: io.ktor.client.plugins.HttpRequestTimeoutException) {
                Timber.w(t, "Coupon activation timeout")
                null
            }
        }

        private suspend fun mapResponse(response: HttpResponse): ActivationApiResult =
            when (response.status) {
                HttpStatusCode.OK -> {
                    runCatching { response.body<ActivateResponseDto>() }
                        .map { ActivationApiResult.Success(it) as ActivationApiResult }
                        .getOrElse { ActivationApiResult.ServerError }
                }

                HttpStatusCode.NotFound -> {
                    ActivationApiResult.NotFound
                }

                HttpStatusCode.Conflict -> {
                    ActivationApiResult.AlreadyUsedOnAnotherDevice
                }

                HttpStatusCode.Gone -> {
                    ActivationApiResult.Revoked
                }

                HttpStatusCode.BadRequest -> {
                    ActivationApiResult.InvalidRequest
                }

                else -> {
                    ActivationApiResult.ServerError
                }
            }
    }
