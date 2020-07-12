package org.itstep.liannoi.miler.infrastructure.presentation

interface ResourceRecognizer {
    fun recognize(name: String): Int
}
