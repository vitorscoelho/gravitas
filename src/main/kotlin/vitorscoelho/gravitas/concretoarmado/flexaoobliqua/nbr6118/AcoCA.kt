package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.nbr6118

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.FuncaoTensaoDeformacao
import kotlin.math.absoluteValue
import kotlin.math.sign

class AcoCA(val fyk: Double, val gamaS: Double, val moduloDeDeformacao: Double) {
    init {
        require(fyk > 0.0) { "|fyk| deve ser maior que 0" }
        require(gamaS > 0.0) { "|gamaS| deve ser maior que 0" }
        require(moduloDeDeformacao > 0.0) { "|moduloDeDeformacao| deve ser maior que 0" }
    }

    val esTLim = -10.0 / 1_000
    val fyd = fyk / gamaS
    val eyd = fyd / moduloDeDeformacao

    val funcaoTensaoDeformacaoELU: FuncaoTensaoDeformacao = FuncaoTensaoDeformacaoAcoCA(
        fyd = fyd, eyd = eyd, moduloDeDeformacao
    )
}

class FuncaoTensaoDeformacaoAcoCA(val fyd: Double, val eyd: Double, val moduloDeDeformacao: Double) :
    FuncaoTensaoDeformacao {
    override val deformacoesInflexao = listOf(-eyd, eyd)

    override fun tensao(deformacao: Double): Double {
        return if (deformacao.absoluteValue >= eyd) deformacao.sign * fyd else deformacao * moduloDeDeformacao
    }
}