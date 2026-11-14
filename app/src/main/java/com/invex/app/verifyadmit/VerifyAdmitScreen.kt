package com.invex.app.verifyadmit

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.TableHeaderText

/** Stateful entry: owns the ViewModel and the camera permission. */
@Composable
fun VerifyAdmitRoute(
    onBack: () -> Unit,
    viewModel: VerifyAdmitViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.CAMERA) }

    VerifyAdmitScreen(
        state = state,
        hasCameraPermission = granted,
        onBack = onBack,
        onTabSelected = viewModel::onTabSelected,
        onQrDetected = viewModel::onQrScanned,
        onRequestPermission = { launcher.launch(Manifest.permission.CAMERA) },
        onScanStudent = viewModel::onScanStudent,
    )
}

@Composable
fun VerifyAdmitScreen(
    state: VerifyAdmitUiState,
    hasCameraPermission: Boolean,
    onBack: () -> Unit,
    onTabSelected: (VerifyTab) -> Unit,
    onQrDetected: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onScanStudent: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 24.dp, bottom = 32.dp),
    ) {
        VerifyAdmitHeader(onBack = onBack, modifier = Modifier.padding(horizontal = 12.dp))
        Spacer(Modifier.height(24.dp))
        VerifyTabs(
            selected = state.tab,
            onSelect = onTabSelected,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(60.dp))
        when (state.tab) {
            VerifyTab.ScanQr -> ScanCard(Modifier.padding(horizontal = 16.dp)) {
                ScanWindow(
                    hasCameraPermission = hasCameraPermission,
                    scanning = state.scanning,
                    onQrDetected = onQrDetected,
                    onRequestPermission = onRequestPermission,
                )
                ScanHint(
                    when {
                        state.lastScan == null ->
                            "Place the QR Code properly inside the area. Scanning will start automatically."
                        state.isVerifying -> "Checking the admit card..."
                        else -> "Tap Scan the Student to scan the next one."
                    },
                )
                state.result?.let { VerifyResultCard(it) }
            }
            // The Students tab is not designed yet.
            VerifyTab.Students -> Text(
                text = "Students list coming soon",
                style = InvexType.Body16,
                color = TableHeaderText,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.weight(1f))
        ScanStudentButton(onClick = onScanStudent, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun VerifyAdmitPreview() {
    InvexTheme {
        VerifyAdmitScreen(
            state = VerifyAdmitUiState(scanning = false),
            hasCameraPermission = false,
            onBack = {},
            onTabSelected = {},
            onQrDetected = {},
            onRequestPermission = {},
            onScanStudent = {},
        )
    }
}
