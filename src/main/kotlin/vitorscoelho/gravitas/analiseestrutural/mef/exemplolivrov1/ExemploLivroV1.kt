package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov1

import org.apache.commons.math3.linear.*
import kotlin.math.absoluteValue
import kotlin.math.pow

//Exemplo da página 114 do livro de MEF do Luiz Eloy Vaz
private class No2D(val x: Double, val y: Double)

private class Secao(val espessura: Double, val moduloDeElasticidade: Double, val poisson: Double) {
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

private class ElementoCST(val no1: No2D, val no2: No2D, val no3: No2D, val secao: Secao) {
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
    fun matrizB(): RealMatrix {
        val n1x = (no2.y - no3.y) / (2.0 * area)
        val n1y = (no3.x - no2.x) / (2.0 * area)
        val n2x = (no3.y - no1.y) / (2.0 * area)
        val n2y = (no1.x - no3.x) / (2.0 * area)
        val n3x = (no1.y - no2.y) / (2.0 * area)
        val n3y = (no2.x - no1.x) / (2.0 * area)
        return Array2DRowRealMatrix(
            arrayOf(
                doubleArrayOf(n1x, 0.0, n2x, 0.0, n3x, 0.0),
                doubleArrayOf(0.0, n1y, 0.0, n2y, 0.0, n3y),
                doubleArrayOf(n1y, n1x, n2y, n2x, n3y, n3x),
            )
        )
    }

    /**Matriz de rigidez do elemento*/
    fun matrizKe(): RealMatrix {
        val b = matrizB()
        val bt = b.transpose()
        val c = secao.matrizC()
        return bt * c * b * area * secao.espessura
    }
}

val matrixFormat = RealMatrixFormat("", "", "", "\n", "", ", ")
fun RealMatrix.imprimir() = println(matrixFormat.format(this))
operator fun RealMatrix.times(other: RealMatrix): RealMatrix = this.multiply(other)
operator fun RealMatrix.times(scalar: Double): RealMatrix = this.scalarMultiply(scalar)
operator fun Double.times(matrix: RealMatrix): RealMatrix = matrix * this

fun main() {
    val secao = Secao(espessura = 1.0, moduloDeElasticidade = 20_000.0, poisson = 0.2)
    val nos = listOf(
        No2D(x = 0.0, y = 0.0),
        No2D(x = 0.0, y = 1.0),
        No2D(x = 2.0, y = 0.0),
        No2D(x = 2.0, y = 1.0),
        No2D(x = 4.0, y = 0.0),
        No2D(x = 4.0, y = 1.0),
    )
    val elementos = listOf(
        ElementoCST(no1 = nos[0], no2 = nos[3], no3 = nos[1], secao = secao),
        ElementoCST(no1 = nos[0], no2 = nos[2], no3 = nos[3], secao = secao),
        ElementoCST(no1 = nos[2], no2 = nos[5], no3 = nos[3], secao = secao),
        ElementoCST(no1 = nos[2], no2 = nos[4], no3 = nos[5], secao = secao),
    )
    val matrizDeIncidencia = arrayOf(
        intArrayOf(0, 3, 1),
        intArrayOf(0, 2, 3),
        intArrayOf(2, 5, 3),
        intArrayOf(2, 4, 5),
    )

    val qtdGrausDeLiberdade = nos.size * 2
    val matrizDeRigidezGlobal = Array2DRowRealMatrix(qtdGrausDeLiberdade, qtdGrausDeLiberdade)
    elementos
        .map { it.matrizKe() }
        .forEachIndexed { index, ke ->
            val numeracaoGrausDeLiberdade = intArrayOf(
                2 * matrizDeIncidencia[index][0], 2 * matrizDeIncidencia[index][0] + 1,
                2 * matrizDeIncidencia[index][1], 2 * matrizDeIncidencia[index][1] + 1,
                2 * matrizDeIncidencia[index][2], 2 * matrizDeIncidencia[index][2] + 1,
            )
            (0..5).forEach { linha ->
                (0..5).forEach { coluna ->
                    matrizDeRigidezGlobal.addToEntry(
                        numeracaoGrausDeLiberdade[linha],
                        numeracaoGrausDeLiberdade[coluna],
                        ke.getEntry(linha, coluna)
                    )
                }
            }
        }
    val vetorDeForcasNodais: RealMatrix = OpenMapRealMatrix(12, 1).apply {
        setEntry(8, 0, 10.0)
        setEntry(10, 0, 10.0)
    }
    val direcoesRestringidas = intArrayOf(0, 1, 2)
    val matrizDeRigidezGlobalConsiderandoRestricoesDeApoio: RealMatrix = matrizDeRigidezGlobal.copy().apply {
        direcoesRestringidas.forEach { index ->
            setEntry(index, index, getEntry(index, index) * 10e12)
        }
    }
    val vetorDeslocamentosNodais =
        MatrixUtils.inverse(matrizDeRigidezGlobalConsiderandoRestricoesDeApoio) * vetorDeForcasNodais
    vetorDeslocamentosNodais.imprimir()

    //Checagem da resposta
    val vetorDeslocamentoResposta = doubleArrayOf(
        0.0, -0.0, 0.0, -0.0002, 0.002, -0.0, 0.002, -0.0002, 0.004, 0.0, 0.004, -0.0002
    )
    vetorDeslocamentoResposta.forEachIndexed { index, valor ->
        val diferenca = vetorDeslocamentosNodais.getEntry(index, 0) - valor
        check(diferenca.absoluteValue <= 10e-8)
    }
}