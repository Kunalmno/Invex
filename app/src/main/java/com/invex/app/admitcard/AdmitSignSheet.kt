package com.invex.app.admitcard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invex.app.sign.ConsentCard
import com.invex.app.sign.SheetButtons
import com.invex.app.sign.SignRequestActions
import com.invex.app.sign.SignRequestUiState
import com.invex.app.sign.SignTabs
import com.invex.app.sign.SignatureBox
import com.invex.app.sign.UnderlinedText
import com.invex.app.ui.theme.ComboText
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText

/**
 * "E-Signature" bottom sheet of the admit card: the same drawing / typing parts as the teacher's sign sheet,
 * without the student search.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmitSignSheet(
    state: SignRequestUiState,
    isSigning: Boolean,
    actions: SignRequestActions,
) {
    ModalBottomSheet(
        onDismissRequest = actions.onCancel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SoftWhite,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UnderlinedText(
                text = "E-Signature",
                style = InvexType.SheetHeading,
                color = ComboText,
                underlineColor = Orange,
                thickness = 4.dp,
                gap = 4.dp,
            )
            SignTabs(selected = state.tab, onSelect = actions.onTabChange)
            SignatureBox(
                tab = state.tab,
                strokes = state.strokes,
                typedName = state.typedName,
                isSaved = state.isSignatureSaved,
                onStrokeStart = actions.onStrokeStart,
                onStrokeMove = actions.onStrokeMove,
                onTypedNameChange = actions.onTypedNameChange,
                onBack = actions.onBack,
                onClear = actions.onClear,
                onSave = actions.onSaveSignature,
            )
            Text(
                text = "This admit card will be signed by and sent to subsequent signee.",
                style = InvexType.SubLabel,
                color = TableHeaderText,
            )
            ConsentCard(
                consent = state.consent,
                onConsentChange = actions.onConsentChange,
                warning = state.warning,
                text = "By signing, I verify my details and subjects are correct and agree to follow all examination rules.",
            )
            SheetButtons(
                onCancel = actions.onCancel,
                onSign = { if (!isSigning) actions.onSign() },
                signLabel = if (isSigning) "Signing..." else "Sign the admit card",
            )
        }
    }
}
