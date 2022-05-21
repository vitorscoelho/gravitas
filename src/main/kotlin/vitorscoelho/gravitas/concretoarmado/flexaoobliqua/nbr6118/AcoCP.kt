package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.nbr6118

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.FuncaoTensaoDeformacao
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math.interpolarY
import kotlin.math.absoluteValue
import kotlin.math.sign

class AcoCP(val fpyk: Double, val fptk: Double, val gamaS: Double, val moduloDeDeformacao: Double, val euk: Double) {
    init {
        require(fpyk > 0.0) { "|fpyk| deve ser maior que 0" }
        require(fptk > fpyk) { "|fptk| deve ser maior que |fpyk|" }
        require(gamaS > 0.0) { "|gamaS| deve ser maior que 0" }
        require(moduloDeDeformacao > 0.0) { "|moduloDeDeformacao| deve ser maior que 0" }
    }

    val fpyd = fpyk / gamaS
    val fptd = fptk / gamaS
    val eyd = fpyd / moduloDeDeformacao

    val funcaoTensaoDeformacaoELU: FuncaoTensaoDeformacao = FuncaoTensaoDeformacaoAcoCP(
        fpyd = fpyd, fptd = fptd, moduloDeDeformacao = moduloDeDeformacao, eyd = eyd, euk = euk
    )
}

class FuncaoTensaoDeformacaoAcoCP(
    val fpyd: Double,
    val fptd: Double,
    val moduloDeDeformacao: Double,
    val eyd: Double,
    val euk: Double
) :
    FuncaoTensaoDeformacao {
    override val deformacoesInflexao = listOf(-eyd, -euk, eyd, euk)

    override fun tensao(deformacao: Double): Double {
        return if (deformacao.absoluteValue < eyd) {
            deformacao * moduloDeDeformacao
        } else {
            deformacao.sign * interpolarY(
                x = deformacao.absoluteValue,
                x1 = eyd, y1 = fpyd,
                x2 = euk, y2 = fptd
            )
        }
    }
}