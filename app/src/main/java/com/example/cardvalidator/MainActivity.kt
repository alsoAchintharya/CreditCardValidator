package com.example.cardvalidator

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.toColorInt
import coil.load
import java.util.Calendar

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var digitViews: List<TextView>

    enum class CardFlag(val logoRes: Int, val color: Int, val regex: String) {
        VISA(R.drawable.ic_visa, 0xFF191278.toInt(), "^4.*"),
        MASTERCARD(R.drawable.ic_mastercard, 0xFF0061A8.toInt(), "^(5[1-5]|2(22[1-9]|2[3-9]|[3-6]|7[0-1]|720)).*"),
        DISCOVER(R.drawable.ic_discover, 0xFF86B8CF.toInt(), "^6(?:011|5|4[4-9]|22).*"),
        AMEX(R.drawable.ic_amex, 0xFF108168.toInt(), "^3[47].*")
    }



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
        val cardBackground = findViewById<View>(R.id.creditCardView)
        val brandLogoDisplay = findViewById<ImageView>(R.id.brandLogo)

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


        cardnoinput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().replace(" ", "")
                displayCardNumber(input)

                val brand = CardFlag.entries.find {
                    input.isNotEmpty() && input.matches(it.regex.toRegex())
                }

                if (brand != null) {
                    brandLogoDisplay.visibility = View.VISIBLE
                    brandLogoDisplay.load(brand.logoRes) {
                        crossfade(true)
                        allowHardware(true)
                    }

                    cardBackground.setBackgroundColor(brand.color)
                } else {
                    brandLogoDisplay.visibility = View.GONE
                    cardBackground.setBackgroundColor(0xFF2D2D2D.toInt())
                }


                if (input.length == 16) {
                    val isValid = isValidLuhn(input)
                    cardnoinput.setTextColor(if (isValid) Color.GREEN else Color.RED)
                    if (!isValid) Toast.makeText(applicationContext, "Invalid card number", Toast.LENGTH_SHORT).show()
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


        expInput.setOnClickListener {
            val c = Calendar.getInstance()
            val picker = android.app.DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog,
                { _, y, m, _ -> expInput.setText(String.format("%02d/%02d", m + 1, y % 100)) },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1
            )
            val dayId = resources.getIdentifier("day", "id", "android")
            if (dayId != 0) picker.datePicker.findViewById<View>(dayId)?.visibility = View.GONE
            picker.show()
        }

        expInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                displayExp(s.toString(), expDisplay)
            }
        })
    }

    private fun displayCardNumber(cardno: String) {
        digitViews.forEachIndexed { index, tv ->
            if (index < cardno.length) {
                tv.text = cardno[index].toString()
                tv.setTextColor(Color.WHITE)
                tv.alpha = 1.0f
            } else {
                tv.text = "•"
                tv.setTextColor("#FFD700".toColorInt())
                tv.alpha = 0.5f
            }
        }
    }

    private fun displayCardName(name: String, display: TextView) {
        display.text = if (name.isEmpty()) "YOUR NAME HERE" else name.uppercase()
        display.alpha = if (name.isEmpty()) 0.5f else 1.0f
    }

    private fun displayCVV(cvv: String, display: TextView) {
        display.text = if (cvv.isEmpty()) "CVV" else cvv
        display.alpha = if (cvv.isEmpty()) 0.5f else 1.0f
    }

    private fun displayExp(expiry: String, display: TextView) {
        display.text = if (expiry.isEmpty()) "MM/YY" else expiry
        display.alpha = if (expiry.isEmpty()) 0.5f else 1.0f
    }
}

fun isValidLuhn(cardno: String): Boolean {
    if (cardno.length < 2) return false
    var sum = 0
    var isAlt = false
    for (i in cardno.length - 1 downTo 0) {
        var d = cardno[i].digitToInt()
        if (isAlt) {
            d *= 2
            if (d > 9) d -= 9
        }
        sum += d
        isAlt = !isAlt
    }
    return sum % 10 == 0
}
