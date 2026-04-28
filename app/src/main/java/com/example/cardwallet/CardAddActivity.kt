package com.example.cardwallet

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.core.graphics.toColorInt
import data.AppDatabase
import data.CreditCard

@SuppressLint("SetTextI18n")
class CardAddActivity : BaseActivity() {

    private lateinit var database: AppDatabase

    private var previewCard = CreditCard(
        id = 0,
        cardNumber = "",
        holderName = "",
        expiry = "",
        cvv = "",
        brandName = null,
        userOwnerId = 0,
    )

    enum class CardFlag(val logoRes: Int, val prefix: String) {
        VISA(R.drawable.ic_visa, "4"),
        MASTERCARD(R.drawable.ic_mastercard, "5"),
        DISCOVER(R.drawable.ic_discover, "6"),
        AMEX(R.drawable.ic_amex, "3")
    }

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_card)

        database = AppDatabase.getDatabase(this)

        val cardnoinput = findViewById<EditText>(R.id.cardnumber)
        val nameInput = findViewById<EditText>(R.id.namenter)
        val cvvInput = findViewById<EditText>(R.id.cvventer)
        val expInput = findViewById<EditText>(R.id.expenter)
        val addSubmit = findViewById<Button>(R.id.addbtn)

        val preview = findViewById<CardView>(R.id.creditCardView)

        val bankNameView = preview.findViewById<TextView>(R.id.bankname)
        val brandLogo = preview.findViewById<ImageView>(R.id.brandLogo)

        val holderNameView = preview.findViewById<TextView>(R.id.holderName)
        val expiryView = preview.findViewById<TextView>(R.id.expiry)
        val cvvView = preview.findViewById<TextView>(R.id.cvv)
        val cardNumberView = preview.findViewById<TextView>(R.id.cardNumber)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fun updatePreview() {

            holderNameView.text =
                previewCard.holderName.ifEmpty { "YOUR NAME HERE" }

            expiryView.text =
                previewCard.expiry.ifEmpty { "MM/YY" }

            cvvView.text =
                previewCard.cvv.ifEmpty { "CVV" }

            val raw = previewCard.cardNumber
            val masked = raw.map { '•' }.joinToString("")
            val formatted = raw.chunked(4).joinToString(" ")

            cardNumberView.text = formatted.ifEmpty {
                "•••• •••• •••• ••••"
            }

            val brand = CardFlag.entries.find {
                previewCard.cardNumber.startsWith(it.prefix)
            }

            if (brand != null) {
                brandLogo.setImageResource(brand.logoRes)
            } else {
                brandLogo.setImageDrawable(null)
            }

            bankNameView.text = brand?.name ?: "BANK NAME HERE"
        }

        cardnoinput.doAfterTextChanged {
            val input = it.toString().replace(" ", "")

            val brand = CardFlag.entries.find { flag ->
                input.startsWith(flag.prefix)
            }

            previewCard = previewCard.copy(
                cardNumber = input,
                brandName = brand?.name
            )

            updatePreview()

            val color = when {
                input.isEmpty() -> Color.BLACK

                input.length in 13..19 && isValidLuhn(input) ->
                    "#2ECC71".toColorInt()

                else ->
                    "#E74C3C".toColorInt()
            }

            cardnoinput.setTextColor(color)
        }

        nameInput.doAfterTextChanged {
            previewCard = previewCard.copy(holderName = it.toString())
            updatePreview()
        }

        cvvInput.doAfterTextChanged {
            previewCard = previewCard.copy(cvv = it.toString())
            updatePreview()
        }

        expInput.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog,
                { _, y, m, _ ->
                    val exp = String.format(
                        "%02d/%02d",
                        m + 1,
                        y.toString().takeLast(2).toInt()
                    )
                    expInput.setText(exp)
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                1
            ).show()
        }

        expInput.doAfterTextChanged {
            previewCard = previewCard.copy(expiry = it.toString())
            updatePreview()
        }

        addSubmit.setOnClickListener {

            val cardNumber = cardnoinput.text.toString().replace(" ", "")
            val name = nameInput.text.toString().trim()
            val expiry = expInput.text.toString().trim()
            val cvv = cvvInput.text.toString().trim()

            when {
                cardNumber.length < 13 || !isValidLuhn(cardNumber) -> {
                    cardnoinput.error = "Invalid Card Number"
                    return@setOnClickListener
                }
                name.isEmpty() -> {
                    nameInput.error = "Name Required"
                    return@setOnClickListener
                }
                expiry.isEmpty() -> {
                    expInput.error = "Expiry Required"
                    return@setOnClickListener
                }
                cvv.length < 3 -> {
                    cvvInput.error = "Invalid CVV"
                    return@setOnClickListener
                }
            }

            val brand = CardFlag.entries.find {
                cardNumber.startsWith(it.prefix)
            }

            val newCard = CreditCard(
                cardNumber = cardNumber,
                holderName = name,
                expiry = expiry,
                cvv = cvv,
                brandName = brand?.name,
                userOwnerId = 0
            )

            lifecycleScope.launch {
                database.cardDao().insert(newCard)

                cardnoinput.text.clear()
                nameInput.text.clear()
                expInput.text.clear()
                cvvInput.text.clear()

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(addSubmit.windowToken, 0)

                Toast.makeText(
                    this@CardAddActivity,
                    "Card Saved to Wallet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isValidLuhn(cardno: String): Boolean {
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
}