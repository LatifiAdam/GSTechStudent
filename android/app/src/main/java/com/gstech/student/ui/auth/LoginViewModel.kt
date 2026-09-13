package com.gstech.student.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gstech.student.data.repository.AuthRepository
import com.gstech.student.model.Role
import com.gstech.student.util.UiState
import com.gstech.student.util.userFriendlyErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>?>(null)
    val state: StateFlow<UiState<Unit>?> = _state.asStateFlow()

    fun login(
        email: String,
        password: String,
        onSuccess: (Role) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = UiState.Error(
                "Enter your institutional email and password."
            )
            return
        }

        _state.value = UiState.Loading

        viewModelScope.launch {
            try {
                val role = authRepository.login(
                    email.trim(),
                    password
                )

                _state.value = UiState.Success(Unit)
                onSuccess(role)

            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400 -> "Invalid login information. Please check your email and password."
                    401 -> "Incorrect email or password."
                    403 -> "You are not authorized to access this account."
                    404 -> "Login service was not found."
                    429 -> "Too many login attempts. Please wait and try again."
                    500, 502, 503, 504 ->
                        "Server error. Please try again later."
                    else ->
                        "Login failed (${e.code()}). Please try again."
                }

                _state.value = UiState.Error(message)

            } catch (e: IOException) {
                _state.value = UiState.Error(
                    "Unable to connect to the server. Check your connection and try again."
                )

            } catch (e: Exception) {
                _state.value = UiState.Error(
                    e.message ?: "An unexpected error occurred. Please try again."
                )
            }
        }
    }
}