package br.com.zup.pix.registra

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

//@Client("//localhost:9091/")
@Client("http://localhost:9091")
interface ContaClient {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscarContaPorTipo(@PathVariable clienteId: String, @QueryValue("tipo") tipo: String): HttpResponse<ContaPorTipoResponse>
}