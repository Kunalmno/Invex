package com.invex.app.supplement

/** A student who can be issued a sheet (the searchable list in the sign sheet). */
data class RosterStudent(
    val id: String,          // seat assignment id
    val name: String,
    val course: String,      // course code, e.g. "CS101"
    val seat: String,        // "Row 2, Seat 3" (the design's "3rd Year" is not stored)
    val rollNumber: String,  // registration number
) {
    val meta: String get() = "$course | $seat | $rollNumber"
}

/** A row of the "Sheets Issued" list: one per student, with a running sheet count. */
data class StudentSheet(
    val id: String,
    val name: String,
    val course: String,
    val seat: String,
    val rollNumber: String,
    val sheetCount: Int,
    val status: SheetStatus,
) {
    val meta: String get() = "$course | $seat | $rollNumber"
}

// PENDING = the sheet is still with the student (Awaiting Return). RESOLVED = returned.
enum class SheetStatus(val label: String) {
    RESOLVED("Resolved"),
    PENDING("Pending"),
}

data class SheetSummary(
    val issued: Int = 0,
    val awaitingReturn: Int = 0,
    val returned: Int = 0,
    val inStock: Int = 0,
)
