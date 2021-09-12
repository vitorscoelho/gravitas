package vitorscoelho.gravitas.fundacao.recalque

data class Forca(val magnitude: Double, val posicao: Vetor3D) {
    val x: Double get() = posicao.x
    val y: Double get() = posicao.y
    val z: Double get() = posicao.z

    init {
        require(magnitude >= 0.0)
        require(posicao.z > 0.0)
    }
}