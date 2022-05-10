package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.LUDecomposition
import kotlin.math.*

/*
Material de estudo:
http://www.mcct.sites.uff.br/wp-content/uploads/sites/454/2018/09/Dissertacao_17.pdf
https://www.ime.unicamp.br/~valle/Teaching/MS211/Aula11-Apresenta%c3%a7%c3%a3o.pdf
https://www.ime.unicamp.br/~valle/Teaching/MS211/Aula12.pdf
https://ufsj.edu.br/portal-repositorio/File/nepomuceno/mn/09MN_Derivacao.pdf
https://www.uio.no/studier/emner/matnat/math/MAT-INF1100/h08/kompendiet/diffint.pdf
https://en.wikipedia.org/wiki/Numerical_differentiation
https://www.cantorsparadise.com/the-best-numerical-derivative-approximation-formulas-998703380948
http://www.karenkopecky.net/Teaching/eco613614/Notes_NumericalDifferentiation.pdf
 */

val funcao = { x: DoubleArray ->
    doubleArrayOf(
        x[4] * x[0].pow(2) + x[0] - 5.0,
        x[4] * x[1].pow(2) + x[1] - x[0],
        x[4] * x[2].pow(2) + x[2] - x[1],
        x[4] * x[3].pow(2) + x[3] - x[2],
        0.25 * x[4] + 0.5 - x[3],
    )
}

val funcao2 = { x: DoubleArray ->
    doubleArrayOf(
        x[0] + x[1] - 3.0,
        x[0].pow(2) + x[1].pow(2) - 9.0
    )
}

val funcao2Jacobiano = { x: DoubleArray ->
    arrayOf(
        doubleArrayOf(1.0, 1.0),
        doubleArrayOf(2.0 * x[0], 2.0 * x[1])
    )
}

fun main() {
    val resposta = funcao(
        doubleArrayOf(
            2.22616547,
            1.29194119,
            0.86913559,
            0.63992834,
            0.55971335,
        )
    )
//    resposta.forEach { println(it) }
//    val jacobianFunction = JacobianFunction(
//        MultivariateDifferentiableFunction(
//
//        )
//    )
    val resposta2 = newtonRaphson(
        funcao = funcao2,
//        funcaoJacobiano = funcao2Jacobiano,
        aproximacaoInicial = doubleArrayOf(1.0, 5.0),
        nMaximoIteracoes = 300,
        tolerancia = doubleArrayOf(0.00001, 0.00001)
    )
    resposta2.forEach { print("$it     ") };println()

    println(
        derivada(
            x = 2.0,
            fx = { x -> x * E.pow(x) }
        )
    )
    println(
        derivadaParcial(
            indiceVariavel = 1,
            x = doubleArrayOf(1.0, 5.0),
            fx = { x -> x[0].pow(2) + x[1].pow(2) - 9.0 }
        )
    )
    val jacobiana = jacobiana(
        x = doubleArrayOf(1.0, 5.0),
        fx = funcao2
    )
    jacobiana.indices.forEach { linha ->
        jacobiana.indices.forEach { coluna ->
            print("${jacobiana[linha][coluna]}    ")
        }
        println()
    }
}

fun newtonRaphson(
    funcao: (DoubleArray) -> DoubleArray,
    aproximacaoInicial: DoubleArray,
    nMaximoIteracoes: Int,
    tolerancia: DoubleArray
): DoubleArray {
    return newtonRaphson(
        funcao = funcao,
        funcaoJacobiano = { x -> jacobiana(x = x, fx = funcao) },
        aproximacaoInicial = aproximacaoInicial,
        nMaximoIteracoes = nMaximoIteracoes,
        tolerancia = tolerancia
    )
}

fun newtonRaphson(
    funcao: (DoubleArray) -> DoubleArray,
    funcaoJacobiano: (DoubleArray) -> Array<DoubleArray>,
    aproximacaoInicial: DoubleArray,
    nMaximoIteracoes: Int,
    tolerancia: DoubleArray
): DoubleArray {
    var iteracao = 0
    val aproximacao = aproximacaoInicial.copyOf()
    while (true) {
        iteracao++
        val jacobiano = funcaoJacobiano(aproximacao)
        val apacheJacobiano = Array2DRowRealMatrix(jacobiano)
        val fx = funcao(aproximacao)

        val errosAceitaveis = errosAceitaveis(erro = fx, delta = tolerancia)
        if (errosAceitaveis) break

        val apacheFx = ArrayRealVector(fx)
        val solver = LUDecomposition(apacheJacobiano).solver
        val solution = solver.solve(apacheFx)
        aproximacao.indices.forEach { index ->
            aproximacao[index] = aproximacao[index] - solution.getEntry(index)
        }

        if (iteracao > nMaximoIteracoes) throw IllegalStateException("Limite de iterações, $nMaximoIteracoes, excedido.")
    }
    return aproximacao
}

private const val deltaDerivada = 0.0000001
fun derivada(x: Double, fx: (x: Double) -> Double): Double {
    val xNextUp = x + deltaDerivada
    val xNextDown = x - deltaDerivada
    val h = xNextUp - xNextDown
    val dFx = fx(xNextUp) - fx(xNextDown)
    return dFx / h
}

fun derivadaParcial(indiceVariavel: Int, x: DoubleArray, fx: (x: DoubleArray) -> Double): Double {
    val variavelNextUp = x[indiceVariavel] + deltaDerivada
    val variavelNextDown = x[indiceVariavel] - deltaDerivada
    val h = variavelNextUp - variavelNextDown
    val valoresNextUp = x.copyOf()
    valoresNextUp[indiceVariavel] = variavelNextUp
    val valoresNextDown = x.copyOf()
    valoresNextDown[indiceVariavel] = variavelNextDown
    val dFx = fx(valoresNextUp) - fx(valoresNextDown)
    return dFx / h
}

fun jacobiana(x: DoubleArray, fx: (x: DoubleArray) -> DoubleArray): Array<DoubleArray> {
    val resposta = Array(size = x.size) { DoubleArray(size = x.size) }
    x.indices.forEach { linha ->
        x.indices.forEach { coluna ->
            resposta[linha][coluna] = derivadaParcial(
                indiceVariavel = coluna,
                x = x,
                fx = { x -> fx(x)[linha] }
            )
        }
    }
    return resposta
}