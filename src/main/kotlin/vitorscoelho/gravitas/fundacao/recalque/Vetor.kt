package vitorscoelho.gravitas.fundacao.recalque

data class Vetor2D(val x: Double, val y: Double) {
    fun to3D(z: Double) = Vetor3D(x = x, y = y, z = z)
}

data class Vetor3D(val x: Double, val y: Double, val z: Double) {
    fun plus(deltaX: Double = 0.0, deltaY: Double = 0.0, deltaZ: Double) =
        Vetor3D(x = x + deltaX, y = y + deltaY, z = z + deltaZ)
}