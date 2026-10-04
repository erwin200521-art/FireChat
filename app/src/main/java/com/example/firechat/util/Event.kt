package com.example.firechat.util

/** Envuelve un valor que debe consumirse una sola vez (errores, navegación). */
class Event<out T>(private val content: T) {
    private var handled = false

    fun getContentIfNotHandled(): T? =
        if (handled) null else { handled = true; content }
}
