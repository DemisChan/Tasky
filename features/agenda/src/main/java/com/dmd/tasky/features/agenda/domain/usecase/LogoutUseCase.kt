package com.dmd.tasky.features.agenda.domain.usecase

import com.dmd.tasky.features.agenda.domain.repository.AgendaRepository
import com.dmd.tasky.features.agenda.domain.repository.LogoutResult
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    suspend operator fun invoke(): LogoutResult {
        return repository.logout()

    }

}