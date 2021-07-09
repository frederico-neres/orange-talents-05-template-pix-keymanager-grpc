package br.com.zup.pix.registra

import javax.persistence.Embeddable

@Embeddable
class Conta(
    val agencia: String,
    val numero: String,
    val titularNome: String,
    val titularCpf: String,
)
