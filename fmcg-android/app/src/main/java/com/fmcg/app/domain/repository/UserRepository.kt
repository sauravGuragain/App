package com.fmcg.app.domain.repository

import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.model.UserDraft
import com.fmcg.app.util.Resource

/** Admin-only user management. Every call requires an admin token. */
interface UserRepository {
    suspend fun listUsers(): Resource<List<User>>
    suspend fun createUser(draft: UserDraft): Resource<User>
    suspend fun deleteUser(id: Int): Resource<Unit>
}
