package com.fmcg.app.presentation.admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.model.UserDraft
import com.fmcg.app.domain.model.UserRole
import com.fmcg.app.domain.repository.UserRepository
import com.fmcg.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    // form fields
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val password: String = "",
    val role: UserRole = UserRole.MARKETING,
) {
    val canSubmit: Boolean
        get() = email.contains("@") &&
            fullName.isNotBlank() &&
            password.length >= 8 &&
            !isSaving
}

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UserManagementUiState())
    val state: StateFlow<UserManagementUiState> = _state.asStateFlow()

    init { refresh() }

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, error = null) }
    fun onFullNameChange(v: String) = _state.update { it.copy(fullName = v, error = null) }
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }
    fun onRoleChange(v: UserRole) = _state.update { it.copy(role = v, error = null) }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearMessage() = _state.update { it.copy(message = null) }

    fun refresh() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val r = repository.listUsers()) {
                is Resource.Success ->
                    _state.update { it.copy(isLoading = false, users = r.data) }
                is Resource.Error ->
                    _state.update { it.copy(isLoading = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun createUser() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val draft = UserDraft(
                email = s.email,
                fullName = s.fullName,
                role = s.role,
                password = s.password,
                phone = s.phone,
            )
            when (val r = repository.createUser(draft)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            email = "",
                            fullName = "",
                            phone = "",
                            password = "",
                            message = "Created ${r.data.email}",
                        )
                    }
                    refresh()
                }
                is Resource.Error ->
                    _state.update { it.copy(isSaving = false, error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            when (val r = repository.deleteUser(id)) {
                is Resource.Success -> refresh()
                is Resource.Error -> _state.update { it.copy(error = r.message) }
                Resource.Loading -> Unit
            }
        }
    }
}
