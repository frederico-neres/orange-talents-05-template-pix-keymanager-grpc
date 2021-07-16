package br.com.zup.pix.consulta

import br.com.zup.pix.chave.ChavePix
import br.com.zup.pix.chave.Conta
import br.com.zup.pix.chave.TipoChavePix
import br.com.zup.pix.chave.TipoConta
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