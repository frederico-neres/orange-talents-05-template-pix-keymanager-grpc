package br.com.zup.pix.registra

import br.com.zup.pix.exception.AlreadyExistsException
import br.com.zup.pix.servicosExternos.BcbClient
import br.com.zup.pix.servicosExternos.CadastraChavePixRequest
import br.com.zup.pix.servicosExternos.CadastraChavePixResponse
import br.com.zup.pix.servicosExternos.ItauClient
import io.micronaut.http.HttpResponse
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RegistraChavePixService(
    private val contaClient: ItauClient,
    private val chavePixRepository: ChavePixRepository,
    private val bcbClient: BcbClient) {

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

        chavePixRepository.save(chavePix)

        val cadastraChavePixRequest = CadastraChavePixRequest.of(chavePix)

        var bcbClientResponse: HttpResponse<CadastraChavePixResponse>
        try {
            bcbClientResponse = bcbClient.create(cadastraChavePixRequest)
        } catch (ex: Exception) {
            throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")
        }

        chavePix.atualiza(bcbClientResponse.body()!!.key)
        return chavePix
    }

}
