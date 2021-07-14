package br.com.zup.pix.remove

import br.com.zup.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.RemoveChavePixRequest
import br.com.zup.RemoveChavePixResponse
import br.com.zup.pix.exception.NotFoundException
import br.com.zup.pix.interceptor.ExceptionAdvice
import br.com.zup.pix.registra.ChavePixRepository
import br.com.zup.pix.validator.ValidUUID
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@ExceptionAdvice
@Validated
@Singleton
class RemoveChavePixEndpoint(
    val chavePixRepository: ChavePixRepository
): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceImplBase() {

    override fun remove(request: RemoveChavePixRequest, responseObserver: StreamObserver<RemoveChavePixResponse>) {

        val (id, clienteId) = validatedParameters(request.pixID, request.clienteId)
        val OptionalChavePixExistente = chavePixRepository.findByIdAndClienteId(id, clienteId)

        if(OptionalChavePixExistente.isEmpty) {
            throw NotFoundException("Chave Pix não encontrada ou não pertence ao cliente")
        }

        val chavePixExistente = OptionalChavePixExistente.get()
        chavePixRepository.deleteById(chavePixExistente.id)

        responseObserver.onNext(RemoveChavePixResponse.newBuilder()
            .setPixID(chavePixExistente.id.toString())
            .setClienteId(chavePixExistente.clienteId)
            .build())
        responseObserver.onCompleted()
    }

    fun validatedParameters(@NotBlank(message = "Pix Id não pode ser vazio")
                            @ValidUUID(message = "Pix Id com formato inválido")
                            id: String,
                            @NotBlank(message = "Cliente Id não pode ser vazio")
                            @ValidUUID(message = "Cliente Id com formato inválido")
                            clienteId: String): Pair<UUID, String> {
        return Pair(UUID.fromString(id), clienteId)
    }

}

