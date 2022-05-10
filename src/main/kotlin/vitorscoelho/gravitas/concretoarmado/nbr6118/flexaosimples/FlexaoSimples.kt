package vitorscoelho.gravitas.concretoarmado.nbr6118.flexaosimples

import kotlin.math.*

class CalculadoraFlexaoSimples(
    val concreto: Concreto, val aco: Aco,
    val ksiLim: Double,
    val b: Double, val h: Double, val d: Double, val dLinha: Double
) {
    init {
        require(ksiLim > 0.0 && ksiLim < 1.0) { "|ksiLim| deve ser maior que 0 e menor que 1" }
        require(b > 0.0) { "|b| deve ser maior que 0, mas é igual a $b" }
        require(h > 0.0) { "|h| deve ser maior que 0, mas é igual a $h" }
        require(d > 0.0) { "|d| deve ser maior que 0, mas é igual a $d" }
        require(dLinha >= 0.0) { "|dLinha| deve ser maior ou igual a 0, mas é igual a $dLinha" }
        require(d > dLinha) { "|d| deve ser maior que |dLinha|" }
    }

    fun dimensionar(momentoDeCalculo: Double): ResultadosFlexaoSimples {
        require(momentoDeCalculo >= 0.0) { "|momentoDeCalculo| deve ser maior ou igual a 0" }
        val mi = momentoDeCalculo / (concreto.sigmaCd * b * d.pow(2))
        val precisaDeArmaduraDeCompressao: Boolean
        val ksiAdotado: Double
        if (mi > 0.5) {
            precisaDeArmaduraDeCompressao = true
            ksiAdotado = ksiLim
        } else {
            val ksiCalculado = (1.0 - sqrt(1.0 - 2.0 * mi)) / concreto.lambda
            precisaDeArmaduraDeCompressao = (ksiCalculado > ksiLim)
            ksiAdotado = min(ksiCalculado, ksiLim)
        }
        val xLN = ksiAdotado * d
        val forcaDeCompressaoNoConcreto = concreto.sigmaCd * b * concreto.lambda * xLN
        val dominio: String = dominio(xLN = xLN)
        val deformacaoArmaduraTracionada = deformacao(xLN = xLN, profundidade = d)
        val tensaoArmaduraTracionada = aco.tensao(deformacaoArmaduraTracionada)
        if (!precisaDeArmaduraDeCompressao) {
            val forcaArmaduraTracionada = -forcaDeCompressaoNoConcreto
            val areaDeAcoTracionada = forcaArmaduraTracionada / tensaoArmaduraTracionada
            return ResultadosFlexaoSimples(
                xLN = xLN, ksi = ksiAdotado, dominio = dominio,
                deformacaoArmaduraComprimida = 0.0, deformacaoArmaduraTracionada = deformacaoArmaduraTracionada,
                tensaoArmaduraComprimida = 0.0, tensaoArmaduraTracionada = tensaoArmaduraTracionada,
                forcaArmaduraComprimida = 0.0, forcaArmaduraTracionada = forcaArmaduraTracionada,
                areaDeAcoArmaduraComprimida = 0.0, areaDeAcoArmaduraTracionada = areaDeAcoTracionada
            )
        } else {
            val deformacaoArmaduraComprimida = deformacao(xLN = xLN, profundidade = dLinha)
            require(deformacaoArmaduraComprimida > 0.0) { "Impossível dimensionar, pois a armadura de compressão está abaixo da linha neutra" }
            val forcaArmaduraComprimida =
                (momentoDeCalculo - forcaDeCompressaoNoConcreto * (d - 0.5 * concreto.lambda * xLN)) / (d - dLinha)
            val forcaArmaduraTracionada = -(forcaDeCompressaoNoConcreto + forcaArmaduraComprimida)
            val areaDeAcoTracionada = forcaArmaduraTracionada / tensaoArmaduraTracionada
            val tensaoArmaduraComprimida = aco.tensao(deformacaoArmaduraComprimida)
            val tensaoNoConcretoNaRegiaoDaArmaduraComprimida =
                if (dLinha > xLN * concreto.lambda) 0.0 else concreto.sigmaCd
            require(tensaoArmaduraComprimida > tensaoNoConcretoNaRegiaoDaArmaduraComprimida) {
                "Impossível encontrar uma área de aço para a armadura de compressão, pois a área de aço teria que ser maior do que a área de concreto"
            }
            val areaDeAcoComprimida =
                forcaArmaduraComprimida / (tensaoArmaduraComprimida - tensaoNoConcretoNaRegiaoDaArmaduraComprimida)
            return ResultadosFlexaoSimples(
                xLN = xLN,
                ksi = ksiAdotado,
                dominio = dominio,
                deformacaoArmaduraComprimida = deformacaoArmaduraComprimida,
                deformacaoArmaduraTracionada = deformacaoArmaduraTracionada,
                tensaoArmaduraComprimida = tensaoArmaduraComprimida,
                tensaoArmaduraTracionada = tensaoArmaduraTracionada,
                forcaArmaduraComprimida = forcaArmaduraComprimida,
                forcaArmaduraTracionada = forcaArmaduraTracionada,
                areaDeAcoArmaduraComprimida = areaDeAcoComprimida,
                areaDeAcoArmaduraTracionada = areaDeAcoTracionada
            )
        }
    }

    private val xLNEntreDominio2e3: Double by lazy { -d * concreto.ecu / (ESTU - concreto.ecu) }
    private val xLNEntreDominio3e4: Double by lazy { d * concreto.ecu / (aco.eyd + concreto.ecu) }

    private fun dominio(xLN: Double): String {
        return when {
            (xLN <= 0.0) -> "1"
            (xLN <= xLNEntreDominio2e3) -> "2"
            //TODO ver a possibilidade de xLNEntreDominio2e3 ser maior que dos domínios que deveriam vir a seguir
            (xLN <= xLNEntreDominio3e4) -> "3"
            (xLN <= d) -> "4"
            (xLN <= h) -> "4a"
            else -> "5"
        }
    }

    private fun deformacao(xLN: Double, profundidade: Double): Double {
        if (xLN <= xLNEntreDominio2e3) return (profundidade - xLN) * ESTU / (d - xLN)
        return -(profundidade - xLN) * concreto.ecu / xLN
    }

    companion object {
        const val ESTU = -10.0 / 1_000.0

        fun ksiLim(fck: Double): Double {
            require(fck > 0.0 && fck <= 9.0)
            return if (fck <= 5.0) 0.45 else 0.35
        }

        fun ksiLim(fck: Double, coeficienteDeRedistribuicao: Double, estruturaDeNosMoveis: Boolean): Double {
            require(fck > 0.0 && fck <= 9.0)
            val coeficienteDeRedistribuicaoMinimoLimite = if (estruturaDeNosMoveis) 0.9 else 0.75
            require(coeficienteDeRedistribuicao >= coeficienteDeRedistribuicaoMinimoLimite)
            val parametroParaEquacao = if (fck <= 5.0) 0.44 else 0.56
            return (coeficienteDeRedistribuicao - parametroParaEquacao) / 1.25
        }
    }
}

data class ResultadosFlexaoSimples(
    val xLN: Double,
    val ksi: Double,
    val dominio: String,
    val deformacaoArmaduraComprimida: Double,
    val deformacaoArmaduraTracionada: Double,
    val tensaoArmaduraComprimida: Double,
    val tensaoArmaduraTracionada: Double,
    val forcaArmaduraComprimida: Double,
    val forcaArmaduraTracionada: Double,
    val areaDeAcoArmaduraComprimida: Double,
    val areaDeAcoArmaduraTracionada: Double
)