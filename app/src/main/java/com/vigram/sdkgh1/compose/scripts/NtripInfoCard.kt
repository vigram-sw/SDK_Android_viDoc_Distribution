package com.vigram.sdkgh1.ui.scripts

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vigram.sdkgh1.MainViewModel
import com.vigram.sdkgh1.R
import com.vigram.sdkgh1.data.mapper.toNtripConnectionInformationSDK
import com.vigram.sdkgh1.domain.entity.NtripConnectionData
import com.vigram.sdk.Models.NtripState
import com.vigram.sdkgh1.ui.scripts.CustomTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.EmptyCoroutineContext.get

@Composable
fun NtripInfoCard(
    viewModel: MainViewModel,
) {
    var contentVisible by rememberSaveable { mutableStateOf(false) }
    val ntripConnectionData = remember { mutableStateOf(NtripConnectionData.empty()) }

    val list by viewModel.mountPoints.collectAsState()

    Column(
        modifier = Modifier
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(onClick = { contentVisible = !contentVisible }) {
            Row {
                Image(
                    painter = painterResource(if (contentVisible) R.drawable.expand_less else R.drawable.expand_more),
                    contentDescription = "Expand more"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (contentVisible) "Hide NTRIP control" else "Show NTRIP control")
            }
        }

        AnimatedVisibility(contentVisible) {
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NtripList(viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                NtripAccount(viewModel)
                Spacer(modifier = Modifier.height(16.dp))
                ElevatedButton(onClick = {
                    viewModel.fetchMounts()
                }) {
                    Text("Get Mounts")
                }
                NtripControl(viewModel)
            }
        }
    }
}

@Composable
private fun NtripList(viewModel: MainViewModel) {
    Column(
        modifier = Modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("List NTRIP info")
        val list = viewModel.listNtripInfo.collectAsState().value
        list.forEach {
            NtripListItem(it, viewModel)
        }
    }
}


@Composable
private fun NtripListItem(ntripConnectionData: NtripConnectionData, viewModel: MainViewModel) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                //onClickDeleteListener(ntripInfo)
            }
            it == SwipeToDismissBoxValue.EndToStart
        },
        positionalThreshold = { it * .70f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Red.copy(alpha = 0.8f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_svg),
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    viewModel.updateNtripInfo(ntripConnectionData)
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = ntripConnectionData.login, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = ntripConnectionData.password,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = ntripConnectionData.host,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(text = ntripConnectionData.port, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun NtripAccount(viewModel: MainViewModel) {

    val ntripInfo by viewModel.ntripInfo.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var mountPoints = viewModel.mountPoints.collectAsState().value.map { it.name }
    val isLoading by viewModel.loadingMounts.collectAsState()

    InfoEditText(
        title = "Host",
        value = ntripInfo.host,
        placeholder = "Enter host"
    ) { updatedValue ->
        viewModel.updateHost(updatedValue)
    }
    Spacer(modifier = Modifier.height(6.dp))
    InfoEditText(
        title = "Port",
        value = ntripInfo.port,
        placeholder = "Enter port"
    ) { updatedValue ->
        viewModel.updatePort(updatedValue)
    }
    Spacer(modifier = Modifier.height(6.dp))
    InfoEditText(
        title = "Login",
        value = ntripInfo.login,
        placeholder = "Enter login"
    ) { updatedValue ->
        viewModel.updateLogin(updatedValue)
    }
    Spacer(modifier = Modifier.height(6.dp))
    InfoEditText(
        title = "Password",
        value = ntripInfo.password,
        placeholder = "Enter password"
    ) { updatedValue ->
        viewModel.updatePassword(updatedValue)
    }
    Spacer(modifier = Modifier.height(6.dp))
    InfoEditText(
        title = "Mount",
        value = ntripInfo.mount,
        placeholder = "Enter mount"
    ) { updatedValue ->
        viewModel.updateMount(updatedValue)
    }
    Spacer(modifier = Modifier.height(6.dp))

    Button(onClick = {
        viewModel.fetchMounts()
        showDialog = true
    }) {
        Text("Mount points")
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select a point") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn {
                            items(mountPoints) { point ->
                                Text(
                                    text = point,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateMount(point)
                                            mountPoints = emptyList()
                                            showDialog = false
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    mountPoints = emptyList()
                    viewModel.fetchMountsCancel()
                }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun NtripControl(viewModel: MainViewModel) {
    var txt by remember { mutableStateOf("") }
    val ntripState = viewModel.ntripState.collectAsState()
    val isConnected = ntripState.value == NtripState.Connected
    val connectButtonText = if (isConnected) "Disconnect" else "Connect"

    val size = viewModel.ntripData.collectAsState().value.size
    val scrollState = rememberScrollState()

    LaunchedEffect(size) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        txt += "$time: $size byte\n"
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Text("Status")
    Text(ntripState.value::class.simpleName.toString())
    Spacer(modifier = Modifier.height(6.dp))

    OutlinedButton(onClick = {
        if (isConnected) {
            viewModel.ntripDisconnect()
        } else {
            viewModel.ntripConnect()
        }
    }) {
        Text(connectButtonText)
    }

    if (isConnected) {
        Text("NTRIP received data info")
        OutlinedTextField(
            value = txt,
            onValueChange = {},
            textStyle = TextStyle(fontSize = 12.sp),
            readOnly = true,
            modifier = Modifier
                .height(300.dp)
                .verticalScroll(scrollState)
        )
    }
}

@Composable
private fun InfoEditText(
    title: String,
    value: String,
    placeholder: String,
    onTextChange: (String) -> Unit,
) {
    Row(modifier = Modifier.wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$title:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f))
        Spacer(modifier = Modifier.width(4.dp))
        CustomTextField(
            modifier = Modifier.weight(1f),
            value = value,
            placeholder = placeholder,
            onValueChange = onTextChange
        )
    }
}

//modifier = Modifier.weight(1f), value = value, onValueChange = onTextChange
@Composable
private fun CustomTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
//            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            .padding(4.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            }
        }
    )
}