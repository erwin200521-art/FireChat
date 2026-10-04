package com.example.firechat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.firechat.databinding.ActivityLoginBinding
import com.example.firechat.ui.users.UsersActivity
import com.example.firechat.util.Resource

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sesión persistente: si ya hay usuario autenticado, saltamos el login
        if (viewModel.isLoggedIn()) {
            goToUsers()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString()
            )
        }
        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.formErrors.observe(this) { errors ->
            binding.tilEmail.error = errors.email
            binding.tilPassword.error = errors.password
        }
        viewModel.authState.observe(this) { state ->
            val loading = state is Resource.Loading
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !loading
            when (state) {
                is Resource.Success -> goToUsers()
                is Resource.Error -> Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }
    }

    private fun goToUsers() {
        startActivity(
            Intent(this, UsersActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
