package br.com.zup.pix.consulta

import br.com.zup.ConsultaChavePixResponse
import br.com.zup.Instituicoes
import br.com.zup.TipoChavePix
import br.com.zup.TipoConta
import br.com.zup.pix.chave.Conta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConsultaChavePixResponseConverter {
    fun convert(chaveInfo: ChavePixInfo): ConsultaChavePixResponse {
        return ConsultaChavePixResponse.newBuilder()
            .setClientId(chaveInfo.clientId?.toString() ?: "")
            .setPixId(chaveInfo.pixId?.toString() ?: "")
            .setChave(
                ConsultaChavePixResponse.ChavePix
                    .newBuilder()
                    .setTipoDeChave(TipoChavePix.valueOf(chaveInfo.tipoDeChave.name))
                    .setChave(chaveInfo.chave)
                    .setConta(
                        ConsultaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                            .setTipoDeConta(TipoConta.valueOf(chaveInfo.tipoDeConta.name))
                            .setInstituicao(Instituicoes.nome(Conta.ITAU_UNIBANCO_ISPB))
                            .setNomeDoTitular(chaveInfo.conta.titularNome)
                            .setCpfDoTitular(chaveInfo.conta.titularCpf)
                            .setAgencia(chaveInfo.conta.agencia)
                            .setNumeroDaConta(chaveInfo.conta.numero)
                            .build()
                    )
                    .setCriadaEm(chaveInfo.registradaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            )
            .build()
    }
}