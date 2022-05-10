package vitorscoelho.gravitas.analiseestrutural.mef.exemploLivroV4

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.RealMatrix
import vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5.imprimir
import kotlin.math.absoluteValue
import kotlin.math.pow

data class No2D(val x: Double, val y: Double)

interface MatrizDeRigidez {
    fun valor(noEfeito: No2D, dofEfeito: DOF, noCausa: No2D, dofCausa: DOF): Double
//    fun paraCadaValor(op: (noEfeito: No2D, grauDeLiberdadeEfeito: DOF, noCausa: No2D, grauDeLiberdadeCausa: DOF) -> Unit)
}

class Secao(val espessura: Double, val moduloDeElasticidade: Double, val poisson: Double) {
    /**
     * Matriz constitutiva para o estado plano de tensão.
     */
    fun matrizC(): RealMatrix = Array2DRowRealMatrix(
        arrayOf(
            doubleArrayOf(1.0, poisson, 0.0),
            doubleArrayOf(poisson, 1.0, 0.0),
            doubleArrayOf(0.0, 0.0, (1.0 - poisson) / 2.0),
        )
    ).scalarMultiply(moduloDeElasticidade / (1.0 - poisson.pow(2)))
}

class ElementoCST(no1: No2D, no2: No2D, no3: No2D, val secao: Secao) {
    val nos = listOf(no1, no2, no3)
    val area = 0.5 * LUDecomposition(
        Array2DRowRealMatrix(
            arrayOf(
                doubleArrayOf(1.0, 1.0, 1.0),
                doubleArrayOf(no1.x, no2.x, no3.x),
                doubleArrayOf(no1.y, no2.y, no3.y),
            )
        )
    ).determinant.absoluteValue

    /**Matriz de compatibilidade cinemática*/
    private fun matrizB(): RealMatrix {
        val n1x = (nos[1].y - nos[2].y) / (2.0 * area)
        val n1y = (nos[2].x - nos[1].x) / (2.0 * area)
        val n2x = (nos[2].y - nos[0].y) / (2.0 * area)
        val n2y = (nos[0].x - nos[2].x) / (2.0 * area)
        val n3x = (nos[0].y - nos[1].y) / (2.0 * area)
        val n3y = (nos[1].x - nos[0].x) / (2.0 * area)
        return Array2DRowRealMatrix(
            arrayOf(
                doubleArrayOf(n1x, 0.0, n2x, 0.0, n3x, 0.0),
                doubleArrayOf(0.0, n1y, 0.0, n2y, 0.0, n3y),
                doubleArrayOf(n1y, n1x, n2y, n2x, n3y, n3x),
            )
        )
    }

    /**Matriz de rigidez do elemento*/
    private fun matrizKe(): RealMatrix {
        val b = matrizB()
        val bt = b.transpose()
        val c = secao.matrizC()
        return bt * c * b * area * secao.espessura
    }

    fun matrizDeRigidezKe(): MatrizDeRigidez {
        //fun valor(noEfeito: No2D, grauDeLiberdadeEfeito: DOF, noCausa: No2D, grauDeLiberdadeCausa: DOF): Double
        val matrizApache = matrizKe()
        fun indiceLocalDOF(dof: DOF) = when (dof) {
            DOF.UX -> 0
            DOF.UZ -> 1
            else -> throw IllegalArgumentException("DOF local inválido no elemento")
        }
        return object : MatrizDeRigidez {
            override fun valor(
                noEfeito: No2D, dofEfeito: DOF,
                noCausa: No2D, dofCausa: DOF
            ): Double {
                if (!nos.contains(noEfeito) || !nos.contains(noCausa)) return 0.0
                if (dofEfeito != DOF.UX && dofEfeito != DOF.UZ) return 0.0
                if (dofCausa != DOF.UX && dofCausa != DOF.UZ) return 0.0
                val indiceNoEfeito = nos.indexOf(noEfeito)
                val indiceNoCausa = nos.indexOf(noCausa)

                val indiceDOFEfeito: Int = 2 * indiceNoEfeito + indiceLocalDOF(dofEfeito)
                val indiceDOFCausa: Int = 2 * indiceNoCausa + indiceLocalDOF(dofCausa)

                val valor = matrizApache.getEntry(indiceDOFEfeito, indiceDOFCausa)
                return valor
            }
        }
    }
}
