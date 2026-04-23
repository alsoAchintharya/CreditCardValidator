package com.example.cardvalidator

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import java.util.Calendar

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var digitViews: List<TextView>

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cardnoinput = findViewById<EditText>(R.id.cardnumber)
        val nameInput = findViewById<EditText>(R.id.namenter)
        val cvvInput = findViewById<EditText>(R.id.cvventer)
        val expInput = findViewById<EditText>(R.id.expenter)

        val holderNameDisplay = findViewById<TextView>(R.id.holderName)
        val cvvDisplay = findViewById<TextView>(R.id.cvv)
        val expDisplay = findViewById<TextView>(R.id.expiry)

        digitViews = listOf(
            findViewById(R.id.digit1), findViewById(R.id.digit2),
            findViewById(R.id.digit3), findViewById(R.id.digit4),
            findViewById(R.id.digit5), findViewById(R.id.digit6),
            findViewById(R.id.digit7), findViewById(R.id.digit8),
            findViewById(R.id.digit9), findViewById(R.id.digit10),
            findViewById(R.id.digit11), findViewById(R.id.digit12),
            findViewById(R.id.digit13), findViewById(R.id.digit14),
            findViewById(R.id.digit15), findViewById(R.id.digit16)
        )

        displayCardNumber("")

        // --- Card Number Watcher ---
        cardnoinput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().replace(" ", "")
                displayCardNumber(input)

                if (input.length == 16) {
                    if (isValidLuhn(input)) {
                        cardnoinput.setTextColor(Color.GREEN)
                    } else {
                        cardnoinput.setTextColor(Color.RED)
                        Toast.makeText(applicationContext, "Invalid card number", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cardnoinput.setTextColor(Color.BLACK)
                }
            }
        })


        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                displayCardName(s.toString(), holderNameDisplay)
            }
        })


        cvvInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                displayCVV(s.toString(), cvvDisplay)
            }
        })


        expInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                displayExp(s.toString(), expDisplay)
            }
        })


        expInput.setOnClickListener {
            val c = Calendar.getInstance()
            val currentYear = c.get(Calendar.YEAR)
            val currentMonth = c.get(Calendar.MONTH)

            val picker = android.app.DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog,
                { _, selectedYear, selectedMonth, _ ->
                    val formattedDate = String.format("%02d/%02d", selectedMonth + 1, selectedYear % 100)
                    expInput.setText(formattedDate)
                }, currentYear, currentMonth, 1
            )


            val daySpinnerId = resources.getIdentifier("day", "id", "android")
            //val daySpinnerId = resources.android.R.id.day
            if (daySpinnerId != 0) {
                val daySpinner = picker.datePicker.findViewById<View>(daySpinnerId)
                daySpinner?.visibility = View.GONE
            }


            picker.datePicker.minDate = System.currentTimeMillis()
            val maxCalendar = Calendar.getInstance()
            maxCalendar.add(Calendar.YEAR, 20)
            picker.datePicker.maxDate = maxCalendar.timeInMillis

            picker.window?.setBackgroundDrawableResource(android.R.color.transparent)
            picker.show()
        }
    }

    private fun displayCardNumber(cardno: String) {
        digitViews.forEachIndexed { index, textView ->
            if (index < cardno.length) {
                textView.text = cardno[index].toString()
                textView.setTextColor(Color.WHITE)
                textView.alpha = 1.0f
            } else {
                textView.text = "•"
                textView.setTextColor("#FFD700".toColorInt())
                textView.alpha = 0.5f
            }
        }
    }

    private fun displayCardName(name: String, display: TextView) {
        if (name.isEmpty()) {
            display.text = "YOUR NAME HERE"
            display.alpha = 0.5f
        } else {
            display.text = name.uppercase()
            display.alpha = 1.0f
        }
    }

    private fun displayCVV(cvv: String, display: TextView) {
        if (cvv.isEmpty()) {
            display.text = "CVV"
            display.alpha = 0.5f
        } else {
            display.text = cvv
            display.alpha = 1.0f
        }
    }

    private fun displayExp(expiry: String, display: TextView) {
        if (expiry.isEmpty()) {
            display.text = "MM/YY"
            display.alpha = 0.5f
        } else {
            display.text = expiry
            display.alpha = 1.0f
        }
    }
}

fun isValidLuhn(cardno: String): Boolean {
    if (cardno.length < 2) return false
    var sum = 0
    var isalt = false
    val digits = cardno.replace(" ", "").reversed()
    for (char in digits) {
        if (!char.isDigit()) continue
        var d = char.digitToInt()
        if (isalt) {
            d *= 2
            if (d > 9) d -= 9
        }
        sum += d
        isalt = !isalt
    }
    return sum % 10 == 0
}
