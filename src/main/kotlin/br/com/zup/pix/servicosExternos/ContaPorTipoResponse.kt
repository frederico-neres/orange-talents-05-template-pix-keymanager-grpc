package br.com.zup.pix.servicosExternos

import br.com.zup.Instituicoes
import br.com.zup.pix.chave.Conta

class ContaPorTipoResponse(
    val agencia: String,
    val numero: String,
    val titular: Titular
    ) {

    fun paraConta(): Conta {
        return Conta(
            instituicao = Instituicoes.nome(Conta.ITAU_UNIBANCO_ISPB),
            agencia = this.agencia,
            numero = this.numero,
            titularNome = this.titular.nome,
            titularCpf = this.titular.cpf,
        )
    }
}

data class Titular(
    val nome: String,
    val cpf: String
)