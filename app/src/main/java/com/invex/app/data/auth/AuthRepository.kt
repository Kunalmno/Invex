package com.invex.app.data.auth

import com.invex.app.login.UserRole
import com.invex.app.register.IdCheck
import com.invex.app.register.IdRules
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException

@Serializable
private data class ProgramRow(val code: String, val name: String)

/**
 * Login with a Faculty ID / Student ID and a password.
 * Supabase logs in with an email, so the ID is turned into `<id in lower case>@invex.app`
 * (the same rule used when the admin creates the user; see Backend.md).
 */
class AuthRepository(private val client: SupabaseClient) {

    suspend fun login(role: UserRole, id: String, password: String): LoginResult {
        val cleanId = id.trim()
        if (cleanId.isEmpty() || password.isEmpty()) {
            return LoginResult.Failure("Enter your ${role.idLabel} and password")
        }
        return try {
            client.auth.signInWith(Email) {
                this.email = emailFor(cleanId)
                this.password = password
            }
            val profile = loadProfile()
            when {
                profile == null -> {
                    client.auth.signOut()
                    LoginResult.Failure("This account is not set up yet. Contact the admin.")
                }
                profile.role != role.dbValue -> {
                    client.auth.signOut()
                    LoginResult.Failure("This ID is not a ${role.label} account")
                }
                else -> LoginResult.Success(profile)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthRestException) {
            LoginResult.Failure("Wrong ${role.idLabel} or password")
        } catch (e: Exception) {
            LoginResult.Failure("Could not reach the server. Check your internet connection and try again.")
        }
    }

    /**
     * Creates the account and signs the person in. The ID format is checked here for a clear message and again by
     * the database (migration 0017), which also creates the profile and decides the role from the ID.
     * Needs "Confirm email" switched OFF in Supabase (the emails are made up).
     */
    suspend fun register(role: UserRole, id: String, password: String): RegisterResult {
        val check = IdRules.check(role, id)
        if (check !is IdCheck.Valid) return RegisterResult.Failure((check as IdCheck.Invalid).message)
        return try {
            if (check.courseCode != null) {
                val known = client.from("programs")
                    .select(Columns.list("code", "name")) { filter { eq("code", check.courseCode) } }
                    .decodeList<ProgramRow>()
                if (known.isEmpty()) {
                    return RegisterResult.Failure("Course code ${check.courseCode} is not available. Check your Student ID.")
                }
            }
            client.auth.signUpWith(Email) {
                this.email = emailFor(check.id)
                this.password = password
                data = buildJsonObject { put("self_register", true) }
            }
            if (client.auth.currentSessionOrNull() == null) {
                return RegisterResult.Failure("Account created, but it needs email confirmation. Ask the admin to turn it off.")
            }
            val profile = loadProfile()
            when {
                profile == null -> {
                    client.auth.signOut()
                    RegisterResult.Failure("Could not set up your account. Check your ${role.idLabel} and try again.")
                }
                profile.role != role.dbValue -> {
                    client.auth.signOut()
                    RegisterResult.Failure("This ID is not a ${role.label} ID")
                }
                else -> RegisterResult.Success(profile)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthRestException) {
            val text = e.message.orEmpty()
            RegisterResult.Failure(
                when {
                    text.contains("already", ignoreCase = true) -> "This ${role.idLabel} is already registered. Log in instead."
                    text.contains("password", ignoreCase = true) -> "Choose a stronger password (at least 6 characters)."
                    text.contains("Database error", ignoreCase = true) -> "This ${role.idLabel} cannot be registered. Check it and try again."
                    else -> "Could not create your account. Try again."
                },
            )
        } catch (e: Exception) {
            RegisterResult.Failure("Could not reach the server. Check your internet connection and try again.")
        }
    }

    /** Called when the app starts: the saved login, if there is one. Null means "show Onboarding / Login". */
    suspend fun restoreSession(): UserProfile? {
        client.auth.awaitInitialization()
        if (client.auth.currentSessionOrNull() == null) return null
        return try {
            loadProfile()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun logout() {
        try {
            client.auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Offline: the local session is still cleared by the library on the next start.
        }
    }

    private suspend fun loadProfile(): UserProfile? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return client.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<UserProfile>()
    }

    private fun emailFor(id: String) = "${id.lowercase()}@invex.app"
}
