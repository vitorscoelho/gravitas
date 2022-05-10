package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.secaotransversal

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.*
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math.simpson
import kotlin.math.max
import kotlin.math.min

class SecaoPoligonal(val vertices: List<Vetor2D>) : SecaoTransversal {

    override fun esforcosResistentes(
        funcaoTensaoDeformacao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
    ): EsforcosFlexaoReta {
        val ordenadasInflexao = ordenadasInflexao(
            deformada = deformada,
            deformacoesInflexao = funcaoTensaoDeformacao.deformacoesInflexao,
            yMin = this.vertices.minOf { it.y },
            yMax = this.vertices.maxOf { it.y }
        )
        val arestas = arestas(vertices = vertices)
        val esforcosFlexaoReta = DoubleArray(size = 2)
        arestas.forEach { aresta ->
            val esforcosAresta = aresta.esforcosResistentes(
                funcaoTensao = funcaoTensaoDeformacao,
                deformada = deformada,
                ordenadasInflexao = ordenadasInflexao
            )
            esforcosFlexaoReta[0] += esforcosAresta.normal
            esforcosFlexaoReta[1] += esforcosAresta.momento
        }
        return EsforcosFlexaoReta(normal = esforcosFlexaoReta[0], momento = esforcosFlexaoReta[1])
    }
}

/**
 * @return uma lista de arestas formadas pelos pontos informados em [vertices]
 */
private fun arestas(vertices: List<Vetor2D>): List<Aresta> {
    require(vertices.size > 1) { "Deveria haver pelo menos dois elementos na lista de vertices" }
    return (vertices + vertices.first()).zipWithNext { atual, proximo ->
        criarAresta(pontoInicial = atual, pontoFinal = proximo)
    }
}

private fun criarAresta(pontoInicial: Vetor2D, pontoFinal: Vetor2D): Aresta {
    val dy = (pontoFinal.y - pontoInicial.y)
    if ((dy == 0.0) || ((pontoInicial.x == 0.0) && (pontoFinal.x == 0.0))) {
        return ArestaSemNecessidadeDeIntegracao
    }
    return ArestaParaIntegracao(pontoInicial = pontoInicial, pontoFinal = pontoFinal)
}

private interface Aresta {
    fun esforcosResistentes(
        funcaoTensao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
        ordenadasInflexao: List<Double>
    ): EsforcosFlexaoReta

    fun esforcosResistentesObliqua(
        funcaoTensao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
        ordenadasInflexao: List<Double>
    ): EsforcosFlexaoObliqua
}

private object ArestaSemNecessidadeDeIntegracao : Aresta {
    override fun esforcosResistentes(
        funcaoTensao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
        ordenadasInflexao: List<Double>
    ) = EsforcosFlexaoReta.ZERO

    override fun esforcosResistentesObliqua(
        funcaoTensao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
        ordenadasInflexao: List<Double>
    ) = EsforcosFlexaoObliqua.ZERO
}

/**
 * Representa uma aresta de uma seção transversal
 * @property pontoInicial ponto inicial da aresta
 * @property pontoFinal ponto final da aresta
 */
private class ArestaParaIntegracao(val pontoInicial: Vetor2D, val pontoFinal: Vetor2D) : Aresta {
    private val dx = (pontoFinal.x - pontoInicial.x)
    private val dy = (pontoFinal.y - pontoInicial.y)
    private val yMin = min(pontoInicial.y, pontoFinal.y)
    private val yMax = max(pontoInicial.y, pontoFinal.y)
    private val yCrescente = pontoFinal.y > pontoInicial.y

    fun x(y: Double) = (dx / dy) * (y - pontoInicial.y) + pontoInicial.x
    fun y(x: Double) = (dy / dx) * (x - pontoInicial.x) + pontoInicial.y

    override fun toString() = "Aresta: $pontoInicial -> $pontoFinal"

    override fun esforcosResistentes(
        funcaoTensao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
        ordenadasInflexao: List<Double>
    ): EsforcosFlexaoReta {
        val ordenadasParaIntegracao = ordenadasParaIntegracao(
            ordenadasInflexao = ordenadasInflexao,
            yMin = yMin, yMax = yMax,
            ordenacaoCrescente = yCrescente
        ).toDoubleArray()
        val funcao = { y: Double ->
            val deformacao = deformada.deformacao(y = y)
            val x = x(y = y)
            val forca = funcaoTensao.tensao(deformacao = deformacao) * x
            val momento = forca * y
            doubleArrayOf(forca, momento)
        }
        val respostaIntegracao = simpson(
            f = funcao, fSize = 2, limitesSubIntervalos = ordenadasParaIntegracao, delta = deltaIntegracao
        )
        return EsforcosFlexaoReta(normal = respostaIntegracao[0], momento = respostaIntegracao[1])
    }

    override fun esforcosResistentesObliqua(
        funcaoTensao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
        ordenadasInflexao: List<Double>
    ): EsforcosFlexaoObliqua {
        val ordenadasParaIntegracao = ordenadasParaIntegracao(
            ordenadasInflexao = ordenadasInflexao,
            yMin = yMin, yMax = yMax,
            ordenacaoCrescente = yCrescente
        ).toDoubleArray()
        val funcao = { y: Double ->
            val deformacao = deformada.deformacao(y = y)
            val x = x(y = y)
            val forca = funcaoTensao.tensao(deformacao = deformacao) * x
            val momentoX = forca * y
            val momentoY = forca * x / 2.0
            doubleArrayOf(forca, momentoX, momentoY)
        }
        val respostaIntegracao = simpson(
            f = funcao, fSize = 3, limitesSubIntervalos = ordenadasParaIntegracao, delta = deltaIntegracao
        )
        return EsforcosFlexaoObliqua(
            normal = respostaIntegracao[0],
            momentoX = respostaIntegracao[1],
            momentoY = respostaIntegracao[2]
        )
    }

    companion object {
        private val deltaIntegracao = doubleArrayOf(0.01, 0.01, 0.01)
    }
}
