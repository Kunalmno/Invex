package com.invex.app.register

import com.invex.app.login.UserRole
import java.time.Year

/**
 * The ID formats. The database checks the same rules (migration 0017), this gives the person a clear message first.
 *   Faculty  FAC25001      = FAC + joining year (2 digits) + 3-digit number
 *   Student  STUD25101001  = STUD + enrollment year (2 digits) + course code (3 digits, 101 = B.Tech) + 3-digit number
 */
sealed interface IdCheck {
    /** [id] is trimmed and in capital letters, [courseCode] is only set for students. */
    data class Valid(val id: String, val courseCode: String?) : IdCheck
    data class Invalid(val message: String) : IdCheck
}

object IdRules {
    private val faculty = Regex("^FAC(\\d{2})(\\d{3})$")
    private val student = Regex("^STUD(\\d{2})(\\d{3})(\\d{3})$")

    const val FACULTY_EXAMPLE = "FAC25001"
    const val STUDENT_EXAMPLE = "STUD25101001"

    fun check(role: UserRole, raw: String, currentYear: Int = Year.now().value): IdCheck {
        val id = raw.trim().uppercase()
        if (id.isEmpty()) return IdCheck.Invalid("Please enter your ${role.idLabel}")
        val nowYY = currentYear % 100

        return when (role) {
            UserRole.FACULTY -> {
                val m = faculty.matchEntire(id)
                    ?: return IdCheck.Invalid(
                        if (id.startsWith("STUD")) "This looks like a Student ID. Switch to Student."
                        else "Faculty ID looks like $FACULTY_EXAMPLE: FAC, joining year, 3-digit number",
                    )
                val (year, number) = m.destructured
                when {
                    year.toInt() > nowYY -> IdCheck.Invalid("The joining year in the ID can't be in the future")
                    number == "000" -> IdCheck.Invalid("The last 3 digits can't be 000")
                    else -> IdCheck.Valid(id, null)
                }
            }
            UserRole.STUDENT -> {
                val m = student.matchEntire(id)
                    ?: return IdCheck.Invalid(
                        if (id.startsWith("FAC")) "This looks like a Faculty ID. Switch to Faculty."
                        else "Student ID looks like $STUDENT_EXAMPLE: STUD, enrollment year, course code, 3-digit number",
                    )
                val (year, course, number) = m.destructured
                when {
                    year.toInt() > nowYY -> IdCheck.Invalid("The enrollment year in the ID can't be in the future")
                    number == "000" -> IdCheck.Invalid("The last 3 digits can't be 000")
                    else -> IdCheck.Valid(id, course)
                }
            }
        }
    }
}
