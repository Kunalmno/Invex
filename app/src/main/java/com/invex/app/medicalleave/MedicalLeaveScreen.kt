package com.invex.app.medicalleave

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.R
import com.invex.app.ui.components.AccentActionButtons
import com.invex.app.ui.components.ServiceHeader
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.Orange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ALLOWED_TYPES = arrayOf(
    "image/jpeg",
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "text/plain",
)

/** Allowed by file extension -> mime type the storage bucket accepts. */
private val EXTENSION_MIME = mapOf(
    "jpg" to "image/jpeg", "jpeg" to "image/jpeg", "pdf" to "application/pdf",
    "doc" to "application/msword",
    "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "txt" to "text/plain",
)

private sealed interface PickOutcome {
    class Ok(val document: PickedDocument) : PickOutcome
    class Rejected(val message: String) : PickOutcome
}

/** Reads the chosen file into memory after checking its type and size (under 10 MB). */
private suspend fun readDocument(context: Context, uri: Uri): PickOutcome = withContext(Dispatchers.IO) {
    try {
        var name = "document"
        var size = -1L
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = c.getString(it) ?: name }
                c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = c.getLong(it) }
            }
        }
        val extension = name.substringAfterLast('.', "").lowercase()
        val mime = EXTENSION_MIME[extension]
            ?: context.contentResolver.getType(uri)?.takeIf { it in ALLOWED_TYPES }
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.takeIf { it in ALLOWED_TYPES }
            ?: return@withContext PickOutcome.Rejected("This file type is not allowed. Use JPG, PDF, DOC or TXT.")
        if (size > MAX_DOCUMENT_BYTES) return@withContext PickOutcome.Rejected("This file is larger than 10 Mb.")
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@withContext PickOutcome.Rejected("Could not read this file.")
        if (bytes.size > MAX_DOCUMENT_BYTES) return@withContext PickOutcome.Rejected("This file is larger than 10 Mb.")
        PickOutcome.Ok(PickedDocument(name, mime, bytes))
    } catch (e: Exception) {
        PickOutcome.Rejected("Could not read this file.")
    }
}

@Composable
fun MedicalLeaveRoute(
    onBack: () -> Unit,
    viewModel: MedicalLeaveViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            when (val outcome = readDocument(context, uri)) {
                is PickOutcome.Ok -> viewModel.onDocumentPicked(outcome.document)
                is PickOutcome.Rejected -> viewModel.onDocumentRejected(outcome.message)
            }
        }
    }

    // The back arrow of the form goes back to the reading screen; everywhere else it leaves the service.
    BackHandler(enabled = state.step == LeaveStep.Form) { viewModel.onBackToReading() }

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
        ServiceHeader(
            title = "Medical Leave",
            iconRes = R.drawable.ic_leave,
            onBack = { if (state.step == LeaveStep.Form) viewModel.onBackToReading() else onBack() },
        )
        when {
            state.isLoading -> StatusMessage("Loading your details...")
            state.error != null -> StatusMessage(state.error!!, actionLabel = "Try again", onAction = viewModel::refresh)
            state.step == LeaveStep.Form -> LeaveForm(
                state = state,
                onConditionMenu = viewModel::onConditionMenu,
                onConditionSelected = viewModel::onConditionSelected,
                onDetailsChange = viewModel::onDetailsChange,
                onChooseFile = { picker.launch(ALLOWED_TYPES) },
                onRemoveFile = viewModel::onRemoveDocument,
                onCancel = onBack,
                onSubmit = viewModel::onSubmit,
                modifier = Modifier.weight(1f),
            )
            else -> LeaveReading(
                state = state,
                onVerifiedChange = viewModel::onVerifiedChange,
                onCancel = onBack,
                onProceed = viewModel::onProceedFromReading,
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (state.step == LeaveStep.Sent) {
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Request sent", style = InvexType.Body18) },
            text = { Text("Your medical leave request was sent. The admin will review it.", style = InvexType.Body16) },
            confirmButton = { TextButton(onClick = onBack) { Text("OK", color = Orange) } },
        )
    }
}

@Composable
private fun LeaveReading(
    state: MedicalLeaveUiState,
    onVerifiedChange: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val student = state.student ?: return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReadingHint("Please read and verify the details")
            LeaveField("Application Date", state.applicationDate)
            LeaveField("Student Name", student.name)
            LeaveField("Enrollment Number", student.enrollmentNumber)
            LeaveField("Course", student.course)
            LeaveField("Specialization", student.specialization)
            LeaveField("Branch", student.branch)
            LeaveField("Semester", semesterOrdinal(student.semester))
            LeaveField("Mobile Number", student.mobile)
            LeaveField("Email", student.email)
        }
        VerifyCheckRow(state.verified, onVerifiedChange, "I have read and verified all the details are correct.")
        AccentActionButtons(onCancel = onCancel, onProceed = onProceed, proceedEnabled = state.verified)
    }
}

@Composable
private fun LeaveForm(
    state: MedicalLeaveUiState,
    onConditionMenu: (Boolean) -> Unit,
    onConditionSelected: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onChooseFile: () -> Unit,
    onRemoveFile: () -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FormLabel("Medical Condition")
            ConditionDropdown(state.condition, MEDICAL_CONDITIONS, state.conditionMenuOpen, onConditionMenu, onConditionSelected)
            FormLabel("Medical Details", Modifier.padding(top = 8.dp))
            DetailsBox(state.details, onDetailsChange)
            FormLabel("Upload Document", Modifier.padding(top = 8.dp))
            DocumentPicker(state.document?.name, state.documentError, onChooseFile, onRemoveFile)
        }
        state.submitError?.let { SubmitErrorText(it) }
        AccentActionButtons(
            onCancel = onCancel,
            onProceed = onSubmit,
            proceedEnabled = state.details.isNotBlank() && !state.isSubmitting,
            proceedLabel = if (state.isSubmitting) "SENDING..." else "PROCEED",
        )
    }
}
