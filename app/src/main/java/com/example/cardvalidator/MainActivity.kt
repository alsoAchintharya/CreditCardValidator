package com.example.cardvalidator

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var adapter: CardAdapter

    enum class CardFlag(val logoRes: Int, val prefix: String) {
        VISA(R.drawable.ic_visa, "4"),
        MASTERCARD(R.drawable.ic_mastercard, "5"),
        DISCOVER(R.drawable.ic_discover, "6"),
        AMEX(R.drawable.ic_amex, "3")
    }

    private var previewCard = CreditCard(
        cardNumber = "",
        holderName = "",
        expiry = "",
        cvv = "",
        brandName = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)
        adapter = CardAdapter(emptyList())

        val recyclerView = findViewById<RecyclerView>(R.id.cardRecyclerView)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        PagerSnapHelper().attachToRecyclerView(recyclerView)

        val cardnoinput = findViewById<EditText>(R.id.cardnumber)
        val nameInput = findViewById<EditText>(R.id.namenter)
        val cvvInput = findViewById<EditText>(R.id.cvventer)
        val expInput = findViewById<EditText>(R.id.expenter)
        val addSubmit = findViewById<Button>(R.id.addbtn)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            database.cardDao().getAllCards().collect { cards ->
                adapter.updateCards(cards)
            }
        }

        fun pushPreview() {
            adapter.updateCards(listOf(previewCard))
        }

        cardnoinput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().replace(" ", "")

                val brand = CardFlag.entries.find {
                    input.startsWith(it.prefix)
                }

                previewCard = previewCard.copy(
                    cardNumber = input,
                    brandName = brand?.name
                )

                pushPreview()

                if (input.length >= 13 && isValidLuhn(input)) {
                    cardnoinput.setTextColor(Color.GREEN)
                } else {
                    cardnoinput.setTextColor(Color.BLACK)
                }
            }
        })

        nameInput.addTextChangedListener {
            previewCard = previewCard.copy(holderName = it.toString())
            pushPreview()
        }

        cvvInput.addTextChangedListener {
            previewCard = previewCard.copy(cvv = it.toString())
            pushPreview()
        }

        expInput.addTextChangedListener {
            previewCard = previewCard.copy(expiry = it.toString())
            pushPreview()
        }

        expInput.setOnClickListener {
            val c = Calendar.getInstance()
            android.app.DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog,
                { _, y, m, _ ->
                    expInput.setText(String.format("%02d/%02d", m + 1, y % 100))
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                1
            ).show()
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
                brandName = brand?.name
            )

            lifecycleScope.launch {
                database.cardDao().insert(newCard)

                previewCard = CreditCard("", "", "", "", null)
                pushPreview()

                cardnoinput.text.clear()
                nameInput.text.clear()
                expInput.text.clear()
                cvvInput.text.clear()

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(addSubmit.windowToken, 0)

                Toast.makeText(
                    this@MainActivity,
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