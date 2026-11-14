package com.invex.app.medicalleave

/** What the reading screen shows. Empty text = the admin has not filled it in yet (shown as "-"). */
data class LeaveStudent(
    val name: String,
    val enrollmentNumber: String,
    val course: String,
    val specialization: String,
    val branch: String,
    val semester: Int?,
    val mobile: String,
    val email: String,
)

/** "Third" (reading screen). */
fun semesterOrdinal(n: Int?): String = when (n) {
    null -> ""
    1 -> "First"; 2 -> "Second"; 3 -> "Third"; 4 -> "Fourth"; 5 -> "Fifth"; 6 -> "Sixth"
    7 -> "Seventh"; 8 -> "Eighth"; 9 -> "Ninth"; 10 -> "Tenth"; 11 -> "Eleventh"; 12 -> "Twelfth"
    else -> n.toString()
}

/** The chosen file, already read into memory (max 10 MB). */
class PickedDocument(val name: String, val mimeType: String, val bytes: ByteArray)

enum class LeaveStep { Reading, Form, Sent }

val MEDICAL_CONDITIONS = listOf(
    "Fever", "Cold and Cough", "Headache or Migraine", "Stomach Problem", "Injury", "Infection", "Other",
)

const val MAX_DETAILS_LENGTH = 1000
const val MAX_DOCUMENT_BYTES = 10 * 1024 * 1024

data class MedicalLeaveUiState(
    val step: LeaveStep = LeaveStep.Reading,
    val isLoading: Boolean = true,
    val error: String? = null,
    val student: LeaveStudent? = null,
    /** "29 Jan 26, 19:09:24" */
    val applicationDate: String = "",
    val verified: Boolean = false,
    val condition: String = MEDICAL_CONDITIONS.first(),
    val conditionMenuOpen: Boolean = false,
    val details: String = "",
    val document: PickedDocument? = null,
    val documentError: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
)
