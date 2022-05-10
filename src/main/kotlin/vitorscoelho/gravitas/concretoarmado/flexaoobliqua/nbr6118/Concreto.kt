package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.nbr6118

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.FuncaoTensaoDeformacao
import kotlin.math.max
import kotlin.math.pow

class Concreto(val fck: Double, val gamaC: Double) {
    init {
        require(fck > 0.0 && fck <= 9.0) { "|fck| deve ser maior que 0 e menor ou igual a 9" }
        require(gamaC > 0.0) { "|gamaC| deve ser maior que 0" }
    }

    val fcd = fck / gamaC
    val lambda: Double
    val sigmaCd: Double
    val ec2: Double
    val ecu: Double

    init {
        val alphaC: Double
        if (fck <= 5.0) {
            this.lambda = 0.8
            alphaC = 0.85
            this.ec2 = 2.0 / 1_000.0
            this.ecu = 3.5 / 1_000.0
        } else {
            this.lambda = 0.8 - (fck * 10.0 - 50.0) / 400.0
            alphaC = 0.85 * (1.0 - (fck * 10.0 - 50.0) / 200.0)
            this.ec2 = (2.0 + 0.085 * (fck * 10.0 - 50.0).pow(0.53)) / 1_000.0
            this.ecu = max(
                this.ec2,
                (2.6 + 35.0 * ((90.0 - fck * 10.0) / 100.0).pow(4)) / 1_000.0
            )
        }
        this.sigmaCd = alphaC * fcd
    }

    val funcaoTensaoDeformacaoEstadio3: FuncaoTensaoDeformacao by lazy {
        FuncaoTensaoDeformacaoConcretoEstadio3(sigmaCd = sigmaCd, ec2 = ec2)
    }

}

class FuncaoTensaoDeformacaoConcretoEstadio1(val moduloDeDeformacao: Double) : FuncaoTensaoDeformacao {
    init {
        require(moduloDeDeformacao > 0.0)
    }

    override val deformacoesInflexao: List<Double> = emptyList()

    override fun tensao(deformacao: Double) = moduloDeDeformacao * deformacao
}

class FuncaoTensaoDeformacaoConcretoEstadio2(val moduloDeDeformacao: Double) : FuncaoTensaoDeformacao {
    init {
        require(moduloDeDeformacao > 0.0)
    }

    override val deformacoesInflexao: List<Double> = listOf(0.0)

    override fun tensao(deformacao: Double) = if (deformacao <= 0.0) 0.0 else moduloDeDeformacao * deformacao
}

class FuncaoTensaoDeformacaoConcretoEstadio3(val sigmaCd: Double, val ec2: Double) : FuncaoTensaoDeformacao {
    init {
        require(sigmaCd > 0.0)
        require(ec2 > 0.0)
    }

    override val deformacoesInflexao: List<Double> = listOf(0.0, ec2)

    override fun tensao(deformacao: Double): Double {
        if (deformacao <= 0.0) return 0.0
        if (deformacao < ec2) return sigmaCd * (1.0 - (1.0 - deformacao / ec2).pow(2))
        return sigmaCd
    }
}