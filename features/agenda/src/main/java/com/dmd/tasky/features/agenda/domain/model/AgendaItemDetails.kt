package com.dmd.tasky.features.agenda.domain.model

import java.time.LocalDateTime

/**
 * Sealed hierarchy containing ONLY the properties that differ between agenda item types.
 * Common properties (id, title, description, time, remindAt, updatedAt) live in AgendaItem.
 *
 * Design decision: Using composition over inheritance because:
 * - 95% of code works with common properties (display title, sort by time)
 * - Only detail screens need type-specific data
 * - Reminder has NO unique properties (data object elegantly expresses this)
 */
sealed interface AgendaItemDetails {

    /**
     * Event-specific properties.
     * Note: 'from' time is NOT here - it's the common 'time' property in AgendaItem.
     * Events are unique in having an END time (to).
     */
    data class Event(
        val to: LocalDateTime,
        val host: String,
        val isUserEventCreator: Boolean,
        val attendees: List<Attendee>,
        val photos: List<Photo>
    ) : AgendaItemDetails

    /**
     * Task-specific: only the completion status differs from base.
     */
    data class Task(
        val isDone: Boolean
    ) : AgendaItemDetails

    /**
     * Reminder has NO unique properties - it's essentially a Task without completion.
     * data object = singleton, no instances needed, just a type marker.
     */
    data object Reminder : AgendaItemDetails
}