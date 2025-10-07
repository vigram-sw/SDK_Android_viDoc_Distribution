package com.vigram.sdkgh1.ui.scripts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vigram.sdkgh1.domain.entity.GgaData
import com.vigram.sdkgh1.domain.entity.GstData
import com.vigram.sdkgh1.ui.theme.BlueTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

@Preview
@Composable
fun DarkTheme() {
    BlueTheme(true) {
        NmeaCard()
    }
}

@Preview
@Composable
fun LightTheme() {
    BlueTheme(false) {
        NmeaCard()
    }
}


@Composable
fun NmeaCard(
    gga: State<GgaData?> = remember { mutableStateOf(GgaData()) },
    gst: State<GstData?> = remember { mutableStateOf(GstData()) },
    txt: State<String?> = remember { mutableStateOf("") },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        GgaInfo(ggaData = gga.value ?: GgaData())
        Spacer(modifier = Modifier.height(8.dp))
        GstInfo(gstData = gst.value ?: GstData())
        Spacer(modifier = Modifier.height(8.dp))
        TxtInfo(txt = txt.value)
    }
}

@Composable
private fun GgaInfo(
    modifier: Modifier = Modifier,
    ggaData: GgaData = GgaData()
) {
    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GNGGA information",
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        TimeInfo()
        Spacer(modifier = Modifier.height(6.dp))
        Info("GNSS Time", ggaData.time)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Latitude", ggaData.latitude)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Longitude", ggaData.longitude)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Quality", ggaData.quality)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Reference Altitude", ggaData.referenceAltitude)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Geoid Separation", ggaData.geoidSeparation)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Correction Age", ggaData.correctionAge)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Correction Station ID", ggaData.correctionStationID)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Satellite Count", ggaData.satelliteCount)
        Spacer(modifier = Modifier.height(6.dp))
        Info("HDOP", ggaData.hdop)
    }
}


@Composable
private fun GstInfo(
    modifier: Modifier = Modifier,
    gstData: GstData = GstData()
) {
    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "GNGST information",
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Info("RMS", gstData.rms)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Semi Major Sigma Error", gstData.semiMajor1SigmaError)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Semi Minor Sigma Error", gstData.semiMinor1SigmaError)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Error Ellipse Orientation", gstData.errorEllipseOrientation)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Latitude Error", gstData.latitudeError)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Longitude Error", gstData.longitudeError)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Altitude Error", gstData.altitudeError)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Accuracy Horizontal", gstData.accuracyHorizontal)
        Spacer(modifier = Modifier.height(6.dp))
        Info("Accuracy Vertical", gstData.accuracyVertical)
    }
}

@Composable
private fun TxtInfo(
    modifier: Modifier = Modifier,
    txt: String?
) {
    var text by remember { mutableStateOf(txt) }
    text += txt

    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TXT information",
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Info("TXT", txt ?: "")
    }
}


@Composable
private fun Info(
    title: String,
    info: String
) {
    Row {
        Text(
            text = "$title:",
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = info,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}


@Composable
private fun TimeInfo() {
    val unixTimeDateFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    var unixTime by remember { mutableStateOf(unixTimeDateFormatter.format(System.currentTimeMillis())) }
    var currentTime by remember {
        mutableStateOf(
            LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100L) // Обновляем каждые 100 мс
            unixTime = unixTimeDateFormatter.format(System.currentTimeMillis()) // Точное UNIX-время
            currentTime = LocalTime.now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_TIME) // Локальное время
        }
    }

    Column {
        Info(title = "Current time", info = currentTime)
        Info(title = "UTC time", info = unixTime)
    }
}