package br.com.zup.pix.registra

import br.com.zup.pix.exception.AlreadyExistsException
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RegistraChavePixService(
    private val contaClient: ItauClient,
    private val chavePixRepository: ChavePixRepository) {

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix): ChavePix {
        val existsByChave = chavePixRepository.existsByChave(novaChavePix.chave)
        if(existsByChave) {
            throw AlreadyExistsException("Já existe uma chave com esse valor")
        }

        val contaPorTipoResponse = contaClient.buscarContaPorTipo(
            clienteId = novaChavePix.clienteId,
            tipo = novaChavePix.tipoConta.name)

        val conta = contaPorTipoResponse?.body()?.paraConta() ?: throw IllegalStateException("Cliente não encontrado")
        val chavePix = novaChavePix.paraChavePix(conta)

        return chavePixRepository.save(chavePix)
    }

}
