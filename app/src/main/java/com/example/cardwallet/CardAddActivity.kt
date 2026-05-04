package com.example.cardwallet

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import java.util.Calendar
import androidx.core.graphics.toColorInt
import com.example.cardwallet.databinding.ActivityAddCardBinding
import com.example.cardwallet.viewmodel.CardAddViewModel
import data.CreditCard

@SuppressLint("SetTextI18n")
class CardAddActivity : AppCompatActivity() {

    private val viewModel: CardAddViewModel by viewModels()
    private lateinit var binding: ActivityAddCardBinding

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

        binding = ActivityAddCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityBase.logoutBtn.setOnClickListener {
            val intent = Intent(this, LogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.activityBase.backProfile.setOnClickListener {
            finish()
        }

        viewModel.init(applicationContext)

        val userId = intent.getLongExtra("userId", -1L)

        if (userId == -1L) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val cardnoinput = binding.cardnumber
        val nameInput = binding.namenter
        val cvvInput = binding.cvventer
        val expInput = binding.expenter
        val addSubmit = binding.addbtn

        val preview = findViewById<CardView>(R.id.creditCardView)
        val bankNameView = preview.findViewById<TextView>(R.id.bankname)
        val brandLogo = preview.findViewById<ImageView>(R.id.brandLogo)
        val holderNameView = preview.findViewById<TextView>(R.id.holderName)
        val expiryView = preview.findViewById<TextView>(R.id.expiry)
        val cvvView = preview.findViewById<TextView>(R.id.cvv)
        val cardNumberView = preview.findViewById<TextView>(R.id.cardNumber)

        fun updatePreview() {

            holderNameView.text =
                previewCard.holderName.uppercase().ifEmpty { "YOUR NAME HERE" }

            expiryView.text =
                previewCard.expiry.ifEmpty { "MM/YY" }

            cvvView.text =
                previewCard.cvv.ifEmpty { "CVV" }

            val raw = previewCard.cardNumber
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
            val upper = it.toString().uppercase()
            if (it.toString() != upper) {
                nameInput.setText(upper)
                nameInput.setSelection(upper.length)
            }

            previewCard = previewCard.copy(holderName = upper)
            updatePreview()
        }

        cvvInput.doAfterTextChanged {
            previewCard = previewCard.copy(cvv = it.toString())
            updatePreview()
        }
        expInput.setOnClickListener {

            val dialogView = layoutInflater.inflate(R.layout.dialog_expiry_picker, null)

            val monthDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.monthSpinner)
            val yearDropdown = dialogView.findViewById<AutoCompleteTextView>(R.id.yearSpinner)

            monthDropdown.keyListener = null
            yearDropdown.keyListener = null

            val months = (1..12).map { String.format("%02d", it) }
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val years = (0..15).map { (currentYear + it).toString().takeLast(2) }


            val monthAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                months
            )

            val yearAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                years
            )

            monthDropdown.setAdapter(monthAdapter)
            yearDropdown.setAdapter(yearAdapter)

            monthDropdown.setText(months[Calendar.getInstance().get(Calendar.MONTH)], false)
            yearDropdown.setText(years[0], false)


            AlertDialog.Builder(this)
                .setTitle("Select Expiry Date")
                .setView(dialogView)
                .setPositiveButton("OK") { _, _ ->
                    val month = monthDropdown.text.toString()
                    val year = yearDropdown.text.toString()
                    expInput.setText("$month/$year")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        expInput.doAfterTextChanged {
            previewCard = previewCard.copy(expiry = it.toString())
            updatePreview()
        }

        addSubmit.setOnClickListener {


            val cardNumber = cardnoinput.text.toString().replace(" ", "")
            val name = nameInput.text.toString().trim().uppercase()
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

            addSubmit.isEnabled = false

            val brand = CardFlag.entries.find {
                cardNumber.startsWith(it.prefix)
            }

            val newCard = CreditCard(
                cardNumber = cardNumber,
                holderName = name,
                expiry = expiry,
                cvv = cvv,
                brandName = brand?.name,
                userOwnerId = userId
            )

            viewModel.insertCard(
                card = newCard,
                onSuccess = {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(addSubmit.windowToken, 0)

                    Toast.makeText(
                        this@CardAddActivity,
                        "Card Saved to Wallet",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                },
                onError = { e ->
                    Toast.makeText(
                        this@CardAddActivity,
                        "Failed to save card",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                    addSubmit.isEnabled = true
                }
            )
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

