package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5

import org.apache.commons.math3.linear.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.absoluteValue
import kotlin.math.pow

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

val matrixFormat = RealMatrixFormat(
    "", "", "", "\n", "", ", ",
    DecimalFormat().apply { this.decimalFormatSymbols = DecimalFormatSymbols().apply { decimalSeparator = '.' } }
)

fun RealMatrix.imprimir() = println(matrixFormat.format(this))
operator fun RealMatrix.times(other: RealMatrix): RealMatrix = this.multiply(other)
operator fun RealMatrix.times(scalar: Double): RealMatrix = this.scalarMultiply(scalar)
operator fun Double.times(matrix: RealMatrix): RealMatrix = matrix * this

class ElementoCST(no1: No2D, no2: No2D, no3: No2D, val secao: Secao) : ElementoFinito {
    override val nos = listOf(no1, no2, no3)
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

    override fun matrizDeRigidez(): MatrizDeRigidez {
        val matrizApache6x6 = matrizKe()
        val vetorDeEspalhamento = intArrayOf(0, 2, 6, 8, 12, 14)
        val matrizApache18x18 = OpenMapRealMatrix(18, 18)
        (0..5).forEach { linha ->
            (0..5).forEach { coluna ->
                val linhaFinal = vetorDeEspalhamento[linha]
                val colunaFinal = vetorDeEspalhamento[coluna]
                matrizApache18x18.setEntry(
                    linhaFinal, colunaFinal,
                    matrizApache6x6.getEntry(linha, coluna)
                )
            }
        }
        return object : MatrizDeRigidez {
            override fun valor(indiceLocalDOFEfeito: Int, indiceLocalDOFCausa: Int): Double {
                return matrizApache18x18.getEntry(indiceLocalDOFEfeito, indiceLocalDOFCausa)
            }
        }
    }
}

fun main() {
    val nos = listOf(
        No2D(x = 0.0, y = 0.0),
        No2D(x = 0.0, y = 1.0),
        No2D(x = 2.0, y = 0.0),
        No2D(x = 2.0, y = 1.0),
        No2D(x = 4.0, y = 0.0),
        No2D(x = 4.0, y = 1.0),
    )
    val elementos = run {
        val secao = Secao(espessura = 1.0, moduloDeElasticidade = 20_000.0, poisson = 0.2)
        listOf(
            ElementoCST(no1 = nos[0], no2 = nos[3], no3 = nos[1], secao = secao),
            ElementoCST(no1 = nos[0], no2 = nos[2], no3 = nos[3], secao = secao),
            ElementoCST(no1 = nos[2], no2 = nos[5], no3 = nos[3], secao = secao),
            ElementoCST(no1 = nos[2], no2 = nos[4], no3 = nos[5], secao = secao),
        )
    }
    val restricoes = mapOf(
        elementos[0].nos[0] to listOf(DOF.UX, DOF.UZ),
        elementos[0].nos[2] to listOf(DOF.UX)
    )
    val cargas = mapOf(
        Carga(magnitude = 10.0, dof = DOF.UX) to listOf(elementos[3].nos[1], elementos[3].nos[2]),
    )

    val analise = AnaliseOutraTentativa(
        elementos = elementos, restricoes = restricoes, cargas = cargas,
        dofsAtivos = DOFsAtivos(
            ux = true, uy = false, uz = true,
            rx = false, ry = false, rz = false
        )//TODO testar as outras maneiras
    )

    val resultados = analise.solve()

    //Checagem da resposta
    val deslocamentoZero = Deslocamento(ux = 0.0, uy = 0.0, uz = 0.0, rx = 0.0, ry = 0.0, rz = 0.0)
    val deslocamentoRespostaLivro = mapOf(
        No2D(x = 0.0, y = 0.0) to deslocamentoZero,
        No2D(x = 0.0, y = 1.0) to deslocamentoZero.copy(uz = -0.0002),//Deslocamento(0.0, -0.0002),
        No2D(x = 2.0, y = 0.0) to deslocamentoZero.copy(ux = 0.002),//Deslocamento(0.002, -0.0),
        No2D(x = 2.0, y = 1.0) to deslocamentoZero.copy(ux = 0.002, uz = -0.0002),//Deslocamento(0.002, -0.0002),
        No2D(x = 4.0, y = 0.0) to deslocamentoZero.copy(ux = 0.004),//Deslocamento(0.004, 0.0),
        No2D(x = 4.0, y = 1.0) to deslocamentoZero.copy(ux = 0.004, uz = -0.0002),//Deslocamento(0.004, -0.0002),
    )
    nos.forEachIndexed { index, no ->
        val deslocamentoX = resultados[no]!!.ux
        val deslocamentoZ = resultados[no]!!.uz
        val deslocamentoLivro = deslocamentoRespostaLivro[no]!!
        val diferencaX = deslocamentoX - deslocamentoLivro.ux
        val diferencaZ = deslocamentoZ - deslocamentoLivro.uz
        check(diferencaX.absoluteValue <= 10e-8) {
            "Nó$index X: Programa:$deslocamentoX  Livro:${deslocamentoLivro.ux}"
        }
        check(diferencaZ.absoluteValue <= 10e-8) {
            "Nó$index Z: Programa:$deslocamentoZ  Livro:${deslocamentoLivro.uz}"
        }
    }
}