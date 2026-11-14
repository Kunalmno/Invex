package com.invex.app.admitcard

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One exam on the card / one subject to read and verify. */
data class AdmitExam(
    val courseCode: String,
    val courseTitle: String,
    val startsAt: OffsetDateTime,
    val endsAt: OffsetDateTime,
) {
    private val start get() = startsAt.atZoneSameInstant(ZoneId.systemDefault())

    /** "01 / 01 / 2000" */
    val dateText: String get() = start.format(DateTimeFormatter.ofPattern("dd / MM / yyyy", Locale.ENGLISH))

    /** Shift A = the exam starts before 12:00, shift B = from 12:00. */
    val shift: String get() = if (start.hour < 12) "A" else "B"
}

data class AdmitStudent(
    val name: String,
    val enrollmentNumber: String,
    val course: String,
    val specialization: String,
    val branch: String,
    val semester: Int?,
)

/** The finished card. [qrCode] is null when the student has no upcoming exam. [signaturePng] is black on transparent. */
class AdmitCard(
    val student: AdmitStudent,
    val exams: List<AdmitExam>,
    val qrCode: String?,
    val signaturePng: ByteArray?,
)

/** What "Verify the Card" shows: the official record behind the QR code. */
data class VerifiedCard(
    val studentName: String,
    val enrollmentNumber: String,
    val courseCode: String,
    val courseTitle: String,
    val building: String,
    val room: String,
    val startsAt: OffsetDateTime,
    val seatRow: Int,
    val seatCol: Int,
) {
    val whenText: String
        get() = startsAt.atZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH))
}

enum class AdmitStep { Reading, Card }

data class AdmitCardUiState(
    val step: AdmitStep = AdmitStep.Reading,
    val isLoading: Boolean = true,
    val error: String? = null,
    val subjects: List<AdmitExam> = emptyList(),
    val verified: Boolean = false,
    val signSheetOpen: Boolean = false,
    val isSigning: Boolean = false,
    val signError: String? = null,
    val card: AdmitCard? = null,
    // Report dialog
    val reportOpen: Boolean = false,
    val reportText: String = "",
    val isReporting: Boolean = false,
    val reportError: String? = null,
    val reportSent: Boolean = false,
    // Verify the card dialog
    val isVerifying: Boolean = false,
    val verifiedCard: VerifiedCard? = null,
    val verifyError: String? = null,
    // Download
    val downloadMessage: String? = null,
)

/** "First Subject" ... */
fun subjectLabel(index: Int): String {
    val words = listOf("First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth", "Tenth", "Eleventh", "Twelfth")
    return (words.getOrNull(index) ?: "${index + 1}th") + " Subject"
}
