<h1 align="center">Card Wallet App</h1>

<p align="center">
  A lightweight Android application for securely managing and organizing digital cards with offline-first support.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Mobile-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/UI-Minimal-black?style=for-the-badge"/>
</p>

---

##  UI/UX Features

-  Offline-first data storage with Room Database
-  Basic authentication flow with secure local handling
-  User profile management
-  Add, view, and manage cards
-  Clean and modern UI
-  Fast and lightweight experience

---

## Application Architecture Overview

The CardWallet app follows MVVM architecture with reactive state management using Kotlin Coroutines and StateFlow.
Each module is independent and communicates via ViewModel-driven state updates.

## Tech Stack

- Kotlin
- Android Jetpack (ViewModel, Lifecycle)
- Coroutines + StateFlow
- RecyclerView
- View Binding
- FileProvider

## App Navigation Flow

Login → Profile → Card List → Add Card → Card Details Actions

## App Screens

<p align="center">
  A quick look at the app interface
</p>

<br/>
<table align="center">
  <tr>
    <td align="center" style="padding: 20px; width: 300px;">
      <br>
      <img src="https://github.com/user-attachments/assets/8b7410a6-b944-42bf-a43d-5805c5c354a0" width="220"/><br/><br/>
      <b>Login Module</b><br/>
      <sub><i>Secure authentication and identity verification system.</i></sub><br/><br/>
      <div align="left">
<b>Architecture</b><br/>
<sub>MVVM with StateFlow and lifecycle-aware coroutines.</sub><br/><br/>

<b>Features</b><br/>
<sub>
• Credential validation<br/>
• Camera-based verification<br/>
• Secure image storage<br/>
• Profile persistence<br/>
• Navigation after verification
</sub><br/><br/>

<b>Technologies</b><br/>
<sub>Kotlin • ViewModel • Coroutines • Activity Result API • FileProvider</sub>
      </div>
    </td>
    <td align="center" style="padding: 20px; width: 300px;">
      <img src="https://github.com/user-attachments/assets/4bbe4271-36e4-4c49-ab76-a5eb28a90721" width="220"/><br/><br/>
      <b>Profile Module</b><br/>
      <sub><i>Main dashboard displaying user profile and saved cards.</i></sub><br/><br/>
      <div align="left">
<b>Architecture</b><br/>
<sub>MVVM with reactive UI updates using StateFlow.</sub><br/><br/>
<b>Features</b><br/>
<sub>
• User profile loading<br/>
• Profile image display<br/>
• Saved card statistics<br/>
• Secure logout handling<br/>
• Navigation to card management
</sub><br/><br/>
<b>Technologies</b><br/>
<sub>Kotlin • View Binding • ViewModel • Coroutines • StateFlow</sub>
      </div>
    </td>
  </tr>
  <tr>
    <td align="center" style="padding: 20px; width: 300px;">
      <br/>
      <img src="https://github.com/user-attachments/assets/48d37e1f-98c3-423e-aa19-b3ffd75106aa" width="220"/><br/><br/>
      <b>Card Management Module</b><br/>
      <sub><i>Manage saved cards with smooth scrolling and deletion support.</i></sub><br/><br/>
      <div align="left">
<b>Architecture</b><br/>
<sub>MVVM with RecyclerView and reactive state handling.</sub><br/><br/>
<b>Features</b><br/>
<sub>
• Horizontal card carousel<br/>
• Add new cards<br/>
• Delete confirmation dialog<br/>
• Reactive UI rendering<br/>
• Navigation between screens
</sub><br/><br/>
<b>Technologies</b><br/>
<sub>Kotlin • RecyclerView • ViewModel • Coroutines • AlertDialog</sub>
        <br/>
      </div>
    </td>
    <td align="center" style="padding: 20px; width: 300px;">
      <br/>
      <img src="https://github.com/user-attachments/assets/89ab34bc-5665-4760-a8a7-e96d49c5a9ca" width="220"/><br/><br/>
      <b>Card Creation Module</b><br/>
      <sub><i>Interactive card creation with validation and live preview.</i></sub><br/><br/>
      <div align="left">
<b>Architecture</b><br/>
<sub>MVVM with reactive form validation using StateFlow.</sub><br/><br/>
<b>Features</b><br/>
<sub>
• Live card preview<br/>
• Card type detection<br/>
• Luhn validation<br/>
• Dynamic formatting<br/>
• Secure local persistence
</sub><br/><br/>
<b>Technologies</b><br/>
<sub>Kotlin • MVVM • StateFlow • Coroutines • Luhn Algorithm</sub>
        <br/>
      </div>
    </td>
  </tr>
</table>

# Core Implementation Highlights

This section highlights the core engineering logic behind the **Card Wallet App**, focusing on validation, reactive architecture, lifecycle safety, and secure Android practices.

---

## Card Validation (Luhn Algorithm)

Used in `CardAddActivity` to validate credit/debit card numbers before storage.

```kotlin
private fun isValidLuhn(cardno: String): Boolean {
    var sum = 0
    var isAlt = false

    for (i in cardno.length - 1 downTo 0) {
        var digit = cardno[i].digitToInt()

        if (isAlt) {
            digit *= 2
            if (digit > 9) digit -= 9
        }

        sum += digit
        isAlt = !isAlt
    }

    return sum % 10 == 0
}
```

### Purpose
*   Prevents invalid card entry.
*   Ensures structural integrity before storage.
*   Implements industry-standard credit card validation algorithm.

---

## Reactive Form State Management (MVVM + StateFlow)

Used in `CardAddActivity` for real-time UI updates via ViewModel.

```kotlin
viewModel.formState.collect { state ->
    holderNameView.text =
        state.holderName.uppercase().ifEmpty { "YOUR NAME HERE" }

    expiryView.text =
        state.expiry.ifEmpty { "MM/YY" }

    cvvView.text =
        state.cvv.ifEmpty { "CVV" }

    cardNumberView.text =
        state.cardNumber.chunked(4).joinToString(" ")
            .ifEmpty { "•••• •••• •••• ••••" }
}
```

### Purpose
*   Enables reactive UI updates.
*   Maintains single source of truth via ViewModel.

---

## Secure Image Capture Flow (FileProvider)

Used in `LogActivity` for identity verification.

```kotlin
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
```

### Purpose
*   Secure temporary image handling.
*   Prevents direct file exposure.
*   Uses Android FileProvider best practices.

---

## Lifecycle-aware Reactive Collection

Used in `ProfileActivity` and `CardListActivity`.

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.cards.collect { cards ->
            adapter.updateCards(cards)
        }

        viewModel.user.collect { user ->
            profileUserName.text =
                user?.username?.uppercase() ?: "GUEST"
        }
    }
}
```

### Purpose
*   Prevents memory leaks.
*   Ensures lifecycle-safe UI updates.
*   Efficient Flow collection pattern.

---

## Card Brand Detection

Used in `CardAddActivity` for automatic card type recognition.

```kotlin
val brand = CardFlag.entries.find {
    state.cardNumber.startsWith(it.prefix)
}

if (brand != null) {
    brandLogo.setImageResource(brand.logoRes)
} else {
    brandLogo.setImageDrawable(null)
}
```

### Purpose
*   Detects card type (Visa, MasterCard, etc.).
*   Improves UX with visual feedback.
