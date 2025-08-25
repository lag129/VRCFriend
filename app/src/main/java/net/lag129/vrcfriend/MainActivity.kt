package net.lag129.vrcfriend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.vrchatapi.ApiClient
import io.github.vrchatapi.Configuration
import io.github.vrchatapi.api.AuthenticationApi
import io.github.vrchatapi.auth.HttpBasicAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lag129.vrcfriend.ui.theme.VRCFriendTheme


class MainActivity : ComponentActivity() {
    lateinit var authHeader: HttpBasicAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultClient: ApiClient? = Configuration.getDefaultApiClient()
        val authApi = AuthenticationApi(defaultClient)

        if (defaultClient != null) {
            authHeader = defaultClient.getAuthentication("authHeader") as HttpBasicAuth
            authHeader.username = ""
            authHeader.password = ""
            defaultClient.setUserAgent("VRCFriend/0.0.1 ${authHeader.username}")
        }

        enableEdgeToEdge()
        setContent {
            VRCFriendTheme {
                val coroutineScope = rememberCoroutineScope()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Button(
                        modifier = Modifier.padding(innerPadding),
                        onClick = { coroutineScope.launch { getCurrentUser(authApi) } }
                    ) {
                        Text("Get Current User")
                    }
                }
            }
        }
    }
}

suspend fun getCurrentUser(authApi: AuthenticationApi) {
    try {
        val user = withContext(Dispatchers.IO) {
            authApi.currentUser
        }
        println("ログインしました: " + user.displayName)
    } catch (e: Exception) {
        when {
            e.toString().contains("emailOtp") -> {
                // val code = TwoFactorEmailCode().code("認証コード")
                // withContext(Dispatchers.IO) {
                //     authApi.verify2FAEmailCode(code)
                // }
            }

            e.toString().contains("requiresTwoFactorAuth") -> {
                // val code = TwoFactorAuthCode().code("認証コード")
                // withContext(Dispatchers.IO) {
                //     authApi.verify2FA(code)
                // }
            }

            else -> {
                println(e.message)
                println(e.javaClass.simpleName)
                e.printStackTrace()
            }
        }
    }
}
