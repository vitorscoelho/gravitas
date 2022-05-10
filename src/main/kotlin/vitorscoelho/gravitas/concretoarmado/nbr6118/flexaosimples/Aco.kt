package vitorscoelho.gravitas.concretoarmado.nbr6118.flexaosimples

import kotlin.math.absoluteValue
import kotlin.math.sign

class Aco(val fyk: Double, val gamaS: Double, val moduloDeElasticidade: Double) {
    init {
        require(fyk > 0.0){"|fyk| deve ser maior que 0"}
        require(gamaS > 0.0){"|gamaS| deve ser maior que 0"}
        require(moduloDeElasticidade > 0.0){"|moduloDeElasticidade| deve ser maior que 0"}
    }

    val fyd = fyk / gamaS
    val eyd = fyd / moduloDeElasticidade

    fun tensao(deformacao: Double): Double {
        return if (deformacao.absoluteValue >= eyd) deformacao.sign * fyd else deformacao * moduloDeElasticidade
    }
}