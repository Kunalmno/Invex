package com.invex.app.results

import java.util.Locale

data class SubjectResult(val name: String, val code: String, val grade: Double) {
    /** "9.40" */
    val gradeText: String get() = String.format(Locale.US, "%.2f", grade)
}

data class SemesterResult(val semester: Int, val subjects: List<SubjectResult>) {
    /** "Semester one" (dropdown). */
    val label: String get() = "Semester ${semesterWord(semester)}"
    /** "Semester One" (heading). */
    val heading: String get() = "Semester ${semesterWord(semester).replaceFirstChar { it.uppercase() }}"
}

private fun semesterWord(n: Int): String = when (n) {
    1 -> "one"; 2 -> "two"; 3 -> "three"; 4 -> "four"; 5 -> "five"; 6 -> "six"
    7 -> "seven"; 8 -> "eight"; 9 -> "nine"; 10 -> "ten"; 11 -> "eleven"; 12 -> "twelve"
    else -> n.toString()
}

data class ResultsUiState(
    val semesters: List<SemesterResult> = emptyList(),
    /** The semester number chosen in the dropdown. */
    val selectedSemester: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val dropdownOpen: Boolean = false,
) {
    val selected: SemesterResult? get() = semesters.firstOrNull { it.semester == selectedSemester }
}
