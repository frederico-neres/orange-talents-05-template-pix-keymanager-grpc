package br.com.zup.pix.registra

import br.com.zup.RegistrarChavePixRequest

fun RegistrarChavePixRequest.paraNovaChavePix(): NovaChavePix {
    return NovaChavePix(
        clienteId = this.clienteId,
        tipo = TipoChavePix.valueOf(this.tipo!!.name),
        chave = this.chave,
        conta = TipoConta.valueOf(this.conta!!.name)
    )
}