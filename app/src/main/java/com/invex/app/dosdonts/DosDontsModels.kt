package com.invex.app.dosdonts

data class Tip(val subTopic: String, val rating: Int)
data class TipTopic(val heading: String, val tips: List<Tip>)
data class TipGroup(val unit: String, val topics: List<TipTopic>)

data class DosDontsUiState(
    val dos: List<TipGroup> = emptyList(),
    val donts: List<TipGroup> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)
