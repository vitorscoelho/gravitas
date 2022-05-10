package vitorscoelho.gravitas.analiseestrutural.mef

interface Vetor {
    val x: Double
    val y: Double
    val z: Double
}

fun criarVetor(x: Double, y: Double, z: Double): Vetor {
    if (z == 0.0) {
        if (y == 0.0) return Vetor1D(x = x)
        return Vetor2D(x = x, y = y)
    }
    return Vetor3D(x = x, y = y, z = z)
}

class Vetor1D(override val x: Double) : Vetor {
    override val y: Double get() = 0.0
    override val z: Double get() = 0.0
}

class Vetor2D(override val x: Double, override val y: Double) : Vetor {
    override val z: Double get() = 0.0
}

class Vetor3D(override val x: Double, override val y: Double, override val z: Double) : Vetor

class No(x: Double, y: Double, z: Double) : Vetor by criarVetor(x = x, y = y, z = z)

data class GrauDeLiberdade(val no: No, val deslocabilidade: Int)

/**
 * @property efeito grau de liberdade onde surge a força devido ao deslocamento unitário em [causa]
 * @property causa grau de liberdade em que é aplicado o deslocamento unitário
 * @property rigidez força que ocorre em [efeito]
 */
data class Colaboracao(val efeito: GrauDeLiberdade, val causa: GrauDeLiberdade, val rigidez: Vetor)

interface ElementoFinito {
    val nos: List<No>

    fun colaboracao(): Sequence<Colaboracao>
}