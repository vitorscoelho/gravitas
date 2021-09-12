package vitorscoelho.gravitas.fundacao.recalque

data class Camada(val profundidade: Double, val moduloDeYoung: Double, val poisson: Double) {
    init {
        require(profundidade > 0.0)
        require(moduloDeYoung > 0.0)
        require(poisson > 0.0)
        require(poisson < 1.0)
    }
}