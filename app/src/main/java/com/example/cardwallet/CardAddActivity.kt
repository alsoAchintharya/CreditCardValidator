package com.example.cardwallet

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cardwallet.databinding.ActivityAddCardBinding
import com.example.cardwallet.viewmodel.CardAddViewModel
import kotlinx.coroutines.launch
import com.example.cardwallet.viewmodel.CardFormState
import androidx.core.graphics.toColorInt
import data.CreditCard

@SuppressLint("SetTextI18n")
class CardAddActivity : AppCompatActivity() {

    private val viewModel: CardAddViewModel by viewModels()
    private lateinit var binding: ActivityAddCardBinding

    enum class CardFlag(val logoRes: Int, val prefix: String) {
        VISA(R.drawable.ic_visa, "4"),
        MASTERCARD(R.drawable.ic_mastercard, "5"),
        DISCOVER(R.drawable.ic_discover, "6"),
        AMEX(R.drawable.ic_amex, "3")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        fun render(state: CardFormState) {

            holderNameView.text =
                state.holderName.uppercase().ifEmpty { "YOUR NAME HERE" }

            expiryView.text =
                state.expiry.ifEmpty { "MM/YY" }

            cvvView.text =
                state.cvv.ifEmpty { "CVV" }

            val formatted = state.cardNumber.chunked(4).joinToString(" ")

            cardNumberView.text = formatted.ifEmpty {
                "•••• •••• •••• ••••"
            }

            val brand = CardFlag.entries.find {
                state.cardNumber.startsWith(it.prefix)
            }

            if (brand != null) {
                brandLogo.setImageResource(brand.logoRes)
            } else {
                brandLogo.setImageDrawable(null)
            }

            bankNameView.text = brand?.name ?: "BANK NAME HERE"
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.formState.collect { render(it) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.insertEvent.collect { event ->
                    when (event) {

                        is CardAddViewModel.InsertEvent.Success -> {
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.hideSoftInputFromWindow(addSubmit.windowToken, 0)

                            Toast.makeText(this@CardAddActivity,
                                "Card Saved to Wallet",
                                Toast.LENGTH_SHORT
                            ).show()

                            viewModel.clearEvent()
                            finish()
                        }

                        is CardAddViewModel.InsertEvent.Error -> {
                            Toast.makeText(this@CardAddActivity,
                                "Failed to save card",
                                Toast.LENGTH_SHORT
                            ).show()

                            addSubmit.isEnabled = true
                            viewModel.clearEvent()
                        }

                        null -> Unit
                    }
                }
            }
        }


        cardnoinput.doAfterTextChanged {
            val input = it.toString().replace(" ", "")

            val brand = CardFlag.entries.find { flag ->
                input.startsWith(flag.prefix)
            }

            viewModel.updateCardNumber(input, brand?.name)

            val color = when {
                input.isEmpty() -> Color.BLACK
                input.length in 13..19 && isValidLuhn(input) -> "#2ECC71".toColorInt()
                else -> "#E74C3C".toColorInt()
            }

            cardnoinput.setTextColor(color)
        }

        nameInput.doAfterTextChanged {
            val upper = it.toString().uppercase()
            if (it.toString() != upper) {
                nameInput.setText(upper)
                nameInput.setSelection(upper.length)
            }
            viewModel.updateHolderName(upper)
        }

        cvvInput.doAfterTextChanged {
            viewModel.updateCvv(it.toString())
        }

        expInput.doAfterTextChanged {
            viewModel.updateExpiry(it.toString())
        }


        addSubmit.setOnClickListener {

            val state = viewModel.formState.value

            val cardNumber = state.cardNumber
            val name = state.holderName.trim().uppercase()
            val expiry = state.expiry.trim()
            val cvv = state.cvv.trim()

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

            viewModel.insertCard(newCard)
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