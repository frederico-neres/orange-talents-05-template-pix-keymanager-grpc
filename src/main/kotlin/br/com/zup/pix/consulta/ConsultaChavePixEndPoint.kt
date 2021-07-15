package br.com.zup.pix.consulta

import br.com.zup.ConsultaChavePixRequest
import br.com.zup.ConsultaChavePixResponse
import br.com.zup.KeyManagerConsultaGrpcServiceGrpc
import br.com.zup.pix.registra.ChavePixRepository
import br.com.zup.pix.interceptor.ExceptionAdvice
import br.com.zup.pix.registra.toModel
import br.com.zup.pix.servicosExternos.BcbClient
import io.grpc.stub.StreamObserver
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
}
