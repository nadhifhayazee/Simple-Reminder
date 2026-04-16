package com.nadhifhayazee.simplereminder.domain.usecase

import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import javax.inject.Inject

class GetReminderByIdUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(id: Int): Reminder? {
        return repository.getReminderById(id)
    }
}
