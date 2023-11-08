package com.naveenapps.expensemanager.domain.usecase.settings.time

import com.naveenapps.expensemanager.domain.model.ReminderTimeState
import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.repository.ReminderTimeRepository
import javax.inject.Inject

class SaveReminderTimeUseCase @Inject constructor(
    private val repository: ReminderTimeRepository
) {
    suspend operator fun invoke(reminderTimeState: ReminderTimeState): Resource<Boolean> {
        return Resource.Success(repository.saveReminderTime(reminderTimeState))
    }
}