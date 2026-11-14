package com.invex.app.circulars

/** A document attached to a circular. [url] is what opens when the row is tapped. */
data class CircularAttachment(
    val fileName: String,
    val dateText: String,
    val url: String,
)

/**
 * One circular from the admin. Same as a broadcast, plus an optional attached document.
 * [id] matches the id of the Dashboard feed row ("c1", "c2", ...).
 */
data class Circular(
    val id: String,
    val heading: String,
    val subheading: String,
    val snippet: String,
    val body: String,
    val circulatedBy: String,
    val attachment: CircularAttachment? = null,
)

sealed interface CircularSheet {
    data object Closed : CircularSheet
    data object List : CircularSheet
    data class Detail(val id: String) : CircularSheet
}

private const val LOREM = "Lorem Ipsum Dorem Lorem Ipsum Dorem Lorem Ipsum Dorem. "

/** Sample circulars for previews only. */
fun sampleCirculars(): kotlin.collections.List<Circular> = listOf("c1", "c2", "c3").map { id ->
    Circular(
        id = id,
        heading = "Heading One",
        subheading = "Lorem Ipsum Dorem",
        snippet = "Lorem Ipsum Dorem Lorem Ipsum Dorem.",
        body = LOREM.repeat(6).trim(),
        circulatedBy = "Admin",
        attachment = CircularAttachment(
            fileName = "CircularPDF_2026_10_01.pdf",
            dateText = "17 August 2026, 10:10 AM",
            url = "",
        ),
    )
}

data class CircularsUiState(
    /** Newest first, loaded from the backend (only the ones meant for this role). */
    val circulars: kotlin.collections.List<Circular> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val sheet: CircularSheet = CircularSheet.Closed,
) {
    fun find(id: String): Circular? = circulars.firstOrNull { it.id == id }
}
