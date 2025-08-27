package net.lag129.vrcfriend.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vrchatapi.model.LimitedUserFriend
import net.lag129.vrcfriend.viewmodel.AuthViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        items(friendsList) { friend ->
            Column() {
                Text(friend.displayName)
                Text(friend.location)
                Text(calculateTimeAgo(friend.lastActivity.toString()))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun calculateTimeAgo(createdTimeString: String): String {
    val createdTimeMillis = Instant.parse(createdTimeString).toEpochMilliseconds()
    val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
    return DateUtils.getRelativeTimeSpanString(
        createdTimeMillis,
        currentTimeMillis,
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
