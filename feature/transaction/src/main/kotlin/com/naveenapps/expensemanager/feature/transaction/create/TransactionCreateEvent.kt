package com.naveenapps.expensemanager.feature.transaction.create

sealed class TransactionCreateEvent {

    // Fired at most once per install, only after enough transactions have been logged and
    // enough time has passed since install (see FeedbackRepository). The Composable observes
    // this and triggers Google Play's in-app review popup, since that needs an Activity.
    data object RequestReview : TransactionCreateEvent()
}
