package net.lag129.vrcfriend.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.vrchatapi.model.CurrentUser

@Composable
fun AuthSuccessScreen(
    user: CurrentUser,
    onLogout: () -> Unit
) {
    LazyColumn {
        items(user.friends) { friend ->
            Text(text = friend)
        }
    }
}