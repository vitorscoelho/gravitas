package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov2

import org.apache.commons.math3.linear.*
import kotlin.math.absoluteValue
import kotlin.math.pow

//Exemplo da página 114 do livro de MEF do Luiz Eloy Vaz
private data class No2D(val x: Double, val y: Double)

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

private class ElementoCST(no1: No2D, no2: No2D, no3: No2D, val secao: Secao) {
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
    fun matrizB(): RealMatrix {
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

    val elementos = run {
        val nos = listOf(
            No2D(x = 0.0, y = 0.0),
            No2D(x = 0.0, y = 1.0),
            No2D(x = 2.0, y = 0.0),
            No2D(x = 2.0, y = 1.0),
            No2D(x = 4.0, y = 0.0),
            No2D(x = 4.0, y = 1.0),
        )
        listOf(
            ElementoCST(no1 = nos[0], no2 = nos[3], no3 = nos[1], secao = secao),
            ElementoCST(no1 = nos[0], no2 = nos[2], no3 = nos[3], secao = secao),
            ElementoCST(no1 = nos[2], no2 = nos[5], no3 = nos[3], secao = secao),
            ElementoCST(no1 = nos[2], no2 = nos[4], no3 = nos[5], secao = secao),
        )
    }
//    val matrizDeIncidencia = arrayOf(
//        intArrayOf(0, 3, 1),
//        intArrayOf(0, 2, 3),
//        intArrayOf(2, 5, 3),
//        intArrayOf(2, 4, 5),
//    )
//    val matrizDeIncidencia = elementos.mapIndexed { index, elementoCST ->
//        val indiceNo1 = nos.indexOf(elementoCST.nos[0])
//        val indiceNo2 = nos.indexOf(elementoCST.nos[1])
//        val indiceNo3 = nos.indexOf(elementoCST.nos[2])
//        intArrayOf(indiceNo1, indiceNo2, indiceNo3)
//    }
    val nosDaEstrutura = elementos
        .asSequence()
        .flatMap { it.nos }
        .distinct()
        .mapIndexed { index, no2D -> Pair(no2D, index) }
        .toMap()

    val qtdGrausDeLiberdade = nosDaEstrutura.size * 2
    val matrizDeRigidezGlobal = Array2DRowRealMatrix(qtdGrausDeLiberdade, qtdGrausDeLiberdade)
    elementos
//        .map { it.matrizKe() }
        .forEachIndexed { index, elementoCST ->
//            val numeracaoGrausDeLiberdade = intArrayOf(
//                2 * matrizDeIncidencia[index][0], 2 * matrizDeIncidencia[index][0] + 1,
//                2 * matrizDeIncidencia[index][1], 2 * matrizDeIncidencia[index][1] + 1,
//                2 * matrizDeIncidencia[index][2], 2 * matrizDeIncidencia[index][2] + 1,
//            )
//            val indiceNo1 = nosDaEstrutura[elementoCST.nos[0]]!!//nos.indexOf(elementoCST.no1)
//            val indiceNo2 = nosDaEstrutura[elementoCST.nos[1]]!!
//            val indiceNo3 = nosDaEstrutura[elementoCST.nos[2]]!!
//            val numeracaoGrausDeLiberdade = intArrayOf(
//                2 * indiceNo1, 2 * indiceNo1 + 1,
//                2 * indiceNo2, 2 * indiceNo2 + 1,
//                2 * indiceNo3, 2 * indiceNo3 + 1,
//            )
            val numeracaoGrausDeLiberdade = elementoCST.nos.flatMap { no ->
                val indiceNo = nosDaEstrutura[no]!!
                listOf(2 * indiceNo, 2 * indiceNo + 1)
            }
            numeracaoGrausDeLiberdade.forEach { print("${it}, ") }
            println()
            val ke = elementoCST.matrizKe()
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
    val indiceDOFForca1 = 2 * nosDaEstrutura[elementos[3].nos[1]]!!
    val indiceDOFForca2 = 2 * nosDaEstrutura[elementos[3].nos[2]]!!
    val vetorDeForcasNodais: RealMatrix = OpenMapRealMatrix(12, 1).apply {
        setEntry(indiceDOFForca1, 0, 10.0)
        setEntry(indiceDOFForca2, 0, 10.0)
    }
    val indiceDOFRestringido1 = 2 * nosDaEstrutura[elementos[0].nos[0]]!!
    val indiceDOFRestringido2 = 2 * nosDaEstrutura[elementos[0].nos[0]]!! + 1
    val indiceDOFRestringido3 = 2 * nosDaEstrutura[elementos[0].nos[2]]!!
    val direcoesRestringidas = intArrayOf(indiceDOFRestringido1, indiceDOFRestringido2, indiceDOFRestringido3)
    val matrizDeRigidezGlobalConsiderandoRestricoesDeApoio: RealMatrix = matrizDeRigidezGlobal.copy().apply {
        direcoesRestringidas.forEach { index ->
            setEntry(index, index, getEntry(index, index) * 10e12)
        }
    }
    val vetorDeslocamentosNodais =
        MatrixUtils.inverse(matrizDeRigidezGlobalConsiderandoRestricoesDeApoio) * vetorDeForcasNodais
    vetorDeslocamentosNodais.imprimir()

    //Checagem da resposta
    val deslocamentoRespostaLivro = mapOf(
        No2D(x = 0.0, y = 0.0) to Deslocamento(0.0, -0.0),
        No2D(x = 0.0, y = 1.0) to Deslocamento(0.0, -0.0002),
        No2D(x = 2.0, y = 0.0) to Deslocamento(0.002, -0.0),
        No2D(x = 2.0, y = 1.0) to Deslocamento(0.002, -0.0002),
        No2D(x = 4.0, y = 0.0) to Deslocamento(0.004, 0.0),
        No2D(x = 4.0, y = 1.0) to Deslocamento(0.004, -0.0002),
    )
//    vetorDeslocamentoResposta.forEachIndexed { index, valor ->
//        val diferenca = vetorDeslocamentosNodais.getEntry(index, 0) - valor
//        check(diferenca.absoluteValue <= 10e-8)
//    }
    nosDaEstrutura.forEach { (no, indiceNo) ->
        val dofX = 2 * indiceNo
        val dofY = 2 * indiceNo + 1
        val deslocamentoX = vetorDeslocamentosNodais.getEntry(dofX, 0)
        val deslocamentoY = vetorDeslocamentosNodais.getEntry(dofY, 0)
        val deslocamentoLivro = deslocamentoRespostaLivro[no]!!
        val diferencaX = deslocamentoX - deslocamentoLivro.deltaX
        val diferencaY = deslocamentoY - deslocamentoLivro.deltaY
        check(diferencaX.absoluteValue <= 10e-8)
        check(diferencaY.absoluteValue <= 10e-8)
    }
}

private data class Deslocamento(val deltaX: Double, val deltaY: Double)

private enum class DOF { UX, UY, UZ, RX, RY, RZ; }

private interface MatrizDeRigidez {
    fun valor(noEfeito: No2D, grauDeLiberdadeEfeito: DOF, noCausa: No2D, grauDeLiberdadeCausa: DOF): Double

    fun paraCadaValor(op: (noEfeito: No2D, grauDeLiberdadeEfeito: DOF, noCausa: No2D, grauDeLiberdadeCausa: DOF) -> Unit)
}

private interface ResultadoAnalise {
    fun deslocamento(no: No2D, dof: DOF): Double
    fun deslocamento(no: No2D): Map<DOF, Double>
}

private interface Analise {
    fun solve(): ResultadoAnalise
}

private interface InterfaceElementoFinito {
    fun matrizDeRigidez(): MatrizDeRigidez
}

private interface Carga {
    val magnitude: Double
    val dof: DOF
}

private class ImplementacaoAnalise(
    val estrutura: List<InterfaceElementoFinito>,
    val carga: Map<No2D, List<Carga>>
) : Analise {
    override fun solve(): ResultadoAnalise {
        TODO()
    }
}