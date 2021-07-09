package br.com.zup.pix.registra

import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RegistraChavePixService(private val chavePixRepository: ChavePixRepository) {

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix): ChavePix {

        val conta = Conta(
            agencia = "0001",
            numero = "291900",
            titularNome = "Isadora",
            titularCpf = "73028446740"
        )

        val chavePix = novaChavePix.paraChavePix(conta)
        return chavePixRepository.save(chavePix)
    }

}
