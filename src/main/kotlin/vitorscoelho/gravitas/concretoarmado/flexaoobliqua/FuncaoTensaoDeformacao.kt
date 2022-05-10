package vitorscoelho.gravitas.concretoarmado.flexaoobliqua

interface FuncaoTensaoDeformacao {
    val deformacoesInflexao: List<Double>
    fun tensao(deformacao: Double): Double
    fun tensao(deformacao: Double, funcaoTensaoDeformacaoMaterialQueEnvolve: FuncaoTensaoDeformacao): Double {
        return tensao(deformacao = deformacao) - funcaoTensaoDeformacaoMaterialQueEnvolve.tensao(deformacao = deformacao)
    }
}

fun ordenadasInflexao(
    deformada: DeformadaFlexaoReta,
    deformacoesInflexao: List<Double>,
    yMin: Double, yMax: Double
): List<Double> {
    if (deformada.curvatura == 0.0) return emptyList()
    return deformacoesInflexao
        .map { deformada.y(deformacao = it) }
        .filter { y -> (y > yMin) && (y < yMax) }
}

fun ordenadasParaIntegracao(
    ordenadasInflexao: List<Double>,
    yMin: Double, yMax: Double,
    ordenacaoCrescente: Boolean
): List<Double> {
    if (ordenadasInflexao.isEmpty()) {
        if (ordenacaoCrescente) return listOf(yMin, yMax) else listOf(yMax, yMin)
    }
    val listaNaoOrdenada = listOf(yMin, yMax) + ordenadasInflexao.filter { y -> (y > yMin) && (y < yMax) }
    return if (ordenacaoCrescente) listaNaoOrdenada.sorted() else listaNaoOrdenada.sortedDescending()
}