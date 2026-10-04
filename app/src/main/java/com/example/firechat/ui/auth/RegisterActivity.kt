package com.example.firechat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.firechat.databinding.ActivityRegisterBinding
import com.example.firechat.ui.users.UsersActivity
import com.example.firechat.util.Resource

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                name = binding.etName.text.toString(),
                email = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString(),
                confirmPassword = binding.etConfirmPassword.text.toString()
            )
        }
        binding.tvGoLogin.setOnClickListener { finish() }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.formErrors.observe(this) { errors ->
            binding.tilName.error = errors.name
            binding.tilEmail.error = errors.email
            binding.tilPassword.error = errors.password
            binding.tilConfirmPassword.error = errors.confirmPassword
        }
        viewModel.authState.observe(this) { state ->
            val loading = state is Resource.Loading
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !loading
            when (state) {
                is Resource.Success -> {
                    startActivity(
                        Intent(this, UsersActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                    finish()
                }
                is Resource.Error -> Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }
    }
}
