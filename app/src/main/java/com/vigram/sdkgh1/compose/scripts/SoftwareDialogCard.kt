package com.vigram.sdkgh1.ui.scripts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.PlatformLocale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vigram.sdkgh1.compose.event.SoftwareUpdateEvent

@Preview
@Composable
fun SoftwareDialogCard(
    state: SoftwareUpdateEvent = SoftwareUpdateEvent.End
) {
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
                Text(text = "Software update", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                when (state) {
                    SoftwareUpdateEvent.End ->
                        Text(text = "Software update complete")

                    is SoftwareUpdateEvent.Error ->
                        Text(text = "Software update error")

                    is SoftwareUpdateEvent.Updating ->
                        CustomProgressBar(state.progress)

                    SoftwareUpdateEvent.None -> {
                        Text(text = "None")
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomProgressBar(progress: Double) {
    val barHeight = 20.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(Color.LightGray.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.toFloat() / 100.0f)
                .height(barHeight)
                .background(Color.LightGray)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(String.format(locale = PlatformLocale.getDefault(), "%.3f%%", progress))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenteredDialog(
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = {}
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.medium,
            color = Color.White
        ) {
            content()
        }
    }
}