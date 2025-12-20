package org.igorv8836.rentcontrol

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform