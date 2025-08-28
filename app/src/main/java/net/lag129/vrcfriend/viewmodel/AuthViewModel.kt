package net.lag129.vrcfriend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import io.github.vrchatapi.ApiClient
import io.github.vrchatapi.Configuration
import io.github.vrchatapi.JSON
import io.github.vrchatapi.api.AuthenticationApi
import io.github.vrchatapi.api.FriendsApi
import io.github.vrchatapi.auth.HttpBasicAuth
import io.github.vrchatapi.model.LimitedUserFriend
import io.github.vrchatapi.model.TwoFactorAuthCode
import io.github.vrchatapi.model.TwoFactorEmailCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lag129.vrcfriend.AuthState
import net.lag129.vrcfriend.CustomTypeAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _friendsList = MutableStateFlow<List<LimitedUserFriend>>(emptyList())
    val friendsList: StateFlow<List<LimitedUserFriend>> = _friendsList.asStateFlow()

    private val _friendsLoading = MutableStateFlow(false)
    val friendsLoading: StateFlow<Boolean> = _friendsLoading.asStateFlow()

    private lateinit var authHeader: HttpBasicAuth

    private var authApi: AuthenticationApi
    private var friendsApi: FriendsApi

    init {
        val defaultClient: ApiClient? = Configuration.getDefaultApiClient()

        authApi = AuthenticationApi(defaultClient)
        friendsApi = FriendsApi(defaultClient)

        if (defaultClient != null) {
            authHeader = defaultClient.getAuthentication("authHeader") as HttpBasicAuth
            authHeader.username = ""
            authHeader.password = ""
            defaultClient.setUserAgent("VRCFriend/0.0.1 ${authHeader.username}")

            val originalGson = JSON.getGson()
            val customGson = originalGson.newBuilder()
                .registerTypeAdapterFactory(CustomTypeAdapterFactory())
                .create()
            JSON.setGson(customGson)
        }
    }

    fun setupImageLoaderWithAuth(context: PlatformContext) {
        val defaultClient: ApiClient? = Configuration.getDefaultApiClient()

        if (defaultClient == null) {
            return
        }

        // VRChat APIクライアントのCookieJarを取得
        val vrchatCookieJar = defaultClient.httpClient.cookieJar

        // VRChat APIドメイン用のUser-Agent追加インターセプター  
        val userAgentInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            val newRequest = if (originalRequest.url.host.contains("vrchat.cloud")) {
                originalRequest.newBuilder()
                    .addHeader("User-Agent", "VRCFriend/0.0.1 ${authHeader.username}")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(newRequest)
        }

        // VRChat APIクライアントと同じCookieJarを使用したOkHttpクライアント
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(vrchatCookieJar)
            .addInterceptor(userAgentInterceptor)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        // カスタムImageLoaderを設定
        val imageLoader = ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory({ okHttpClient }))
            }
            .build()

        SingletonImageLoader.setSafe { imageLoader }
    }

    fun login(username: String, password: String) {
        _authState.value = AuthState.Loading
        authHeader.username = username
        authHeader.password = password

        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    authApi.currentUser
                }
                _authState.value = AuthState.Success(user)
                loadFriends()
            } catch (e: Exception) {
                when {
                    e.toString().contains("emailOtp") -> {
                        _authState.value = AuthState.RequiresEmailOtp
                    }

                    e.toString().contains("requiresTwoFactorAuth") -> {
                        _authState.value = AuthState.RequiresTwoFactorAuth
                    }

                    else -> {
                        _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
                    }
                }
            }
        }
    }

    fun verifyEmailOtp(code: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val otpCode = TwoFactorEmailCode().code(code)
                withContext(Dispatchers.IO) {
                    authApi.verify2FAEmailCode(otpCode)
                }
                val user = withContext(Dispatchers.IO) {
                    authApi.currentUser
                }
                _authState.value = AuthState.Success(user)
                loadFriends()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Email OTP verification failed")
            }
        }
    }

    fun verifyTwoFactorAuth(code: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val authCode = TwoFactorAuthCode().code(code)
                withContext(Dispatchers.IO) {
                    authApi.verify2FA(authCode)
                }
                val user = withContext(Dispatchers.IO) {
                    authApi.currentUser
                }
                _authState.value = AuthState.Success(user)
                loadFriends()
            } catch (e: Exception) {
                _authState.value =
                    AuthState.Error(e.message ?: "Two factor auth verification failed")
            }
        }
    }

    fun loadFriends() {
        _friendsLoading.value = true
        viewModelScope.launch {
            try {
                val friends = withContext(Dispatchers.IO) {
                    friendsApi.getFriends(0, 100, null)
                }
                _friendsList.value = friends
            } catch (e: Exception) {
                _friendsList.value = emptyList()
            } finally {
                _friendsLoading.value = false
            }
        }
    }

    fun resetToIdle() {
        _authState.value = AuthState.Idle
        _friendsList.value = emptyList()
    }
}