package br.com.zup.pix.consulta

import br.com.zup.pix.registra.ChavePix
import br.com.zup.pix.registra.Conta
import br.com.zup.pix.registra.TipoChavePix
import br.com.zup.pix.registra.TipoConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clientId: String? = null,
    val tipoDeChave: TipoChavePix,
    val chave: String,
    val tipoDeConta: TipoConta,
    val conta: Conta,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clientId = chave.clienteId,
                tipoDeChave = chave.tipo,
                chave = chave.chave,
                tipoDeConta = chave.tipoConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}