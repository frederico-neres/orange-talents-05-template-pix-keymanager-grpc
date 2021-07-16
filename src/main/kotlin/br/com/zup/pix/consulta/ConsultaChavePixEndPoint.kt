package br.com.zup.pix.consulta

import br.com.zup.*
import br.com.zup.ConsultaChavePixResponse
import br.com.zup.pix.chave.ChavePixRepository
import br.com.zup.pix.interceptor.ExceptionAdvice
import br.com.zup.pix.registra.toModel
import br.com.zup.pix.servicosExternos.BcbClient
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton
import javax.validation.Validator

@ExceptionAdvice
@Singleton
class ConsultaChavePixEndPoint(
    private val chavePixRepository: ChavePixRepository,
    private val bcbClient: BcbClient,
    private val validator: Validator
): KeyManagerConsultaGrpcServiceGrpc.KeyManagerConsultaGrpcServiceImplBase() {

    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {

        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(chavePixRepository, bcbClient)
        responseObserver.onNext(ConsultaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()
    }

    override fun consultaTodas(
        request: ListaChavePixRequest?,
        responseObserver: StreamObserver<ListaChavePixResponse>?
    ) {
        if (request?.clientId.isNullOrBlank())
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")

        val uuidClientId = UUID.fromString(request?.clientId)
        val chaves = chavePixRepository.findByClienteId(uuidClientId.toString()).map {
                ListaChavePixResponse.DetalhesChave.newBuilder()
                    .setPixId(it.id.toString())
                    .setTipoChave(TipoChavePix.valueOf(it.tipo.name))
                    .setChave(it.chave)
                    .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                    .setCriadoEm( it.criadaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    }).build()
        }

        responseObserver?.onNext(
            ListaChavePixResponse.newBuilder()
                .setClientId(uuidClientId.toString())
                .addAllChavesPix(chaves)
                .build()
        )
        responseObserver?.onCompleted()
    }

}
