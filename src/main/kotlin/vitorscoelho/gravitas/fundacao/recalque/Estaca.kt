package vitorscoelho.gravitas.fundacao.recalque

data class TrechoDeEstaca(
    val comprimento: Double,
    val secao: Secao,
    val moduloDeDeformacao: Double,
    val atritoLateralTopo: Double,
    val atritoLateralBase: Double,
    val qtdDivisoes: Int
) {
    init {
        require(comprimento > 0.0)
        require(moduloDeDeformacao > 0.0)
        require(atritoLateralTopo >= 0.0)
        require(atritoLateralBase >= 0.0)
        require(qtdDivisoes > 1)
    }

    private val variacaoAtritoPorCentimetro = (atritoLateralBase - atritoLateralTopo) / comprimento

    private fun atritoLateral(distanciaTopo: Double) = atritoLateralTopo + distanciaTopo * variacaoAtritoPorCentimetro

    /**Retorna uma lista com forças representando a resultante no centro da seção*/
    fun forcasFuste(topoDoTrecho: Vetor3D): Sequence<Forca> {
        val delta = comprimento / qtdDivisoes
        val forcasResultantes = (0 until qtdDivisoes).asSequence()
            .map { divisao -> divisao.toDouble() * delta }
            .map { zMin ->
                val trapezio = Trapezio(
                    yInferior = zMin,
                    larguraInferior = atritoLateral(distanciaTopo = zMin),
                    larguraSuperior = atritoLateral(distanciaTopo = zMin + delta),
                    altura = delta
                )
                Forca(
                    magnitude = trapezio.area,
                    posicao = topoDoTrecho.plus(deltaZ = trapezio.ycg)
                )
            }
        return secao.forcasPerimetro(forcasResultantes = forcasResultantes)
    }

    fun forcasPonta(posicaoPonta: Vetor3D, cargaNaPonta: Double): Sequence<Forca> =
        secao.forcasPonta(forcaResultante = Forca(magnitude = cargaNaPonta, posicao = posicaoPonta))
}

data class EstacaPrismatica(val topo: Vetor3D, val trechos: List<TrechoDeEstaca>, val cargaNaPonta: Double) {
    init {
        //TODO deve exigir que não possa existir transferência de carga com profundidade negativa
        require(trechos.isNotEmpty())
        require(cargaNaPonta >= 0.0)
    }

    private val topoDosTrechos: Map<TrechoDeEstaca, Vetor3D> by lazy {
        var topoAtual = topo
        trechos.associateWith { trecho ->
            val topoDoTrecho = topoAtual
            topoAtual = topoAtual.plus(deltaZ = trecho.comprimento)
            topoDoTrecho
        }
    }

    val forcasDoFuste: Sequence<Forca> by lazy {
        trechos.asSequence()
            .flatMap { trecho -> trecho.forcasFuste(topoDoTrecho = topoDosTrechos[trecho]!!) }
    }

    val forcasDaPonta: Sequence<Forca> by lazy {
        trechos.last().forcasPonta(
            posicaoPonta = topo.plus(deltaZ = trechos.sumOf { it.comprimento }),
            cargaNaPonta = cargaNaPonta
        )
    }
}