package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5

data class No2D(val x: Double, val y: Double)

interface ElementoFinito {
    val nos: List<No2D>
    fun matrizDeRigidez(): MatrizDeRigidez
}

class ElementoBarra2D(
    val noInicial: No2D,
    val noFinal: No2D,
    val area: Double,
    val inercia: Double,
    val moduloDeDeformacao: Double,
) {

}