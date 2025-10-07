package com.vigram.sdkgh1.compose.scripts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vigram.sdkgh1.compose.event.ConfigurationEvent
import com.vigram.sdkgh1.ui.scripts.CenteredDialog

@Composable
@Preview
fun ConfigurationCard(
    configurationStatus: ConfigurationEvent = ConfigurationEvent.Done,
    onClickDoneListener: () -> Unit = {}
) {

    CenteredDialog {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configuration...",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                val state = when (configurationStatus) {
                    ConfigurationEvent.Done -> "done"
                    is ConfigurationEvent.Error -> configurationStatus.message
                    is ConfigurationEvent.InProgress -> configurationStatus.message
                }

                Text(
                    text = state,
                    color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}