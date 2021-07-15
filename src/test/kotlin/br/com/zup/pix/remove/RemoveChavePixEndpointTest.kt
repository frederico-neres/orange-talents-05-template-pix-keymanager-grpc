package br.com.zup.pix.remove

import br.com.zup.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.RemoveChavePixRequest
import br.com.zup.pix.servicosExternos.RemoveChavePixRequest as BcbClientRemoveChavePixRequest
import br.com.zup.pix.registra.*
import br.com.zup.pix.servicosExternos.BcbClient
import br.com.zup.pix.servicosExternos.RemoveChavePixResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    val chavePixRepository: ChavePixRepository,
    val clientGrpc: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub,
) {

    @field:Inject
    lateinit var bcbClient: BcbClient
    lateinit var CHAVE_PIX_EXISTENTE: ChavePix

    @BeforeEach
    internal fun setUp() {
        CHAVE_PIX_EXISTENTE = ChavePix(
            clienteId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipo = TipoChavePix.CPF,
            chave = "97383289935",
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = Conta(
                agencia = "0001",
                numero = "291900",
                titularNome = "Fred",
                titularCpf = "97383289935"
            )
        )

        chavePixRepository.save(CHAVE_PIX_EXISTENTE)
    }

    @AfterEach
    internal fun tearDown() {
        chavePixRepository.deleteAll()
    }

    @Test
    internal fun `DEVE remover chave Pix existente`() {

        val request = getRequest(CHAVE_PIX_EXISTENTE.id.toString(), CHAVE_PIX_EXISTENTE.clienteId)

        Mockito.`when`(bcbClient.delete(key = CHAVE_PIX_EXISTENTE.chave,
            request = BcbClientRemoveChavePixRequest(
                key = CHAVE_PIX_EXISTENTE.chave,
                participant = Conta.ITAU_UNIBANCO_ISPB
            )))
            .thenReturn(HttpResponse.ok(RemoveChavePixResponse(
                key = CHAVE_PIX_EXISTENTE.chave,
                participant = Conta.ITAU_UNIBANCO_ISPB,
                deletedAt = LocalDateTime.now()
            )))

        val response = clientGrpc.remove(request)

        with(response) {
            assertEquals(CHAVE_PIX_EXISTENTE.clienteId, response.clienteId)
            assertEquals(CHAVE_PIX_EXISTENTE.id.toString(), response.pixID)
        }

        val chavePix = chavePixRepository.findById(CHAVE_PIX_EXISTENTE.id)
        assertTrue(chavePix.isEmpty)
    }


    @Test
    internal fun `NAO deve remover chave Pix quando chave inexistente`() {

        val pixIDInexistente = UUID.randomUUID().toString()
        testNotFoundchavePix(pixIDInexistente, CHAVE_PIX_EXISTENTE.clienteId)
    }

    @Test
    internal fun `NAO deve remover chave Pix quando chave pertence a outro Cliente`() {

        val clienteIdInexistente = UUID.randomUUID().toString()
        testNotFoundchavePix(CHAVE_PIX_EXISTENTE.id.toString(), clienteIdInexistente)
    }

    fun testNotFoundchavePix(pixID: String, clienteId: String) {
        val request = getRequest(pixID, clienteId)

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.remove(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    fun getRequest(pixID: String, clienteId: String): RemoveChavePixRequest {
        return RemoveChavePixRequest.newBuilder()
            .setClienteId(clienteId)
            .setPixID(pixID)
            .build()
    }

    @MockBean(BcbClient::class)
    fun `bcbClientMock`(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class grpcFactory {
        @Bean
        fun registeStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)=
            KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
    }
}