package br.com.zup.pix.registra

import br.com.zup.*
import br.com.zup.pix.chave.ChavePix
import br.com.zup.pix.chave.ChavePixRepository
import br.com.zup.pix.chave.Conta
import br.com.zup.pix.servicosExternos.*
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
import java.time.LocalDateTime
import javax.inject.Inject
import br.com.zup.pix.chave.TipoChavePix as TipoChavePixEntity
import br.com.zup.pix.chave.TipoConta as TipoContaEntity


@MicronautTest(transactional = false)
internal class RegistraChavePixEndpointTest(
    val chavePixRepository: ChavePixRepository,
    val clientGrpc: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub,
    val itauClient: ItauClient,
) {
    @field:Inject
    lateinit var bcbClient: BcbClient

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
                titularCpf = request.chave,
                instituicao = Instituicoes.nome(Conta.ITAU_UNIBANCO_ISPB)
            )
        )

        chavePixRepository.save(chavePix)

        Mockito.`when`(itauClient
            .buscarContaPorTipo(request.clienteId, request.conta.name))
            .thenReturn(dadosDaResponse())

        Mockito.`when`(bcbClient.create(cadastraChavePixRequest(request.chave, KeyType.CPF)))
            .thenReturn(cadastraChavePixResponse(request.chave, KeyType.CPF))

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

        Mockito.`when`(bcbClient.create(cadastraChavePixRequest(request.chave, KeyType.CPF)))
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

    @Test
    fun `NAO deve cadastrar chave Pix quando nao for possivel registrar chave no BCB`() {

        val request = getRequest("97383289935", TipoChavePix.CPF)
        Mockito.`when`(itauClient
            .buscarContaPorTipo(request.clienteId, request.conta.name))
            .thenReturn(dadosDaResponse())

        Mockito.`when`(bcbClient.create(cadastraChavePixRequest(request.chave, KeyType.CPF)))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            clientGrpc.registra(request)
        }

        with(error) {
                assertEquals(Status.FAILED_PRECONDITION.code, status.code)
        }
    }

    fun templateDeveRegistrarChavePix(chave: String, tipo: TipoChavePix) {

        val request = getRequest(chave, tipo)

        Mockito.`when`(itauClient
            .buscarContaPorTipo(request.clienteId, request.conta.name))
            .thenReturn(dadosDaResponse())

        val keyType = br.com.zup.pix.chave.TipoChavePix.valueOf(tipo.name).toBbcKeyType()
        val cadastraChavePixRequest = cadastraChavePixRequest(chave, keyType)

        Mockito.`when`(bcbClient.create(cadastraChavePixRequest))
            .thenReturn(cadastraChavePixResponse(chave, keyType))

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


    private fun cadastraChavePixRequest(chave: String, tipo: KeyType): CadastraChavePixRequest {
        return CadastraChavePixRequest(
            keyType = tipo,
            key = chave,
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

private fun cadastraChavePixResponse(chave: String, tipo: KeyType): HttpResponse<CadastraChavePixResponse> {
    return HttpResponse.created(CadastraChavePixResponse(
        keyType = KeyType.CPF,
        key = chave,
        bankAccount = bankAccount(),
        owner = owner(),
        createdAt = LocalDateTime.now()
    ))
}
    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = Conta.ITAU_UNIBANCO_ISPB,
            branch = "0001",
            accountNumber = "291900",
            accountType = AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Fred",
            taxIdNumber = "97383289935"
        )
    }

    @MockBean(ItauClient::class)
    fun `sistemaItauMock`(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun `bcbClientMock`(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class grpcFactory {
        @Bean
        fun registeStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)=
            KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
    }
}