package com.fmcg.app.data.repository

import com.fmcg.app.data.mapper.toDomain
import com.fmcg.app.data.remote.api.UserApi
import com.fmcg.app.data.remote.dto.UserCreateDto
import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.model.UserDraft
import com.fmcg.app.domain.repository.UserRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val io: CoroutineDispatcher,
) : UserRepository {

    override suspend fun listUsers(): Resource<List<User>> = withContext(io) {
        try {
            Resource.Success(api.list().items.map { it.toDomain() })
        } catch (e: HttpException) {
            Resource.Error(httpMessage(e), e.code())
        } catch (e: IOException) {
            Resource.Error("Network error — check your connection")
        }
    }

    override suspend fun createUser(draft: UserDraft): Resource<User> = withContext(io) {
        try {
            val dto = api.create(
                UserCreateDto(
                    email = draft.email.trim(),
                    fullName = draft.fullName.trim(),
                    role = draft.role.name.lowercase(),
                    password = draft.password,
                    phone = draft.phone?.trim()?.takeIf { it.isNotEmpty() },
                )
            )
            Resource.Success(dto.toDomain())
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                409 -> "That email is already registered"
                422 -> "Check the details — the server rejected them"
                403 -> "Only an admin can create users"
                else -> httpMessage(e)
            }
            Resource.Error(msg, e.code())
        } catch (e: IOException) {
            Resource.Error("Network error — check your connection")
        }
    }

    override suspend fun deleteUser(id: Int): Resource<Unit> = withContext(io) {
        try {
            api.delete(id)
            Resource.Success(Unit)
        } catch (e: HttpException) {
            Resource.Error(httpMessage(e), e.code())
        } catch (e: IOException) {
            Resource.Error("Network error — check your connection")
        }
    }

    private fun httpMessage(e: HttpException) = when (e.code()) {
        401 -> "Session expired — sign in again"
        403 -> "Admin access required"
        404 -> "User not found"
        else -> "Request failed (${e.code()})"
    }
}
