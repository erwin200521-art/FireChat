package com.example.firechat.ui.users

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.firechat.R
import com.example.firechat.data.repository.AuthRepository
import com.example.firechat.databinding.ActivityUsersBinding
import com.example.firechat.ui.auth.LoginActivity
import com.example.firechat.ui.chat.ChatActivity
import com.example.firechat.util.Resource

/** Lista de usuarios disponibles para conversar. */
class UsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersBinding
    private val viewModel: UsersViewModel by viewModels()

    private val adapter = UsersAdapter { user ->
        startActivity(ChatActivity.newIntent(this, user.uid, user.name))
    }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* opcional */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Por ejemplo, al abrir desde una notificación tras cerrar sesión
        if (!AuthRepository().isLoggedIn()) {
            goToLogin()
            return
        }

        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.rvUsers.adapter = adapter
        observeViewModel()
        requestNotificationPermission()
    }

    private fun observeViewModel() {
        viewModel.users.observe(this) { state ->
            binding.progress.visibility = if (state is Resource.Loading) View.VISIBLE else View.GONE
            when (state) {
                is Resource.Success -> {
                    adapter.submitList(state.data)
                    binding.tvEmpty.visibility = if (state.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resource.Error -> Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                else -> Unit
            }
        }
        viewModel.loggedOut.observe(this) { event ->
            event.getContentIfNotHandled()?.let { goToLogin() }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.users_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == R.id.action_logout) {
            viewModel.logout()
            true
        } else super.onOptionsItemSelected(item)

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}
