package com.example.delta.server

fun Throwable.toException(): Exception =
    this as? Exception ?: Exception(this.message ?: "خطای نامشخص", this)
