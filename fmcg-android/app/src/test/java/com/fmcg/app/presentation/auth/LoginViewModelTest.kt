package com.fmcg.app.presentation.auth

import com.fmcg.app.MainDispatcherRule
import com.fmcg.app.domain.model.User
import com.fmcg.app.domain.model.UserRole
import com.fmcg.app.domain.usecase.LoginUseCase
import com.fmcg.app.fakes.FakeAuthRepository
import com.fmcg.app.fakes.FakePushRepository
import com.fmcg.app.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val user = User(1, "rep@ex.com", "Rep", UserRole.MARKETING, null, true)

    private fun viewModel(result: Resource<User>) =
        LoginViewModel(LoginUseCase(FakeAuthRepository(result)), FakePushRepository())

    @Test
    fun successful_login_sets_role() = runTest {
        val vm = viewModel(Resource.Success(user))
        vm.onEmailChange("rep@ex.com")
        vm.onPasswordChange("password123")
        vm.login()
        advanceUntilIdle()
        assertEquals(UserRole.MARKETING, vm.state.value.loggedInRole)
        assertNull(vm.state.value.error)
    }

    @Test
    fun blank_fields_produce_error_without_calling_backend() = runTest {
        val vm = viewModel(Resource.Success(user))
        vm.login()
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.loggedInRole)
    }

    @Test
    fun failed_login_surfaces_error() = runTest {
        val vm = viewModel(Resource.Error("Incorrect email or password"))
        vm.onEmailChange("rep@ex.com")
        vm.onPasswordChange("password123")
        vm.login()
        advanceUntilIdle()
        assertEquals("Incorrect email or password", vm.state.value.error)
        assertNull(vm.state.value.loggedInRole)
    }
}
