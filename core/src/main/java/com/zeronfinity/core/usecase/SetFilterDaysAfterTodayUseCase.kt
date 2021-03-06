package com.zeronfinity.core.usecase

import com.zeronfinity.core.repository.FilterTimeRangeRepository
import com.zeronfinity.core.repository.FilterTimeRangeRepository.FilterTimeTypeEnum

class SetFilterDaysAfterTodayUseCase(private val filterTimeRangeRepository: FilterTimeRangeRepository) {
    operator fun invoke(filterTimeTypeEnum: FilterTimeTypeEnum, value: Int) =
        filterTimeRangeRepository.setDaysAfterToday(filterTimeTypeEnum, value)
}
