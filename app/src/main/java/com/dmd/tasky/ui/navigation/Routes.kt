package com.dmd.tasky.ui.navigation

import kotlinx.serialization.Serializable


@Serializable
data object AuthGraph

@Serializable
data object AgendaGraph

sealed interface AuthRoute {
    @Serializable
    data object Login : AuthRoute

    @Serializable
    data object Register : AuthRoute
}

sealed interface AgendaRoute {

    @Serializable
    data object Agenda : AgendaRoute

}
