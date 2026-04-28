package com.example.activitynavigation

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.File

class LogActivity : AppCompatActivity() {

    private lateinit var userManager: UserManager
    private lateinit var camView: ImageView
    private lateinit var behavior: BottomSheetBehavior<View>

    private var tempUri: Uri? = null
    private var photoFile: File? = null
    private var verified = false

    private lateinit var loginBtn: Button
    private lateinit var verifyBtn: Button

    private val imglauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            verified = true
            verifyBtn.visibility = View.INVISIBLE
            loginBtn.visibility = View.VISIBLE
            camView.setImageURI(tempUri)
            val username = findViewById<EditText>(R.id.user).text.toString().trim()
            photoFile?.let { userManager.saveProfileImage(it, username) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets}


        userManager = UserManager(this)
        handleDefaultUser()
        setupUI()
    }

    private fun setupUI() {
        val userField: EditText = findViewById(R.id.user)
        loginBtn = findViewById(R.id.login)
        verifyBtn = findViewById(R.id.verButton)
        val toggleSheetBtn: Button = findViewById(R.id.createUser)
        val mainLayout: CoordinatorLayout = findViewById(R.id.main)
        camView = findViewById(R.id.camcap)

        // Bottom Sheet Setup
        val bottomSheetView = findViewById<View>(R.id.persistent_bottom_sheet)
        behavior = BottomSheetBehavior.from(bottomSheetView)
        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        verifyBtn.setOnClickListener {
            val name = userField.text.toString().trim()
            if (name.isEmpty()) {
                showToast("Field is required")
            } else if (userManager.isUserExists(name)) {
                takePic()

            } else {
                showToast("User not found")
            }
        }

        loginBtn.setOnClickListener {
            if (verified) {
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("username", userField.text.toString())
                }
                startActivity(intent)
            }
        }

        toggleSheetBtn.setOnClickListener {
            if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                animateSheetPeek()
            } else {
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        findViewById<Button>(R.id.add).setOnClickListener { handleRegistration() }
        mainLayout.setOnClickListener { behavior.state = BottomSheetBehavior.STATE_HIDDEN }
    }

    private fun handleRegistration() {
        val name = findViewById<EditText>(R.id.newUser).text.toString().trim()
        val dob = findViewById<EditText>(R.id.newDob).text.toString().trim()
        val phone = findViewById<EditText>(R.id.newPhone).text.toString().trim()
        val addr = findViewById<EditText>(R.id.newAddress).text.toString().trim()

        if (name.isEmpty() || dob.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
            showToast("All fields required")
            return
        }

        if (userManager.isUserExists(name)) {
            showToast("Username already exists")
        } else {
            userManager.createUser(name, dob, phone, addr)
            showToast("Welcome, $name!")
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            clearInputs()
        }
    }

    private fun takePic() {
        photoFile = File.createTempFile("tmp_img", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        tempUri = FileProvider.getUriForFile(this, "com.example.activitynavigation.fileprovider", photoFile!!)
        imglauncher.launch(tempUri)
    }

    private fun handleDefaultUser() {
        if (!userManager.isUserExists("achintharya")) {
            userManager.createUser("achintharya", "02-07-2005", "9160023473", "Bengaluru")
        }
    }

    private fun animateSheetPeek() {
        ValueAnimator.ofInt(0, 350).apply {
            addUpdateListener { behavior.peekHeight = it.animatedValue as Int }
            duration = 1000 // Reduced for better feel
            start()
        }
    }

    private fun clearInputs() {
        val ids = listOf(R.id.newUser, R.id.newDob, R.id.newPhone, R.id.newAddress)
        ids.forEach { id ->
            findViewById<EditText>(id)?.text?.clear()
        }
    }


    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
