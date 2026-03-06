package com.ahanam05.galleon.data.models

import com.google.firebase.firestore.DocumentId

data class Budget(
    @DocumentId
    val monthlyBudget: Double = 0.0,
    val monthYear: String = "", // Format: "2025-01" for January 2025
)
