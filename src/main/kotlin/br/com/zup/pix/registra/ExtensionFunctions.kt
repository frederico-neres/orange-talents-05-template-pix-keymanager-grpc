package br.com.zup.pix.registra

import br.com.zup.RegistrarChavePixRequest
import java.util.*

fun RegistrarChavePixRequest.paraNovaChavePix(): NovaChavePix {
    return NovaChavePix(
        clienteId = this.clienteId,
        tipo = TipoChavePix.valueOf(this.tipo!!.name),
        chave = if (this.tipo.name.equals(TipoChavePix.CHAVE_ALEATORIA.name)) UUID.randomUUID().toString() else this.chave,
        tipoConta = TipoConta.valueOf(this.conta!!.name)
    )
}