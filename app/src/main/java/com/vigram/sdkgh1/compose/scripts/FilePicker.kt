package com.vigram.sdkgh1.ui.scripts

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vigram.sdkgh1.R
import com.vigram.sdkgh1.helpers.getFileExtension

@Preview
@Composable
fun FilePickerButton(
    softwareFile: State<ByteArray> = mutableStateOf(byteArrayOf()),
    onFileListener: (ByteArray)->Unit = {}
) {
    val context = LocalContext.current
    var selectedFileUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var name by rememberSaveable { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedFileUri = result.data?.data

            selectedFileUri?.let {
                name = getFileExtension(it, context) ?: "null"
                if (name.split(".").last().contains("hex")) {
                    val file = readBytesFromUri(context, selectedFileUri!!) ?: byteArrayOf()
                    onFileListener(file)

                    Toast.makeText(
                        context,
                        "Device will be updated on next start!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    onFileListener(byteArrayOf())
                    name = "$name\nThe file is not supported!"
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                }
                launcher.launch(intent)
            }) {
            Text("Select firmware file")
        }

        if (name != "") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    name,
                    textAlign = TextAlign.Center,
                    color = if (softwareFile.value.isEmpty()) Color.Red else Color.Green
                )

                IconButton(onClick = {
                    onFileListener(byteArrayOf())
                    name = ""
                }) {
                    Image(
                        modifier = Modifier.size(20.dp, 20.dp),
                        painter = painterResource(id = R.drawable.delete_svg), // Use your actual drawable ID
                        contentDescription = "clear data",
                        colorFilter = ColorFilter.tint(Color.Red)
                    )
                }
            }
        }
    }
}

private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        null
    }
}