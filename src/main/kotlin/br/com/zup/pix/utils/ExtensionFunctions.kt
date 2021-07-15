package br.com.zup.pix.registra

import br.com.zup.ConsultaChavePixRequest
import br.com.zup.ConsultaChavePixRequest.FiltroCase.*
import br.com.zup.RegistrarChavePixRequest
import br.com.zup.pix.consulta.Filtro
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun RegistrarChavePixRequest.paraNovaChavePix(): NovaChavePix {
    return NovaChavePix(
        clienteId = this.clienteId,
        tipo = TipoChavePix.valueOf(this.tipo!!.name),
        chave = if (this.tipo.name.equals(TipoChavePix.CHAVE_ALEATORIA.name)) UUID.randomUUID().toString() else this.chave,
        tipoConta = TipoConta.valueOf(this.conta!!.name)
    )
}

fun ConsultaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when(filtroCase!!) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clientId = it.clientId, pixId = it.pixId)
        }
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro
}