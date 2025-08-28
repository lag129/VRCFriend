package net.lag129.vrcfriend.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vrchatapi.model.LimitedUserFriend
import net.lag129.vrcfriend.ui.theme.VRCFriendTheme
import net.lag129.vrcfriend.viewmodel.AuthViewModel
import java.time.OffsetDateTime
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
            FriendCard(friend)
        }
    }
}

@Composable
fun FriendCard(friend: LimitedUserFriend) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = friend.imageUrl,
            contentDescription = friend.displayName,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column() {
            Text(friend.displayName)
            Text(setStatus(friend.status.value))
            Text(setStatusDescription(friend))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

private fun setStatus(status: String): String {
    when (status) {
        "offline" -> {
            return "⚪ Offline"
        }

        "ask me" -> {
            return "🟠 Ask Me"
        }

        "join me" -> {
            return "🔵 Join Me"
        }

        else -> {
            return "🟢 Online"
        }
    }
}

private fun setStatusDescription(friend: LimitedUserFriend): String {
    if (friend.status.value == "offline") {
        return calculateTimeAgo(friend.lastActivity.toString())
    }

    if (friend.statusDescription != "") {
        return friend.statusDescription
    }

    return friend.status.value
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

@Preview(showBackground = true)
@Composable
fun PreviewFriendCard() {
    VRCFriendTheme {
        FriendCard(
            friend = LimitedUserFriend().apply {
                displayName = "あいうabc"
                imageUrl = ""
                lastActivity = OffsetDateTime.now()
                location = "offline"
            }
        )
    }
}