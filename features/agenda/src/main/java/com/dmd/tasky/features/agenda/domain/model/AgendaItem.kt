package com.dmd.tasky.features.agenda.domain.model

import java.time.LocalDateTime

/**
 * Unified agenda item using COMPOSITION pattern.
 *
 * Before (inheritance):
 *   sealed interface AgendaItem {
 *       data class Event(override val id, override val title, ..., val attendees) : AgendaItem
 *       data class Task(override val id, override val title, ..., val isDone) : AgendaItem
 *   }
 *   Problem: Repeated "override val" for every common property in every subtype.
 *
 * After (composition):
 *   data class AgendaItem(val id, val title, ..., val details: AgendaItemDetails)
 *   Benefit: Common properties defined ONCE. Type-specific in 'details'.
 *
 * Usage:
 *   // Common access - no type check needed
 *   items.sortedBy { it.time }
 *   Text(item.title)
 *
 *   // Type-specific access
 *   when (item.details) {
 *       is AgendaItemDetails.Event -> item.details.attendees
 *       is AgendaItemDetails.Task -> item.details.isDone
 *       is AgendaItemDetails.Reminder -> { }
 *   }
 */
data class AgendaItem(
    val id: String,
    val title: String,
    val description: String?,
    val time: LocalDateTime,          // For Events: this is 'from'. For Task/Reminder: the scheduled time.
    val remindAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val details: AgendaItemDetails    // Type-specific properties live here
)
