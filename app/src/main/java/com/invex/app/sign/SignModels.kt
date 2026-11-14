package com.invex.app.sign

import androidx.compose.ui.geometry.Offset

enum class SignTab(val label: String) {
    DRAW("Draw"),
    TYPE("Type"),
}

/** Why a sign attempt was refused. The banner shows [message]. Only NO_CONSENT is the designed text. */
enum class SignWarning(val message: String) {
    NO_STUDENT("Please select a student before signing."),
    NO_SIGNATURE("Please add your signature before signing."),
    NO_CONSENT("Please confirm you agree to all terms before signing."),
}

/** A signature the teacher chose to keep with "Save signature". It is reloaded the next time the sheet opens. */
data class SavedSignature(
    val tab: SignTab,
    val strokes: List<List<Offset>>,
    val typedName: String,
)

data class SignRequestUiState(
    val query: String = "",
    val selectedStudentId: String? = null,
    val tab: SignTab = SignTab.DRAW,
    val strokes: List<List<Offset>> = emptyList(),
    val typedName: String = "",
    val consent: Boolean = false,
    val warning: SignWarning? = null,
    val savedSignature: SavedSignature? = null,
) {
    val hasSignature: Boolean
        get() = when (tab) {
            SignTab.DRAW -> strokes.isNotEmpty()
            SignTab.TYPE -> typedName.isNotBlank()
        }

    /** True when the signature currently on screen is the one that was saved. */
    val isSignatureSaved: Boolean
        get() = hasSignature && savedSignature == SavedSignature(tab, strokes, typedName)
}

/** Everything the sheet can do. All default to no-ops so previews stay short. */
class SignRequestActions(
    val onQueryChange: (String) -> Unit = {},
    val onSelectStudent: (String) -> Unit = {},
    val onTabChange: (SignTab) -> Unit = {},
    val onStrokeStart: (Offset) -> Unit = {},
    val onStrokeMove: (Offset) -> Unit = {},
    val onBack: () -> Unit = {},
    val onClear: () -> Unit = {},
    val onTypedNameChange: (String) -> Unit = {},
    val onSaveSignature: () -> Unit = {},
    val onConsentChange: (Boolean) -> Unit = {},
    val onCancel: () -> Unit = {},
    val onSign: () -> Unit = {},
)
