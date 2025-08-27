package net.lag129.vrcfriend.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.vrchatapi.model.LimitedUserFriend
import net.lag129.vrcfriend.viewmodel.AuthViewModel

@Composable
fun AuthSuccessScreen(
    authViewModel: AuthViewModel,
) {
    val friendsList by authViewModel.friendsList.collectAsState()
    val friendsLoading by authViewModel.friendsLoading.collectAsState()

    if (friendsLoading) {
        CircularProgressIndicator()
    } else {
        FriendsList(friendsList)
    }
}

@Composable
fun FriendsList(friendsList: List<LimitedUserFriend>) {
    LazyColumn {
        items(friendsList) { friend ->
            Text(text = friend.displayName)
        }
    }
}