package vitorscoelho.gravitas.fundacao.recalque

class Trapezio(val yInferior: Double, val larguraInferior: Double, val larguraSuperior: Double, val altura: Double) {
    init {
        require(larguraInferior >= 0.0)
        require(larguraSuperior >= 0.0)
        require(altura >= 0.0)
    }

    val ySuperior: Double get() = yInferior + altura
    val ycg: Double
    val area: Double

    init {
        if (altura == 0.0 || (larguraInferior == 0.0 && larguraSuperior == 0.0)) {
            this.area = 0.0
            this.ycg = yInferior
        } else {
            this.area = altura * (larguraInferior + larguraSuperior) / 2.0
            this.ycg =
                yInferior + altura * (2.0 * larguraSuperior + larguraInferior) / (3.0 * (larguraSuperior + larguraInferior))
        }
    }

    fun largura(y: Double): Double =
        (y - yInferior) * (larguraSuperior - larguraInferior) / altura + larguraInferior
}