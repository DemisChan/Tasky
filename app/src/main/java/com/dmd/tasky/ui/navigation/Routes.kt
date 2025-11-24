package com.dmd.tasky.ui.navigation

import kotlinx.serialization.Serializable

sealed interface AuthGraphRoutes {
    @Serializable
    data object AuthGraph : AuthGraphRoutes

    @Serializable
    data object Login : AuthGraphRoutes

    @Serializable
    data object Register : AuthGraphRoutes
}

sealed interface AgendaGraphRoutes {

    @Serializable
    data object AgendaGraph : AgendaGraphRoutes

    @Serializable
    data object Agenda : AgendaGraphRoutes

}
