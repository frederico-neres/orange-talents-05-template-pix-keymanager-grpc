package br.com.zup.pix.registra

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotBlank val clienteId: String,
    @field:NotNull val tipo: TipoChavePix,
    @field:NotBlank @field:Size(max = 77) var chave: String,
    @field:NotNull val tipoConta: TipoConta,
    @field:NotNull @field:Embedded val conta: Conta,
) {

    @Id
    @Column(length = 16)
    var id: UUID = UUID.randomUUID()
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun atualiza(chave: String) { this.chave = chave }
}