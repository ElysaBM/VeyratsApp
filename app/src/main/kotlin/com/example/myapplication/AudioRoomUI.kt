package com.example.myapplication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.video.android.compose.ui.components.avatar.UserAvatar
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleMicrophoneAction
import io.getstream.video.android.core.Call
import io.getstream.video.android.core.ParticipantState
import kotlinx.coroutines.launch

@Composable
fun AudioRoomUI(call: Call) {
    val custom by call.state.custom.collectAsState()
    val title = custom["title"] as? String
    val description = custom["description"] as? String
    val participants by call.state.participants.collectAsState()
    val activeSpeakers by call.state.activeSpeakers.collectAsState()
    val activeSpeaker = activeSpeakers.firstOrNull()
    val backstage by call.state.backstage.collectAsState()
    val isMicrophoneEnabled by call.microphone.isEnabled.collectAsState()

    Description(title, description, participants)

    activeSpeaker?.let {
        Text("${it.userNameOrId} is speaking")
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 32.dp)
    ) {
        Participants(
            modifier = Modifier.weight(4f),
            participants = participants
        )
        Controls(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            call = call,
            isMicrophoneEnabled = isMicrophoneEnabled,
            backstage = backstage,
            enableMicrophone = { call.microphone.setEnabled(it) }
        )
    }
}

@Composable
fun Description(
    title: String?,
    description: String?,
    participants: List<ParticipantState>
) {
    Text("$title", fontSize = 30.sp)
    Text("$description", fontSize = 20.sp, modifier = Modifier.padding(16.dp))
    Text("${participants.size} participants", fontSize = 20.sp)
}

@Composable
fun Participants(
    modifier: Modifier = Modifier,
    participants: List<ParticipantState>
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 128.dp),
    ) {
        items(items = participants, key = { it.sessionId }) { participant ->
            ParticipantAvatar(participant)
        }
    }
}

@Composable
fun ParticipantAvatar(
    participant: ParticipantState,
    modifier: Modifier = Modifier
) {
    val image by participant.image.collectAsState()
    val nameOrId by participant.userNameOrId.collectAsState()
    val isSpeaking by participant.speaking.collectAsState()
    val audioEnabled by participant.audioEnabled.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            UserAvatar(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                userImage = image,
                userName = nameOrId,
            )

            if (isSpeaking) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(BorderStroke(2.dp, Color.Gray), CircleShape)
                )
            } else if (!audioEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black)
                            .size(16.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp),
                            painter = painterResource(id = io.getstream.video.android.ui.common.R.drawable.stream_video_ic_mic_off),
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = nameOrId,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = participant.roles.value.firstOrNull() ?: "",
            fontSize = 11.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun Controls(
    modifier: Modifier = Modifier,
    call: Call,
    backstage: Boolean = false,
    isMicrophoneEnabled: Boolean = false,
    enableMicrophone: (Boolean) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ToggleMicrophoneAction(
            modifier = Modifier.size(52.dp),
            isMicrophoneEnabled = isMicrophoneEnabled,
            onCallAction = { enableMicrophone(it.isEnabled) }
        )

        Button(
            onClick = {
                scope.launch {
                    if (backstage) call.goLive() else call.stopLive()
                }
            }
        ) {
            Text(text = if (backstage) "Go Live" else "End")
        }
    }
}
