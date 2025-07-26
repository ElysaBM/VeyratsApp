package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.android.video.generated.models.MemberRequest
import io.getstream.video.android.compose.permission.LaunchMicrophonePermissions
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.avatar.UserAvatar
import io.getstream.video.android.compose.ui.components.call.controls.actions.ToggleMicrophoneAction
import io.getstream.video.android.core.*
import io.getstream.video.android.model.User
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource

class AudioRoom : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiKey = "4k4c5d4akbnz"
        val userToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMTIzNCJ9.q400M48oCUZJUqR1S9-xoFTsDPN0bj7rL3qOugTiv-8"
        val userId = "1234"
        val callId = "room1234"

        val user = User(
            id = userId,
            name = "Tutorial"
        )

        val client = StreamVideoBuilder(
            context = applicationContext,
            apiKey = apiKey,
            geo = GEO.GlobalEdgeNetwork,
            user = user,
            token = userToken,
        ).build()

        val call = client.call("audio_room", callId)

        setContent {
            val coroutineScope = rememberCoroutineScope()

            // Ask microphone permissions, then join call and enable mic
            LaunchMicrophonePermissions(
                call = call,
                onPermissionsResult = { granted ->
                    if (granted) {
                        coroutineScope.launch {
                            // ✅ Enable microphone BEFORE joining
                            call.microphone.setEnabled(true)

                            val result = call.join(
                                create = true,
                                createOptions = CreateCallOptions(
                                    members = listOf(
                                        MemberRequest(userId = userId, role = "host", custom = emptyMap()),
                                        MemberRequest(userId="fin",role = "host",custom = emptyMap())
                                    ),
                                    custom = mapOf(
                                        "title" to "Compose Trends",
                                        "description" to "Talk about how easy compose makes it to reuse and combine UI"
                                    )
                                )
                            )
                            result.onError {
                                Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )

            VideoTheme {
                val connection by call.state.connection.collectAsState()
                val activeSpeakers by call.state.activeSpeakers.collectAsState()
                val audioLevel = activeSpeakers.firstOrNull()?.audioLevel?.collectAsState()

                val color1 = Color.White.copy(alpha = 0.2f + (audioLevel?.value ?: 0f) * 0.8f)
                val color2 = Color.White.copy(alpha = 0.2f + (audioLevel?.value ?: 0f) * 0.8f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(color1, color2)))
                        .fillMaxSize()
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {

                    if (connection != RealtimeConnection.Connected) {
                        Text("Loading", fontSize = 30.sp)
                    } else {
                        AudioRoom(call = call)
                    }
                }
            }
        }
    }

    @Composable
    fun AudioRoom(call: Call) {
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
                .padding(0.dp, 32.dp, 0.dp, 0.dp)
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
                        .padding(VideoTheme.dimens.componentPaddingFixed),
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
}
