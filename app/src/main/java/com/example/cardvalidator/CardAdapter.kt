package com.example.cardvalidator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private var cards: List<CreditCard>
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    var previewCard: CreditCard? = null

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val holderName: TextView = view.findViewById(R.id.holderName)
        val expiry: TextView = view.findViewById(R.id.expiry)
        val cvv: TextView = view.findViewById(R.id.cvv)
        val brandLogo: ImageView = view.findViewById(R.id.brandLogo)

        val digitViews: List<TextView> = (1..16).map { i ->
            val id = view.context.resources.getIdentifier(
                "digit$i",
                "id",
                view.context.packageName
            )
            view.findViewById(id)
        }
    }

    override fun getItemCount(): Int {
        return cards.size + (if (previewCard != null) 1 else 0)
    }

    private fun getCard(position: Int): CreditCard? {
        return if (previewCard != null) {
            if (position == 0) previewCard
            else cards.getOrNull(position - 1)
        } else {
            cards.getOrNull(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_credit_card, parent, false)

        val width = (parent.resources.displayMetrics.widthPixels * 0.9).toInt()
        view.layoutParams = RecyclerView.LayoutParams(
            width,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )

        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        val card = getCard(position) ?: return

        holder.holderName.text =
            if (card.holderName.isBlank()) "YOUR NAME HERE"
            else card.holderName.uppercase()

        holder.expiry.text =
            card.expiry.ifBlank { "MM/YY" }

        holder.cvv.text =
            card.cvv.ifBlank { "***" }

        holder.digitViews.forEachIndexed { index, tv ->
            if (index < card.cardNumber.length) {
                tv.text = card.cardNumber[index].toString()
                tv.alpha = 1.0f
            } else {
                tv.text = "•"
                tv.alpha = 0.5f
            }
        }

        val brand = card.brandName?.let { name ->
            MainActivity.CardFlag.entries.find { it.name == name }
        }

        if (brand != null) {
            holder.brandLogo.setImageResource(brand.logoRes)
            holder.brandLogo.visibility = View.VISIBLE
        } else {
            holder.brandLogo.visibility = View.INVISIBLE
        }
    }

    fun updateCards(newCards: List<CreditCard>) {
        this.cards = newCards
        notifyDataSetChanged()
    }

    fun updatePreview(card: CreditCard?) {
        this.previewCard = card
        notifyDataSetChanged()
    }
}