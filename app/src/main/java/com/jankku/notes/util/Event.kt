package com.jankku.notes.util

sealed class Event {
    data class NavigateUp(val shouldNavigate: Boolean) : Event()
}
