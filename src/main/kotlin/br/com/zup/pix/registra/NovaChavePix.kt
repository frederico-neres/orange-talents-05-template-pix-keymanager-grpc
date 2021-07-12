package br.com.zup.pix.registra

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
class NovaChavePix(
    @field:NotBlank val clienteId: String,
    @field:NotNull val tipo: TipoChavePix,
    @field:NotBlank @field:Size(max = 77) val chave: String,
    @field:NotNull val tipoConta: TipoConta,
) {
    fun paraChavePix(conta: Conta): ChavePix {
        return ChavePix(
            clienteId = this.clienteId,
            tipo = this.tipo,
            chave = this.chave,
            tipoConta = this.tipoConta,
            conta = conta
        )
    }
}