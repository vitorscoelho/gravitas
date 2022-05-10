package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math

import org.apache.commons.math3.analysis.integration.SimpsonIntegrator
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun main() {
    testeBenchmark()
}

@OptIn(ExperimentalTime::class)
fun testeBenchmark() {
    val funcao = { x: Double -> 7.0 * x.pow(6) - x.pow(5) - 5.0 * x.pow(2) + x - 10.0 }
    val a = -1.0
    val b = 2.0
    val delta = 1e-8
    val meu = {
        simpson(
            f = funcao,
            a = a, b = b,
            delta = delta
        )
    }
    val apacheIntegrator = SimpsonIntegrator(1e-8, delta, 2, 64)
    val apache = {
        apacheIntegrator.integrate(
            4000_000,
            funcao,
            a, b
        )
    }

    val (valorMeu, tempoMeu) = measureTimedValue {
        var retorno = 0.0
        repeat(9000) { retorno = meu() }
        retorno
    }
    val (valorApache, tempoApache) = measureTimedValue {
        var retorno = 0.0
        repeat(9000) { retorno = apache() }
        retorno
    }

    println("Meu")
    println("valor= $valorMeu")
    println("tempo= $tempoMeu")
    println("Apache")
    println("valor= $valorApache")
    println("tempo= $tempoApache")

    val razao = tempoApache.toDouble(DurationUnit.NANOSECONDS) / tempoMeu.toDouble(DurationUnit.NANOSECONDS)
    if (tempoMeu < tempoApache) {
        println("'Meu' venceu. É $razao mais rápido.")
    } else {
        println("'Apache' venceu. É ${1.0 / razao} mais rápido.")
    }
}

//https://web.math.utk.edu/~ccollins/refs/Handouts/rich.pdf
//https://github.com/scijs/integrate-adaptive-simpson
fun simpson(
    f: (Double) -> Double,
    a: Double,
    b: Double,
    fa: Double,
    fm: Double,
    fb: Double,
    V0: Double,
    delta: Double,
): Double {
    val h = b - a
    val f1 = f(a + h / 4.0)
    val f2 = f(b - h / 4.0)
    val sl = h * (fa + 4.0 * f1 + fm) / 12.0
    val sr = h * (fm + 4.0 * f2 + fb) / 12.0
    val s2 = sl + sr
    val err = (s2 - V0) / 15.0
    if (err.absoluteValue < delta) return s2 + err
    val m = a + h / 2.0
    val V1 = simpson(f, a, m, fa, f1, fm, sl, delta / 2.0)
    val V2 = simpson(f, m, b, fm, f2, fb, sr, delta / 2.0)
    return V1 + V2
}

fun simpson(
    f: (Double) -> Double,
    a: Double,
    b: Double,
    delta: Double
): Double {
    val fa = f(a)
    val fb = f(b)
    val fm = f((a + b) / 2.0)
    val V0 = (fa + 4.0 * fm + fb) * (b - a) / 6.0
    return simpson(
        f = f,
        a = a, b = b,
        fa = fa, fm = fm, fb = fb,
        V0 = V0,
        delta = delta
    )
}

fun simpson(
    f: (Double) -> Double,
    limitesSubIntervalos: DoubleArray,
    delta: Double
) {
    var total = 0.0
    var index = 1
    while (index <= limitesSubIntervalos.lastIndex) {
        total += simpson(
            f = f,
            a = limitesSubIntervalos[index - 1], b = limitesSubIntervalos[index],
            delta = delta
        )
        index++
    }
}

fun simpson(
    f: (Double) -> DoubleArray,
    size: Int,
    a: Double,
    b: Double,
    fa: DoubleArray,
    fm: DoubleArray,
    fb: DoubleArray,
    V0: DoubleArray,
    delta: DoubleArray,
): DoubleArray {
    val h = b - a
    val f1 = f(a + h / 4.0)
    val f2 = f(b - h / 4.0)
    val sl = DoubleArray(size = size) { index ->
        h * (fa[index] + 4.0 * f1[index] + fm[index]) / 12.0
    }
    val sr = DoubleArray(size = size) { index ->
        h * (fm[index] + 4.0 * f2[index] + fb[index]) / 12.0
    }
    val s2 = DoubleArray(size = size) { index ->
        sl[index] + sr[index]
    }
    val err = DoubleArray(size = size) { index ->
        (s2[index] - V0[index]) / 15.0
    }
    val todosErrosAceitaveis = errosAceitaveis(erro = err, delta = delta)
    if (todosErrosAceitaveis) {
        return DoubleArray(size = size) { index -> s2[index] + err[index] }
    }
    val m = a + h / 2.0
    val delta2 = DoubleArray(size = size) { index -> delta[index] / 2.0 }
    val V1 = simpson(f, size, a, m, fa, f1, fm, sl, delta2)
    val V2 = simpson(f, size, m, b, fm, f2, fb, sr, delta2)
    return DoubleArray(size = size) { index -> V1[index] + V2[index] }
}

fun simpson(
    f: (Double) -> DoubleArray,
    a: Double,
    b: Double,
    delta: DoubleArray
): DoubleArray {
    val fa = f(a)
    val fb = f(b)
    val fm = f((a + b) / 2.0)
    val size = fa.size
    val V0 = DoubleArray(size = size) { index -> (fa[index] + 4.0 * fm[index] + fb[index]) * (b - a) / 6.0 }
    return simpson(
        f = f,
        size = size,
        a = a, b = b,
        fa = fa, fm = fm, fb = fb,
        V0 = V0,
        delta = delta
    )
}

/**
 * @param fSize tamanho da [DoubleArray] que a função [f] retorna
 */
fun simpson(
    f: (Double) -> DoubleArray,
    fSize: Int,
    limitesSubIntervalos: DoubleArray,
    delta: DoubleArray
): DoubleArray {
    val total = DoubleArray(size = fSize)
    val rangeSoma = (0 until fSize)
    var index = 1
    while (index <= limitesSubIntervalos.lastIndex) {
        val respostaParcial = simpson(
            f = f,
            a = limitesSubIntervalos[index - 1], b = limitesSubIntervalos[index],
            delta = delta
        )
        rangeSoma.forEach { total[it] += respostaParcial[it] }
        index++
    }
    return total
}

fun simpsonTeste(
    f: (Double) -> Double,
    a: Double,
    b: Double,
    delta: Double
): Double {
    val fArray = { x: Double -> doubleArrayOf(f(x)) }
    val deltaArray = doubleArrayOf(delta)
    return simpson(
        f = fArray,
        a = a, b = b,
        delta = deltaArray
    ).first()
}
