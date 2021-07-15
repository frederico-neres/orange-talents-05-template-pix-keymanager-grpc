package br.com.zup.pix.validator

import io.micronaut.core.annotation.AnnotationValue
import java.lang.annotation.Documented
import java.util.*
import javax.inject.Singleton
import javax.validation.Constraint
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

@Documented
@Constraint(validatedBy = [ValidUUIDValidator::class])
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(RUNTIME)
annotation class ValidUUID(
    val message: kotlin.String = "UUID Inválido",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Singleton
class ValidUUIDValidator: ConstraintValidator<ValidUUID, String> {
    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValidUUID>,
        context: ConstraintValidatorContext,
    ): Boolean {
        try {
            UUID.fromString(value)
            return true
        }catch (ex: Exception) {
            return false
        }
    }

}
