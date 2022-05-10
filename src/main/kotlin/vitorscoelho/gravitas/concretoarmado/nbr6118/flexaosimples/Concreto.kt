package vitorscoelho.gravitas.concretoarmado.nbr6118.flexaosimples

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
}