package com.invex.app.admitcard

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.R
import com.invex.app.medicalleave.LeaveField
import com.invex.app.medicalleave.ReadingHint
import com.invex.app.medicalleave.VerifyCheckRow
import com.invex.app.sign.SignRequestActions
import com.invex.app.sign.SignRequestViewModel
import com.invex.app.sign.SignTab
import com.invex.app.ui.components.AccentActionButtons
import com.invex.app.ui.components.ServiceHeader
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.TableHeaderText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AdmitCardRoute(
    onBack: () -> Unit,
    viewModel: AdmitCardViewModel = viewModel(),
    signViewModel: SignRequestViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val signState by signViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // The card is a result of signing: back from the card leaves the service, as the arrow does.
    BackHandler(enabled = state.signSheetOpen) { viewModel.onCloseSignSheet() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ServiceHeader(title = "Admit Card", iconRes = R.drawable.ic_admit_card, onBack = onBack)
        when {
            state.step == AdmitStep.Card && state.card != null -> CardStep(
                state = state,
                card = state.card!!,
                onVerify = viewModel::onVerifyCard,
                onDownload = {
                    scope.launch {
                        val where = withContext(Dispatchers.IO) { AdmitCardPdf.save(context, state.card!!) }
                        Toast.makeText(
                            context,
                            if (where != null) "Admit card saved in $where" else "Could not save the admit card",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                },
                modifier = Modifier.weight(1f),
            )
            state.isLoading -> StatusMessage("Loading your subjects...")
            state.error != null -> StatusMessage(state.error!!, actionLabel = "Try again", onAction = viewModel::refresh)
            else -> ReadingStep(
                state = state,
                onVerifiedChange = viewModel::onVerifiedChange,
                onReport = viewModel::onOpenReport,
                onSign = {
                    if (state.verified && state.subjects.isNotEmpty()) {
                        signViewModel.reset()
                        viewModel.onOpenSignSheet()
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (state.signSheetOpen) {
        AdmitSignSheet(
            state = signState,
            isSigning = state.isSigning,
            actions = SignRequestActions(
                onTabChange = signViewModel::onTabChange,
                onStrokeStart = signViewModel::onStrokeStart,
                onStrokeMove = signViewModel::onStrokeMove,
                onBack = signViewModel::onBack,
                onClear = signViewModel::onClear,
                onTypedNameChange = signViewModel::onTypedNameChange,
                onSaveSignature = signViewModel::onSaveSignature,
                onConsentChange = signViewModel::onConsentChange,
                onCancel = viewModel::onCloseSignSheet,
                onSign = {
                    if (signViewModel.submitSignature()) {
                        val s = signViewModel.state.value
                        val png = com.invex.app.sign.SignatureRenderer.toPng(s.tab, s.strokes, s.typedName)
                        if (png != null) viewModel.onSigned(png, if (s.tab == SignTab.DRAW) "draw" else "type")
                    }
                },
            ),
        )
    }

    state.signError?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissSignError,
            text = { Text(message, style = InvexType.Body16) },
            confirmButton = { TextButton(onClick = viewModel::onDismissSignError) { Text("OK", color = Orange) } },
        )
    }

    if (state.reportOpen) {
        AlertDialog(
            onDismissRequest = viewModel::onCloseReport,
            title = { Text("Report a problem", style = InvexType.Body18) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tell the admin what is wrong with your subjects.", style = InvexType.Body16, color = TableHeaderText)
                    OutlinedTextField(
                        value = state.reportText,
                        onValueChange = viewModel::onReportTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                    )
                    state.reportError?.let { Text(it, style = InvexType.Caption12, color = Color(0xFFA11B1B)) }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::onSendReport, enabled = state.reportText.isNotBlank() && !state.isReporting) {
                    Text(if (state.isReporting) "Sending..." else "Send", color = Orange)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::onCloseReport) { Text("Cancel", color = Color.Black) } },
        )
    }

    if (state.reportSent) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissReportSent,
            text = { Text("Your report was sent. The admin will look into it.", style = InvexType.Body16) },
            confirmButton = { TextButton(onClick = viewModel::onDismissReportSent) { Text("OK", color = Orange) } },
        )
    }

    state.verifiedCard?.let { v ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissVerify,
            title = { Text("Card verified", style = InvexType.Body18) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("This card matches the official records.", style = InvexType.Body16, color = TableHeaderText)
                    Text("${v.studentName} (${v.enrollmentNumber})", style = InvexType.Body16)
                    Text("${v.courseCode} - ${v.courseTitle}", style = InvexType.Body16)
                    Text(v.whenText, style = InvexType.Body16)
                    Text("${v.building}, Room ${v.room}", style = InvexType.Body16)
                    Text("Row ${v.seatRow}, Seat ${v.seatCol}", style = InvexType.Body16)
                }
            },
            confirmButton = { TextButton(onClick = viewModel::onDismissVerify) { Text("OK", color = Orange) } },
        )
    }
    state.verifyError?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissVerify,
            text = { Text(message, style = InvexType.Body16) },
            confirmButton = { TextButton(onClick = viewModel::onDismissVerify) { Text("OK", color = Orange) } },
        )
    }
}

@Composable
private fun ReadingStep(
    state: AdmitCardUiState,
    onVerifiedChange: (Boolean) -> Unit,
    onReport: () -> Unit,
    onSign: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReadingHint("Please read and verify all the subjects.")
            if (state.subjects.isEmpty()) {
                StatusMessage("You have no upcoming exams.")
            } else {
                state.subjects.forEachIndexed { index, exam -> LeaveField(subjectLabel(index), exam.courseTitle) }
            }
        }
        VerifyCheckRow(state.verified, onVerifiedChange, "I have read and verified all the details are correct.")
        AccentActionButtons(
            onCancel = onReport,
            onProceed = onSign,
            proceedEnabled = state.verified && state.subjects.isNotEmpty(),
            proceedLabel = "E - Sign",
            cancelLabel = "Report",
        )
    }
}

@Composable
private fun CardStep(
    state: AdmitCardUiState,
    card: AdmitCard,
    onVerify: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Your admit card is ready", style = InvexType.Body16, color = Color.Black)
            Text(
                "Verify the card with official records or download right away.",
                style = InvexType.Caption12,
                color = TableHeaderText,
            )
            AdmitCardView(card, Modifier.padding(top = 8.dp))
        }
        AccentActionButtons(
            onCancel = onVerify,
            onProceed = onDownload,
            proceedEnabled = !state.isVerifying,
            proceedLabel = "Download",
            cancelLabel = if (state.isVerifying) "Verifying..." else "Verify the Card",
        )
    }
}
