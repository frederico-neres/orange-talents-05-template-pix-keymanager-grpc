package br.com.zup.pix.chave

import br.com.zup.pix.chave.ChavePix
import br.com.zup.pix.chave.Conta
import br.com.zup.pix.chave.TipoChavePix
import br.com.zup.pix.chave.TipoConta
import br.com.zup.pix.validator.ValidaChavePix
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidaChavePix
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