package com.nkuppan.expensemanager.domain.usecase.settings.filter

import com.nkuppan.expensemanager.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSelectedFilterNameAndDateRangeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val getFilterTypeUseCase: GetFilterTypeUseCase,
    private val getFilterRangeDateStringUseCase: GetFilterRangeDateStringUseCase,
) {

    operator fun invoke(): Flow<String> {
        return getFilterTypeUseCase.invoke().map {
            val filterRangeText = getFilterRangeDateStringUseCase.invoke(it)
            val filterNameText = settingsRepository.getFilterRangeValue(it)
            return@map "${filterNameText}(${filterRangeText})"
        }
    }
}