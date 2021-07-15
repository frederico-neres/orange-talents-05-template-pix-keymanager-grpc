package br.com.zup.pix.servicosExternos

import br.com.zup.pix.registra.ChavePix
import br.com.zup.pix.registra.Conta
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client(value = "\${bcb.pix.url}")
interface BcbClient {

    @Post("/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun create(@Body request: CadastraChavePixRequest): HttpResponse<CadastraChavePixResponse>

    @Delete("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun delete(@PathVariable key: String, @Body request: RemoveChavePixRequest): HttpResponse<RemoveChavePixResponse>

}


@Introspected
class CadastraChavePixRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object {
        fun of(chavePix: ChavePix): CadastraChavePixRequest {
            return CadastraChavePixRequest(
                keyType = chavePix.tipo.toBbcKeyType(),
                key = chavePix.chave,
                bankAccount = BankAccount(
                    participant = "String",
                    branch = chavePix.conta.agencia,
                    accountNumber = chavePix.conta.numero,
                    accountType = chavePix.tipoConta.toBbcAccountType()
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = chavePix.conta.titularNome,
                    taxIdNumber = chavePix.conta.titularCpf
                )
            )
        }
    }

}


data class CadastraChavePixResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

data class RemoveChavePixRequest(
    val key: String,
    val participant: String
) {
    constructor(chave: String): this(chave, Conta.ITAU_UNIBANCO_ISPB)
}

data class RemoveChavePixResponse (
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {
    enum class OwnerType {
            NATURAL_PERSON, LEGAL_PERSON
        }
}

enum class AccountType {
    CACC, SVGS, UNKNOWN;
}

