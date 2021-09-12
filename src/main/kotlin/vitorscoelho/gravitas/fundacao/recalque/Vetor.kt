package vitorscoelho.gravitas.fundacao.recalque

/**
 * @property profundidade IMPORTANTE: positivo para baixo
 */
data class Ponto(val x: Double, val y: Double, val profundidade: Double)

data class Camada(val profundidade: Double, val moduloDeYoung: Double, val poisson: Double)