package com.ahanam05.galleon.data.models

import com.google.firebase.firestore.DocumentId

data class Expense(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Long = 0L
)
