package com.pairshot.core.coupon.remote

import com.pairshot.core.coupon.config.CouponApiConfig
import com.pairshot.core.coupon.remote.dto.ActivateRequestDto
import com.pairshot.core.coupon.remote.dto.ActivateResponseDto
import com.pairshot.core.coupon.remote.dto.CouponListRequestDto
import com.pairshot.core.coupon.remote.dto.CouponListResponseDto
import com.pairshot.core.coupon.remote.dto.ErrorResponseDto
import com.pairshot.core.coupon.remote.dto.StatusRequestDto
import com.pairshot.core.coupon.remote.dto.StatusResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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
            val response = postJson(apiConfig.activatePath, request) ?: return ActivationApiResult.NetworkError
            return mapResponse(response)
        }

        override suspend fun fetchStatus(request: StatusRequestDto): StatusApiResult {
            if (apiConfig.baseUrl.isBlank()) return StatusApiResult.NetworkError
            val response = postJson(apiConfig.statusPath, request) ?: return StatusApiResult.NetworkError
            return mapStatusResponse(response)
        }

        override suspend fun fetchMyCoupons(deviceHash: String): ListApiResult {
            if (apiConfig.baseUrl.isBlank()) return ListApiResult.NetworkError
            val response =
                postJson(apiConfig.byDevicePath, CouponListRequestDto(deviceHash = deviceHash))
                    ?: return ListApiResult.NetworkError
            return mapListResponse(response)
        }

        private suspend inline fun <reified T> postJson(
            path: String,
            body: T,
        ): HttpResponse? {
            val url = apiConfig.baseUrl.trimEnd('/') + path
            return try {
                client.post(url) {
                    contentType(ContentType.Application.Json)
                    apiConfig.authHeaderName?.let { name ->
                        apiConfig.authHeaderValue?.let { value -> header(name, value) }
                    }
                    header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    setBody(body)
                }
            } catch (t: java.io.IOException) {
                Timber.w(t, "Coupon API network I/O error — path=%s", path)
                null
            } catch (t: io.ktor.client.plugins.HttpRequestTimeoutException) {
                Timber.w(t, "Coupon API timeout — path=%s", path)
                null
            }
        }

        private suspend fun mapResponse(response: HttpResponse): ActivationApiResult =
            when (response.status) {
                HttpStatusCode.OK -> {
                    runCatching { response.body<ActivateResponseDto>() }
                        .map { ActivationApiResult.Success(it) as ActivationApiResult }
                        .getOrElse {
                            Timber.tag(API_TAG).w(it, "200 response body decode failed")
                            ActivationApiResult.ServerError
                        }
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
                    val body = runCatching { response.body<ErrorResponseDto>() }.getOrNull()
                    when (body?.error) {
                        "INVALID_CODE_FORMAT" -> {
                            ActivationApiResult.InvalidCodeFormat
                        }

                        "INVALID_SIGNATURE" -> {
                            ActivationApiResult.InvalidSignature
                        }

                        else -> {
                            Timber.tag(API_TAG).w("400 BadRequest unrecognized error=%s", body?.error)
                            ActivationApiResult.InvalidCodeFormat
                        }
                    }
                }

                HttpStatusCode.TooManyRequests -> {
                    ActivationApiResult.ServerError
                }

                else -> {
                    val bodyText = runCatching { response.bodyAsText() }.getOrDefault("<read failed>")
                    Timber.tag(API_TAG).w("unexpected status=%d body=%s", response.status.value, bodyText)
                    ActivationApiResult.ServerError
                }
            }

        private suspend fun mapStatusResponse(response: HttpResponse): StatusApiResult =
            when (response.status) {
                HttpStatusCode.OK -> {
                    runCatching { response.body<StatusResponseDto>() }
                        .map { dto ->
                            if (dto.status == "revoked") StatusApiResult.Revoked else StatusApiResult.Activated
                        }.getOrElse {
                            Timber.tag(API_TAG).w(it, "status 200 body decode failed")
                            StatusApiResult.ServerError
                        }
                }

                HttpStatusCode.NotFound -> {
                    StatusApiResult.NotFoundOrForeign
                }

                HttpStatusCode.TooManyRequests -> {
                    StatusApiResult.ServerError
                }

                else -> {
                    Timber.tag(API_TAG).w("status unexpected status=%d", response.status.value)
                    StatusApiResult.ServerError
                }
            }

        private suspend fun mapListResponse(response: HttpResponse): ListApiResult =
            when (response.status) {
                HttpStatusCode.OK -> {
                    runCatching { response.body<CouponListResponseDto>() }
                        .map { ListApiResult.Success(it.coupons) as ListApiResult }
                        .getOrElse {
                            Timber.tag(API_TAG).w(it, "by-device 200 body decode failed")
                            ListApiResult.ServerError
                        }
                }

                HttpStatusCode.TooManyRequests -> {
                    ListApiResult.ServerError
                }

                else -> {
                    Timber.tag(API_TAG).w("by-device unexpected status=%d", response.status.value)
                    ListApiResult.ServerError
                }
            }

        private companion object {
            const val API_TAG = "CouponApi"
        }
    }
