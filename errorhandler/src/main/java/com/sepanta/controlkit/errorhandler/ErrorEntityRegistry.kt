package com.sepanta.controlkit.errorhandler

/*
 *  File: ErrorEntityRegistry.kt
 *
 *  Created by morteza on 9/9/25.
 */
object ErrorEntityRegistry {
    private val registry = mutableListOf<Class<out IErrorEntity>>()

    fun register(clazz: Class<out IErrorEntity>) {
        registry.add(clazz)
    }

    fun getAllEntities(): List<Class<out IErrorEntity>> = registry
}

data class GenericErrorEntity(
    override val message: String = "Unknown Error",
    val code: Int? = null,
    val rawJson: String? = null
) : IErrorEntity