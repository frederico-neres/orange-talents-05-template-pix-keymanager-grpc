package br.com.zup.pix.servicosExternos

import br.com.zup.Instituicoes
import br.com.zup.pix.consulta.ChavePixInfo
import br.com.zup.pix.chave.ChavePix
import br.com.zup.pix.chave.Conta
import br.com.zup.pix.chave.TipoConta
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
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

    @Get("/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML])
    fun findByKey(@PathVariable key: String): HttpResponse<ConsultaChavePixResponse>
}


@Introspected
data class CadastraChavePixRequest(
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
                    participant = Conta.ITAU_UNIBANCO_ISPB,
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

data class ConsultaChavePixResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)
{
    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipoDeChave = keyType.toTipoChavePix(),
            chave = this.key,
            tipoDeConta = when(this.bankAccount.accountType) {
                AccountType.CACC -> TipoConta.CONTA_CORRENTE
                AccountType.SVGS -> TipoConta.CONTA_POUPANCA
                else -> TipoConta.UNKNOWN_CONTA
            },
            conta = Conta(
                instituicao = Instituicoes.nome(bankAccount.participant),
                titularNome = owner.name,
                titularCpf = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numero = bankAccount.accountNumber
            )

        )
    }
}

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

