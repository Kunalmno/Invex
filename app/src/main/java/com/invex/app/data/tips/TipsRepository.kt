package com.invex.app.data.tips

import com.invex.app.dosdonts.TipGroup
import com.invex.app.dosdonts.TipTopic
import com.invex.app.dosdonts.Tip
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class TipRow(
    val kind: String,
    val unit: String,
    @SerialName("topic_heading") val topicHeading: String,
    @SerialName("sub_topic") val subTopic: String,
    val rating: Int,
)

sealed interface TipsResult {
    /** [dos] and [donts] are lists of units (in the admin's order). */
    data class Success(val dos: List<TipGroup>, val donts: List<TipGroup>) : TipsResult
    data class Failure(val message: String) : TipsResult
}

/** Reads the exam tips the admin entered and rated. Students cannot change them. */
class TipsRepository(private val client: SupabaseClient) {

    suspend fun load(): TipsResult = try {
        val rows = client.from("exam_tips")
            .select(Columns.list("kind", "unit", "topic_heading", "sub_topic", "rating")) {
                order("sort_order", Order.ASCENDING)
            }
            .decodeList<TipRow>()
        TipsResult.Success(
            dos = group(rows.filter { it.kind == "do" }),
            donts = group(rows.filter { it.kind == "dont" }),
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        TipsResult.Failure("Could not load the Do’s and Don’ts. Check your internet connection.")
    }

    /** Unit -> topic heading -> sub topics, keeping the order the rows came in. */
    private fun group(rows: List<TipRow>): List<TipGroup> =
        rows.groupBy { it.unit }.map { (unit, unitRows) ->
            TipGroup(
                unit = unit,
                topics = unitRows.groupBy { it.topicHeading }.map { (heading, topicRows) ->
                    TipTopic(heading, topicRows.map { Tip(it.subTopic, it.rating.coerceIn(1, 5)) })
                },
            )
        }
}
