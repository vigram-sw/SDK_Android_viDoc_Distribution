package com.vigram.sdkgh1.compose.scripts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vigram.sdkgh1.compose.event.AuthenticationEvent
import com.vigram.sdkgh1.ui.scripts.CenteredDialog


@Composable
@Preview(backgroundColor = 0xFFFFFFFF, apiLevel = 30, showSystemUi = false, showBackground = false,
    wallpaper = androidx.compose.ui.tooling.preview.Wallpapers.NONE
)
fun AuthenticationCard(authenticationState: AuthenticationEvent = AuthenticationEvent.Start){

    val message = when(authenticationState){
        is AuthenticationEvent.Error -> authenticationState.message
        AuthenticationEvent.Start -> "start"
        AuthenticationEvent.Success -> "success"
    }

    CenteredDialog {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentHeight()
                .wrapContentWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Authentication is $message",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 18.sp
                )
            }
        }
    }
}