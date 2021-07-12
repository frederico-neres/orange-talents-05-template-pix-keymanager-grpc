package br.com.zup.pix.registra

class ContaPorTipoResponse(
    val agencia: String,
    val numero: String,
    val titular: Titular
    ) {

    fun paraConta(): Conta {
        return Conta(
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