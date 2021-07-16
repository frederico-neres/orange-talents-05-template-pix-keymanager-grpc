package br.com.zup.pix.consulta


import br.com.zup.Instituicoes
import br.com.zup.pix.chave.Conta
import br.com.zup.pix.chave.TipoChavePix
import br.com.zup.pix.chave.TipoConta
import br.com.zup.pix.servicosExternos.AccountType
import br.com.zup.pix.servicosExternos.BankAccount
import br.com.zup.pix.servicosExternos.Owner
import java.time.LocalDateTime

class ConsultaChavePixResponse(
    val keyType: TipoChavePix,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {
    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipoDeChave = keyType,
            chave = this.key,
            tipoDeConta = when(this.bankAccount.accountType) {
                AccountType.CACC -> TipoConta.CONTA_CORRENTE
                AccountType.SVGS -> TipoConta.CONTA_POUPANCA
                else -> TipoConta.UNKNOWN_CONTA
            },
            conta = Conta(
                instituicao = Instituicoes.nome(bankAccount.participant),
                titularNome = owner.name,
                titularCpf = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numero = bankAccount.accountNumber
                )

        )
    }
}