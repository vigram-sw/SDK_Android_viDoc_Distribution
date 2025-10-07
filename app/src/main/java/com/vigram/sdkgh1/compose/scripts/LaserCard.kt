package com.vigram.sdkgh1.compose.scripts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vigram.sdk.Models.Satellite.DynamicStateType
import com.vigram.sdkgh1.R
import com.vigram.sdkgh1.compose.event.LaserUiEvent
import com.vigram.sdkgh1.domain.entity.MainLaserPosition

@Preview
@Composable
fun LaserCard(
    modifier: Modifier = Modifier,
    laserUiEvent: (LaserUiEvent) -> Unit = {},
) {
    val activeButton: MutableState<MainLaserPosition> =
        remember { mutableStateOf(MainLaserPosition.Bottom) }

    var contentVisible by rememberSaveable { mutableStateOf(true) }
    var duration by rememberSaveable { mutableIntStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(onClick = { contentVisible = !contentVisible }) {
            Row {
                Image(
                    painter = painterResource(if (contentVisible) R.drawable.expand_less else R.drawable.expand_more),
                    contentDescription = "Expand more"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (contentVisible) "Hide Laser control" else "Show Laser control")
            }
        }
    }

    AnimatedVisibility(contentVisible) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InfoEditText(title = "Duration (second):", value = duration.toString(), onTextChange = {
                duration = it.toIntOrNull() ?: 0
            })
            LaserPositionSelected(activeButton)
            ElevatedButton(onClick = {
                laserUiEvent(LaserUiEvent.LaserOn(activeButton.value))
            }) { Text(text = "LaserOn", color = MaterialTheme.colorScheme.onSecondary) }
            ElevatedButton(onClick = {
                laserUiEvent(LaserUiEvent.LaserOff(activeButton.value))
            }) { Text(text = "LaserOff", color = MaterialTheme.colorScheme.onSecondary) }
        }
    }
}

@Preview
@Composable
private fun LaserPositionSelected(
    active: MutableState<MainLaserPosition> = mutableStateOf(MainLaserPosition.Bottom)
) {
    val list = MainLaserPosition.entries

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        list.forEach { position ->
            ElevatedButton(
                onClick = {
                    active.value = position
                },
                modifier = Modifier,
                colors = if (active.value == position)
                    ButtonDefaults.elevatedButtonColors().copy(
                        containerColor = ButtonDefaults.elevatedButtonColors().contentColor,
                        contentColor = ButtonDefaults.elevatedButtonColors().containerColor
                    )
                else
                    ButtonDefaults.elevatedButtonColors()
            ) {
                Text(text = position.name, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}

@Composable
private fun InfoEditText(title: String, value: String, onTextChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        CustomTextField(value, onTextChange)
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
            .width(60.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        value = value,
        singleLine = true,
        onValueChange = onValueChange,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                innerTextField()
            }
        }
    )
}