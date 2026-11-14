package com.invex.app.data

import com.invex.app.BuildConfig
import com.invex.app.data.attendance.AttendanceRepository
import com.invex.app.data.auth.AuthRepository
import com.invex.app.data.duties.DutiesRepository
import com.invex.app.data.exam.ExamDetailsRepository
import com.invex.app.data.admitcard.AdmitCardRepository
import com.invex.app.data.medical.MedicalLeaveRepository
import com.invex.app.data.notices.NoticesRepository
import com.invex.app.data.restroom.RestroomRepository
import com.invex.app.data.results.ResultsRepository
import com.invex.app.data.tips.TipsRepository
import com.invex.app.data.rooms.RoomLayoutRepository
import com.invex.app.data.supplement.SupplementRepository
import com.invex.app.data.verify.VerifyAdmitRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * The one connection to Supabase for the whole app.
 * The URL and the public (anon / publishable) key come from local.properties through BuildConfig,
 * so they are not typed into the code. Never put the service_role key in the app.
 */
object InvexBackend {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)      // login; the session is saved on the phone
            install(Postgrest) // tables, views and functions
            install(Storage)   // files (circular PDFs, signatures)
        }
    }

    val auth: AuthRepository by lazy { AuthRepository(client) }
    val duties: DutiesRepository by lazy { DutiesRepository(client) }
    val examDetails: ExamDetailsRepository by lazy { ExamDetailsRepository(client) }
    val verifyAdmit: VerifyAdmitRepository by lazy { VerifyAdmitRepository(client) }
    val attendance: AttendanceRepository by lazy { AttendanceRepository(client) }
    val supplement: SupplementRepository by lazy { SupplementRepository(client) }
    val restroom: RestroomRepository by lazy { RestroomRepository(client) }
    val notices: NoticesRepository by lazy { NoticesRepository(client) }
    val roomLayout: RoomLayoutRepository by lazy { RoomLayoutRepository(client) }
    val results: ResultsRepository by lazy { ResultsRepository(client) }
    val medicalLeave: MedicalLeaveRepository by lazy { MedicalLeaveRepository(client) }
    val admitCard: AdmitCardRepository by lazy { AdmitCardRepository(client) }
    val tips: TipsRepository by lazy { TipsRepository(client) }
}
