package br.com.zup.pix.interceptor

import io.micronaut.aop.Around

import kotlin.annotation.AnnotationRetention.RUNTIME

@MustBeDocumented
@Retention(RUNTIME)
@Target(AnnotationTarget.CLASS)
@Around
annotation class ExceptionAdvice
