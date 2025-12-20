package org.igorv8836.rentcontrol

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RentControl",
    ) {
        App()
    }
}