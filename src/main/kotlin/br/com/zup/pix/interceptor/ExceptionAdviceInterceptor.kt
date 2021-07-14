package br.com.zup.pix.interceptor

import br.com.zup.pix.exception.AlreadyExistsException
import br.com.zup.pix.exception.NotFoundException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ExceptionAdvice::class)
class ExceptionAdviceInterceptor: MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {
            return context.proceed()
        }catch (ex: Exception) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when(ex) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription(ex.message)
                is IllegalStateException -> Status.FAILED_PRECONDITION
                    .withCause(ex)
                    .withDescription(ex.message)
                is AlreadyExistsException -> Status.ALREADY_EXISTS
                    .withCause(ex)
                    .withDescription(ex.message)
                is NotFoundException -> Status.NOT_FOUND
                    .withCause(ex)
                    .withDescription(ex.message)
                else -> Status.UNKNOWN
                    .withCause(ex)
                    .withDescription("Ocorreu um erro desconhecido" )
            }

            responseObserver.onError(status.asRuntimeException())
        }

        return null
    }
}