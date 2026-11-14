package com.invex.app.broadcasts

/**
 * One broadcast from the admin. The list shows [heading], [subheading] and [snippet];
 * the detail page shows [heading], [body] and [broadcastedBy].
 * [id] matches the id of the Dashboard feed row ("b1", "b2", ...).
 */
data class Broadcast(
    val id: String,
    val heading: String,
    val subheading: String,
    val snippet: String,
    val body: String,
    val broadcastedBy: String,
)

/** Which sheet is open on top of the Dashboard. Only one at a time. */
sealed interface BroadcastSheet {
    data object Closed : BroadcastSheet
    data object List : BroadcastSheet
    data class Detail(val id: String) : BroadcastSheet
}

private const val LOREM = "Lorem Ipsum Dorem Lorem Ipsum Dorem Lorem Ipsum Dorem. "

/** Sample broadcasts for previews only. */
fun sampleBroadcasts(): kotlin.collections.List<Broadcast> = listOf("b1", "b2", "b3").map { id ->
    Broadcast(
        id = id,
        heading = "Heading One",
        subheading = "Lorem Ipsum Dorem",
        snippet = "Lorem Ipsum Dorem Lorem Ipsum Dorem.",
        body = LOREM.repeat(6).trim(),
        broadcastedBy = "Admin",
    )
}

data class BroadcastsUiState(
    /** Newest first, loaded from the backend (only the ones meant for this role). */
    val broadcasts: kotlin.collections.List<Broadcast> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val sheet: BroadcastSheet = BroadcastSheet.Closed,
) {
    fun find(id: String): Broadcast? = broadcasts.firstOrNull { it.id == id }
}
