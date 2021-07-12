package br.com.zup.pix.registra

import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RegistraChavePixService(
    private val contaClient: ContaClient,
    private val chavePixRepository: ChavePixRepository) {

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix): ChavePix {

        val contaPorTipoResponse = contaClient.buscarContaPorTipo(
            clienteId = novaChavePix.clienteId,
            tipo = novaChavePix.tipoConta.name)

        val conta = contaPorTipoResponse?.body().paraConta()
        val chavePix = novaChavePix.paraChavePix(conta)

        return chavePixRepository.save(chavePix)
    }

}
