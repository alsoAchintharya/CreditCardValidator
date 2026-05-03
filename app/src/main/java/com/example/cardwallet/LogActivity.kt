package com.example.cardwallet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cardwallet.viewmodel.LogViewModel
import kotlinx.coroutines.launch
import java.io.File

class LogActivity : AppCompatActivity() {

    private val viewModel: LogViewModel by viewModels()

    private lateinit var camView: ImageView

    private var tempUri: Uri? = null
    private var photoFile: File? = null
    private var verified = false

    private lateinit var loginBtn: Button
    private lateinit var verifyBtn: Button

    private lateinit var currentUsername: String

    private val imglauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                verified = true
                verifyBtn.visibility = View.INVISIBLE
                loginBtn.visibility = View.VISIBLE
                camView.setImageURI(tempUri)

                photoFile?.let { file ->
                    viewModel.updateProfileImage(currentUsername, file.absolutePath)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log)

        viewModel.init(applicationContext)
        viewModel.createDefaultUser()

        setupUI()
    }

    private fun setupUI() {
        val userField: EditText = findViewById(R.id.user)
        val passwordField: EditText = findViewById(R.id.password)

        loginBtn = findViewById(R.id.login)
        verifyBtn = findViewById(R.id.verButton)
        camView = findViewById(R.id.camcap)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResult.collect { result ->
                    when (result) {
                        is LogViewModel.LoginResult.Success -> {
                            takePic()
                            viewModel.resetLoginResult()
                        }
                        is LogViewModel.LoginResult.UserNotFound -> {
                            showToast("User not found")
                            viewModel.resetLoginResult()
                        }
                        is LogViewModel.LoginResult.IncorrectPassword -> {
                            showToast("Incorrect password")
                            viewModel.resetLoginResult()
                        }
                        null -> {
                            // Do nothing
                        }
                    }
                }
            }
        }

        verifyBtn.setOnClickListener {
            val name = userField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (name.isEmpty() || password.isEmpty()) {
                showToast("Field is required")
            } else {
                currentUsername = name
                viewModel.verifyUser(name, password)
            }
        }

        loginBtn.setOnClickListener {
            val user = viewModel.loggedInUser.value
            if (verified && user != null) {
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("userId", user.userId)
                }
                startActivity(intent)
            }
        }
    }


    private fun takePic() {
        photoFile = File.createTempFile("tmp_img_", ".jpg", cacheDir)

        tempUri = FileProvider.getUriForFile(
            this,
            "com.example.cardwallet.fileprovider",
            photoFile!!
        )

        grantUriPermission(
            "com.android.camera",
            tempUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        imglauncher.launch(tempUri!!)
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}