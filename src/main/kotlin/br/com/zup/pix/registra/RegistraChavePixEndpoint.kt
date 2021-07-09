package br.com.zup.pix.registra

import br.com.zup.KeyManagerGrpcServiceGrpc
import br.com.zup.RegistrarChavePixRequest
import br.com.zup.RegistrarChavePixResponse
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class RegistraChavePixEndpoint(private val service: RegistraChavePixService): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun registra(
        request: RegistrarChavePixRequest?,
        responseObserver: StreamObserver<RegistrarChavePixResponse>?
    ) {

        val novaChavePix = request!!.paraNovaChavePix()
        val chavePixSalva = service.registra(novaChavePix)

        responseObserver?.onNext(RegistrarChavePixResponse.newBuilder()
                                                          .setClienteId(chavePixSalva.clienteId)
                                                          .setPixID(chavePixSalva.id.toString())
                                                          .build())
        responseObserver?.onCompleted()
    }
}