package vitorscoelho.gravitas.analiseestrutural.rigidezdireta

sealed class CondicaoDeExtremidade

object RIGIDA : CondicaoDeExtremidade()

object ARTICULADA : CondicaoDeExtremidade()

class FLEXIVEL(val coeficienteDeMola: Double) : CondicaoDeExtremidade() {
    init {
        require(coeficienteDeMola >= 0.0)
    }
}