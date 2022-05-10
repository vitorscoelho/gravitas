package vitorscoelho.gravitas.concretoarmado.flexaoobliqua

/**
 * @property normal esforço normal, em kN. Positivo para compressão.
 * @property momento momento fletor, em kN.cm. Positivo quando comprime a face superior.
 */
data class EsforcosFlexaoReta(val normal: Double, val momento: Double) {
    operator fun minus(outro: EsforcosFlexaoReta) = EsforcosFlexaoReta(
        normal = this.normal - outro.normal, momento = this.momento - outro.momento
    )

    operator fun plus(outro: EsforcosFlexaoReta) = EsforcosFlexaoReta(
        normal = this.normal + outro.normal, momento = this.momento + outro.momento
    )

    operator fun times(valor: Double) = EsforcosFlexaoReta(
        normal = this.normal * valor, momento = this.momento * valor
    )

    operator fun div(valor: Double) = EsforcosFlexaoReta(
        normal = this.normal / valor, momento = this.momento / valor
    )

    companion object {
        val ZERO: EsforcosFlexaoReta by lazy { EsforcosFlexaoReta(normal = 0.0, momento = 0.0) }
    }
}

operator fun Double.times(esforco: EsforcosFlexaoReta) = EsforcosFlexaoReta(
    normal = esforco.normal * this, momento = esforco.momento * this
)

/**
 * @property normal esforço normal, em kN. Positivo para compressão.
 * @property momentoX momento fletor, em kN.cm. Positivo quando comprime a face superior.
 */
data class EsforcosFlexaoObliqua(val normal: Double, val momentoX: Double, val momentoY: Double) {
    operator fun minus(outro: EsforcosFlexaoObliqua) = EsforcosFlexaoObliqua(
        normal = this.normal - outro.normal,
        momentoX = this.momentoX - outro.momentoX,
        momentoY = this.momentoY - outro.momentoY
    )

    operator fun plus(outro: EsforcosFlexaoObliqua) = EsforcosFlexaoObliqua(
        normal = this.normal + outro.normal,
        momentoX = this.momentoX + outro.momentoX,
        momentoY = this.momentoY + outro.momentoY
    )

    operator fun times(valor: Double) = EsforcosFlexaoObliqua(
        normal = this.normal * valor, momentoX = this.momentoX * valor, momentoY = this.momentoY * valor
    )

    operator fun div(valor: Double) = EsforcosFlexaoObliqua(
        normal = this.normal / valor, momentoX = this.momentoX / valor, momentoY = this.momentoY / valor
    )

    companion object {
        val ZERO: EsforcosFlexaoObliqua by lazy { EsforcosFlexaoObliqua(normal = 0.0, momentoX = 0.0, momentoY = 0.0) }
    }
}

operator fun Double.times(esforco: EsforcosFlexaoObliqua) = EsforcosFlexaoObliqua(
    normal = esforco.normal * this, momentoX = esforco.momentoX * this, momentoY = esforco.momentoY * this
)
