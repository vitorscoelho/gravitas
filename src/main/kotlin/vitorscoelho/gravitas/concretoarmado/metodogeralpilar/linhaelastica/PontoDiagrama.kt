package vitorscoelho.gravitas.concretoarmado.metodogeralpilar.linhaelastica

interface PontoDiagrama {
    val x: Double
    val valorAEsquerda: Double
    val valorAdireita: Double
    val isValorUnico: Boolean get() = valorAEsquerda == valorAdireita

    companion object {
        fun criar(x: Double, valorAEsquerda: Double, valorAdireita: Double): PontoDiagrama {
            return if (valorAEsquerda == valorAdireita) {
                PontoDiagramaValorUnico(x = x, valor = valorAEsquerda)
            } else {
                PontoDiagramaValorDuplo(x = x, valorAEsquerda = valorAEsquerda, valorAdireita = valorAdireita)
            }
        }

        fun criar(x: Double, valor: Double): PontoDiagrama = PontoDiagramaValorUnico(
            x = x, valor = valor
        )
    }
}

private fun PontoDiagrama.descricao(): String {
    return "PontoDiagrama(x=$x, valorAEsquerda=$valorAEsquerda, valorADireita=$valorAdireita)"
}

private class PontoDiagramaValorUnico(override val x: Double, valor: Double) : PontoDiagrama {
    override val valorAEsquerda: Double = valor
    override val valorAdireita: Double get() = valorAEsquerda

    override fun toString() = descricao()
}

private class PontoDiagramaValorDuplo(
    override val x: Double,
    override val valorAEsquerda: Double,
    override val valorAdireita: Double
) : PontoDiagrama {
    override fun toString() = descricao()
}
