package com.czcz.myapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppViewModel : ViewModel() {

    // 注册表单状态
    private val _account = MutableStateFlow("")
    val account: StateFlow<String> = _account

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible

    private val _confirmPasswordVisible = MutableStateFlow(false)
    val confirmPasswordVisible: StateFlow<Boolean> = _confirmPasswordVisible

    private val _agreed = MutableStateFlow(false)
    val agreed: StateFlow<Boolean> = _agreed

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 校验逻辑
    val isAccountValid: Boolean
        get() = _account.value.length >= 4

    val isPasswordValid: Boolean
        get() = _password.value.length >= 6

    val isConfirmValid: Boolean
        get() = _confirmPassword.value == _password.value && _confirmPassword.value.isNotEmpty()

    val canRegister: Boolean
        get() = isAccountValid && isPasswordValid && isConfirmValid && _agreed.value && !_isLoading.value

    // Setter 方法
    fun setAccount(value: String) { _account.value = value }
    fun setPassword(value: String) { _password.value = value }
    fun setConfirmPassword(value: String) { _confirmPassword.value = value }
    fun setPasswordVisible(value: Boolean) { _passwordVisible.value = value }
    fun setConfirmPasswordVisible(value: Boolean) { _confirmPasswordVisible.value = value }
    fun setAgreed(value: Boolean) { _agreed.value = value }

    fun register(onSuccess: () -> Unit) {
        if (canRegister) {
            _isLoading.value = true
            onSuccess()
        }
    }

    fun resetRegisterState() {
        _isLoading.value = false
    }
}
