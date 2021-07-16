package br.com.zup.pix.consulta

import br.com.zup.KeyManagerConsultaGrpcServiceGrpc
import br.com.zup.ListaChavePixRequest
import br.com.zup.pix.registra.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ListaChavePixEndpointTest(
    val grpcClient: KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub,
    val chavePixRepository: ChavePixRepository
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
    }

    @BeforeEach
    fun setup() {
        chavePixRepository.save(chave(tipo = TipoChavePix.EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = CLIENTE_ID))
        chavePixRepository.save(chave(tipo = TipoChavePix.CHAVE_ALEATORIA, chave = "randomkey-2", clienteId = UUID.randomUUID().toString()))
        chavePixRepository.save(chave(tipo = TipoChavePix.CHAVE_ALEATORIA, chave = "randomkey-3", clienteId = CLIENTE_ID))
    }

    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `DEVE listar todas as chaves do cliente`() {

        val clienteId = CLIENTE_ID.toString()

        val response = grpcClient.consultaTodas(
            ListaChavePixRequest.newBuilder()
                .setClientId(clienteId)
                .build())

        with (response.chavesPixList) {
            assertThat(this, Matchers.hasSize(2))
            assertThat(
                this.map { Pair(it.tipoChave, it.chave) }.toList(),
                Matchers.containsInAnyOrder(
                    Pair(br.com.zup.TipoChavePix.CHAVE_ALEATORIA, "randomkey-3"),
                    Pair(br.com.zup.TipoChavePix.EMAIL, "rafael.ponte@zup.com.br")
                )
            )
        }
    }


    @Test
    fun `NAO deve listar as chaves do cliente quando cliente nao possuir chaves`() {

        val clienteSemChaves = UUID.randomUUID().toString()

        val response = grpcClient.consultaTodas(ListaChavePixRequest.newBuilder()
            .setClientId(clienteSemChaves)
            .build())

        assertEquals(0, response.chavesPixCount)
    }

    @Test
    fun `NAO deve listar todas as chaves do cliente quando clienteId for invalido`() {

        val clienteIdInvalido = ""

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consultaTodas(ListaChavePixRequest.newBuilder()
                .setClientId(clienteIdInvalido)
                .build())
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStup(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceBlockingStub {
            return KeyManagerConsultaGrpcServiceGrpc.newBlockingStub(channel)
        } }

    private fun chave(
        tipo: TipoChavePix,
        chave: String = UUID.randomUUID().toString(),
        clienteId: String = UUID.randomUUID().toString(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipo = tipo,
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
}