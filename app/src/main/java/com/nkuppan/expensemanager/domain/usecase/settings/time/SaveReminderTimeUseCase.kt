package com.nkuppan.expensemanager.domain.usecase.settings.time

import com.nkuppan.expensemanager.domain.model.ReminderTimeState
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.ReminderTimeRepository
import javax.inject.Inject

class SaveReminderTimeUseCase @Inject constructor(
    private val repository: ReminderTimeRepository
) {
    suspend operator fun invoke(reminderTimeState: ReminderTimeState): Resource<Boolean> {
        return Resource.Success(repository.saveReminderTime(reminderTimeState))
    }
}