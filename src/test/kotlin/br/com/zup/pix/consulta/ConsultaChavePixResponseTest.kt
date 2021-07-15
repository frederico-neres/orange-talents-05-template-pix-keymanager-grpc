package br.com.zup.pix.consulta

import br.com.zup.ConsultaChavePixRequest
import br.com.zup.KeyManagerConsultaGrpcServiceGrpc
import br.com.zup.pix.registra.*
import br.com.zup.pix.servicosExternos.*
import br.com.zup.pix.servicosExternos.ConsultaChavePixResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import java.time.LocalDateTime
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaChavePixResponseTest(
    val grpcClient: KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
    }

    @BeforeEach
    fun setup() {
        chavePixRepository.save(chave(tipo = TipoChavePix.EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = CLIENTE_ID))
        chavePixRepository.save(chave(tipo = TipoChavePix.CPF, chave = "63657520325", clienteId = CLIENTE_ID))
        chavePixRepository.save(chave(tipo = TipoChavePix.CHAVE_ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
        chavePixRepository.save(chave(tipo = TipoChavePix.CELULAR, chave = "+551155554321", clienteId = CLIENTE_ID))
    }


    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `DEVE carregar chave por pixId e clienteId`() {

        val chaveExistente = chavePixRepository.findByChave("+551155554321").get()

        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setPixId(ConsultaChavePixRequest.FiltroPorPix.newBuilder()
                    .setPixId(chaveExistente.id.toString())
                    .setClientId(chaveExistente.clienteId.toString())
                    .build()
                ).build())

        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clientId)
            assertEquals(chaveExistente.tipo.name, this.chave.tipoDeChave.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `NAO deve carregar chave por pixId e clienteId quando registro nao existir`() {
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val thrown = assertThrows <StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPix.newBuilder()
                            .setPixId(pixIdNaoExistente)
                            .setClientId(clienteIdNaoExistente)
                            .build()
                    ).build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `DEVE carregar chave por valor da chave quando registro existir localmente`() {

        val chaveExistente = chavePixRepository.findByChave("rafael.ponte@zup.com.br").get()

        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave("rafael.ponte@zup.com.br")
                .build()
        )

        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), this.clientId)
            assertEquals(chaveExistente.tipo.name, this.chave.tipoDeChave.name)
            assertEquals(chaveExistente.chave, this.chave.chave)
        }
    }

    @Test
    fun `DEVE carregar chave por valor da chave quando registro nao existir localmente mas existir no BCB`() {

        val bcbResponse = pixKeyDetailsResponse()
        Mockito.`when`(bcbClient.findByKey(key = "user.from.another.bank@santander.com.br"))
            .thenReturn(HttpResponse.ok(bcbResponse))

        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder()
                .setChave("user.from.another.bank@santander.com.br")
                .build()
        )

        with(response) {
            assertEquals("", this.pixId)
            assertEquals("", this.clientId)
            assertEquals(bcbResponse.keyType.name, this.chave.tipoDeChave.name)
            assertEquals(bcbResponse.key, this.chave.chave)
        }
    }

    @Test
    fun `NAO deve carregar chave por valor da chave quando registro nao existir localmente nem no BCB`() {

        Mockito.`when`(bcbClient.findByKey(key = "not.existing.user@santander.com.br"))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setChave("not.existing.user@santander.com.br")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix inexistente", status.description)
        }
    }
    @Test
    fun `NAO deve carregar chave por pixId e clienteId quando filtro invalido`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPorPix.newBuilder()
                            .setPixId("")
                            .setClientId("")
                            .build()
                    ).build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(status.description?.contains("pixId: UUID Inválido") ?: false)
            assertTrue(status.description?.contains("clientId: UUID Inválido") ?: false)
        }
    }

    @Test
    fun `NAO deve carregar chave por valor da chave quando filtro invalido`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder().setChave("").build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(status.description?.contains("chave: must not be blank") ?: false)
        }
    }

    @Test
    fun `NAO deve carregar chave quando filtro invalido`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder().build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }

    private fun chave(
        tipo: TipoChavePix,
        chave: String = UUID.randomUUID().toString(),
        clienteId: String = UUID.randomUUID().toString(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipo= tipo,
            chave = chave,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = Conta(
                instituicao = "UNIBANCO ITAU",
                titularNome = "Rafael Ponte",
                titularCpf = "12345678900",
                agencia = "1218",
                numero = "123456"
            )
        )
    }

    private fun pixKeyDetailsResponse(): ConsultaChavePixResponse {
        return ConsultaChavePixResponse(
            keyType = KeyType.EMAIL,
            key = "user.from.another.bank@santander.com.br",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )

    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Another User",
            taxIdNumber = "12345678901"
        )
    }

    @MockBean(BcbClient::class)
    fun `bcbClientMock`(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub {
            return KeyManagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
        } }

}