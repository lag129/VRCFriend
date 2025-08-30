package net.lag129.vrcfriend.ui.screens

import android.annotation.SuppressLint
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val onlineFriendsList by authViewModel.onlineFriendsList.collectAsState()
    val offlineFriendsList by authViewModel.offlineFriendsList.collectAsState()
    val friendsLoading by authViewModel.friendsLoading.collectAsState()

    // 認証付きImageLoaderを初期化
    LaunchedEffect(Unit) {
        authViewModel.setupImageLoaderWithAuth(context)
    }

    if (friendsLoading) {
        CircularProgressIndicator()
    } else {
        Column(
            modifier = modifier
                .padding(16.dp)
                .padding(top = 32.dp)
        ) {
            Text("オンライン", fontSize = 24.sp)
            FriendsList(onlineFriendsList)
            Spacer(modifier = Modifier.height(32.dp))
            Text("オフライン", fontSize = 24.sp)
            FriendsList(offlineFriendsList)
        }
    }
}

@Composable
fun FriendsList(
    @SuppressLint("ComposeUnstableCollections") friendsList: List<LimitedUserFriend>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center
    ) {
        LazyColumn {
            items(friendsList) { friend ->
                HorizontalDivider(
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color,
                    modifier = Modifier.height(1.dp)
                )
                FriendCard(friend)
            }
        }

        HorizontalDivider(
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color,
            modifier = Modifier.height(1.dp)
        )
    }
}

@Composable
fun FriendCard(
    friend: LimitedUserFriend,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = friend.imageUrl,
                contentDescription = friend.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .clip(CircleShape)
                    .height(64.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(friend.displayName, fontWeight = FontWeight.SemiBold)
                Text(setStatus(friend.status.value), fontWeight = FontWeight.Thin)
                Text(setStatusDescription(friend), fontWeight = FontWeight.Thin)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
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
private fun PreviewFriendCard() {
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