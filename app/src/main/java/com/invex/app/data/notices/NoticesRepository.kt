package com.invex.app.data.notices

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.hours

/** One notice as the database function `list_notices` returns it (a broadcast or a circular). */
@Serializable
private data class NoticeRow(
    val id: String,
    val heading: String,
    val subheading: String = "",
    val snippet: String = "",
    val body: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("attachment_path") val attachmentPath: String? = null,
    @SerialName("attachment_name") val attachmentName: String? = null,
    @SerialName("published_at") val publishedAt: String,
)

/** A notice ready to show. [attachmentUrl] is a temporary link (1 hour) to the PDF, or null when there is none. */
data class Notice(
    val id: String,
    val heading: String,
    val subheading: String,
    val snippet: String,
    val body: String,
    val authorName: String,
    val attachmentName: String?,
    val attachmentUrl: String?,
    val publishedText: String,
)

sealed interface NoticesResult {
    data class Success(val notices: List<Notice>) : NoticesResult
    data class Failure(val message: String) : NoticesResult
}

enum class NoticeKind(val dbValue: String) { BROADCAST("broadcast"), CIRCULAR("circular") }

/**
 * Broadcasts and circulars written by the admin. The database only returns the ones meant for the
 * signed-in role. Circular PDFs sit in a private bucket, so each gets a temporary signed link.
 */
class NoticesRepository(private val client: SupabaseClient) {

    suspend fun load(kind: NoticeKind): NoticesResult = try {
        val rows = client.postgrest
            .rpc("list_notices", buildJsonObject { put("p_kind", kind.dbValue) })
            .decodeList<NoticeRow>()
        val zone = ZoneId.systemDefault()
        NoticesResult.Success(
            rows.map { row ->
                Notice(
                    id = row.id,
                    heading = row.heading,
                    subheading = row.subheading,
                    snippet = row.snippet,
                    body = row.body,
                    authorName = row.authorName,
                    attachmentName = row.attachmentName ?: row.attachmentPath?.substringAfterLast('/'),
                    attachmentUrl = row.attachmentPath?.let { signedUrl(it) },
                    publishedText = OffsetDateTime.parse(row.publishedAt).atZoneSameInstant(zone).format(DATE_FORMAT),
                )
            },
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        NoticesResult.Failure("Could not load. Check your internet connection.")
    }

    /** If the link cannot be made the file still shows, it just does not open (the row is not clickable). */
    private suspend fun signedUrl(path: String): String? = try {
        client.storage.from("notice-files").createSignedUrl(path, 1.hours)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, hh:mm a", Locale.ENGLISH)
    }
}
