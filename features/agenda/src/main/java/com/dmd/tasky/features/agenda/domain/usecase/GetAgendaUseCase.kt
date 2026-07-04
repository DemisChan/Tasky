package com.dmd.tasky.features.agenda.domain.usecase

import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.domain.model.AgendaItem
import com.dmd.tasky.features.agenda.domain.repository.AgendaError
import com.dmd.tasky.features.agenda.domain.repository.AgendaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class GetAgendaUseCase @Inject constructor(
    private val repository: AgendaRepository
) {
    operator fun invoke(date: LocalDate): Flow<Result<List<AgendaItem>, AgendaError>> {
        return flow {
            emit(repository.getAgendaForDate(date))
        }
    }
}
