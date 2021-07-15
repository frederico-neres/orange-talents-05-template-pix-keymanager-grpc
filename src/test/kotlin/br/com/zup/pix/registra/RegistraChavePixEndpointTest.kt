package br.com.zup.pix.registra

import br.com.zup.*
import br.com.zup.TipoChavePix
import br.com.zup.TipoConta
import br.com.zup.pix.servicosExternos.ContaPorTipoResponse
import br.com.zup.pix.servicosExternos.ItauClient
import br.com.zup.pix.servicosExternos.Titular
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import br.com.zup.pix.registra.TipoChavePix as TipoChavePixEntity
import br.com.zup.pix.registra.TipoConta as TipoContaEntity


@MicronautTest(transactional = false)
internal class RegistraChavePixEndpointTest(
    val chavePixRepository: ChavePixRepository,
    val clientGrpc: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    val itauClient: ItauClient
) {


    @BeforeEach
    internal fun setUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    internal fun `DEVE registrar chave pix CPF`() {
        templateDeveRegistrarChavePix("97383289935", TipoChavePix.CPF)
    }

    @Test
    internal fun `DEVE registrar chave pix CELULAR`() {
        templateDeveRegistrarChavePix("48987212046", TipoChavePix.CELULAR)
    }

    @Test
    internal fun `DEVE registrar chave pix EMAIL`() {
        templateDeveRegistrarChavePix("sebastianamayadepaula@eccofibra.com.br", TipoChavePix.EMAIL)
    }

    @Test
    internal fun `DEVE registrar chave pix ALEATORIA`() {
        templateDeveRegistrarChavePix("", TipoChavePix.CHAVE_ALEATORIA)
    }

    @Test
    internal fun `NAO deve registrar chave pix se ja existir uma igual`() {

        val request = getRequest("97383289935", TipoChavePix.CPF)

        val chavePix = ChavePix(
            clienteId = request.clienteId,
            tipo = TipoChavePixEntity.valueOf(request.tipo.name),
            chave = request.chave,
            tipoConta = TipoContaEntity.valueOf(request.conta.name),
            conta = Conta(
                agencia = "0001",
                numero = "291900",
                titularNome = "Fred",
                titularCpf = request.chave
            )
        )

        chavePixRepository.save(chavePix)


        Mockito.`when`(itauClient
            .buscarContaPorTipo(request.clienteId, request.conta.name))
            .thenReturn(dadosDaResponse())


        val error = assertThrows<StatusRuntimeException> {
           clientGrpc.registra(request)
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Já existe uma chave com esse valor", status.description)
        }
    }

    @Test
    internal fun `NAO deve registrar chave pix quando nao encontrar dados da conta cliente`() {

        val request = getRequest("97383289935", TipoChavePix.CPF)

        Mockito.`when`(itauClient
            .buscarContaPorTipo(request.clienteId, request.conta.name))
            .thenReturn(HttpResponse.notFound())


        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registra(request)
        }

        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
        }
    }


    @Test
    fun `NAO deve cadastrar chave quando parametros invalidos`() {

        val request =  RegistrarChavePixRequest.newBuilder().build()

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registra(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(status.description?.contains("Chave pix inválida") ?: false)
        }
    }

    fun templateDeveRegistrarChavePix(chave: String, tipo: TipoChavePix) {
        val request = getRequest(chave, tipo)

        Mockito.`when`(itauClient
            .buscarContaPorTipo(request.clienteId, request.conta.name))
            .thenReturn(dadosDaResponse())

        val response = clientGrpc.registra(request)
        with(response) {
            assertEquals(request.clienteId, response.clienteId)
            assertNotNull(response.pixID)
        }
    }

    fun getRequest(chave: String, tipo: TipoChavePix): RegistrarChavePixRequest {
        return RegistrarChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setChave(chave)
            .setTipo(tipo)
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()
    }

    fun dadosDaResponse(): MutableHttpResponse<ContaPorTipoResponse>? {
        return HttpResponse.ok<ContaPorTipoResponse>(
            ContaPorTipoResponse(
                agencia = "0001",
                numero = "291900",
                titular = Titular(nome = "Fred", cpf= "97383289935")
            )
        )
    }

    @MockBean(ItauClient::class)
    fun `sistemaItauMock`(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @Factory
    class grpcFactory {
        @Bean
        fun registeStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)=
            KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
    }
}