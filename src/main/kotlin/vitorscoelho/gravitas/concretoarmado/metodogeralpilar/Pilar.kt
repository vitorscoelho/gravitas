package vitorscoelho.gravitas.concretoarmado.metodogeralpilar

import vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5.Carga
import vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5.DOF
import vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5.No2D
import vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5.analiseSolverLinear
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.EsforcosFlexaoReta
import vitorscoelho.gravitas.utils.gpa
import kotlin.math.pow

fun main() {
    exemploTesteJorgeLuizCeccon()
}

fun sd() {
    val vao = 350
    val gamaF3 = 1.0

    val diagramaDeEsforcos = TrechoGrafico(
        ponto1 = PontoGrafico(
            x = 0.0,
            valor = Esforco(
                normal = 570.0,
                momento = (0.29 * 10.0 * vao + 0.43 * 10.0 * 100.0) / gamaF3,
                cortante = 0.0,
            ),
        ),
        ponto2 = PontoGrafico(
            x = vao.toDouble(),
            valor = Esforco(
                normal = 570.0,
                momento = (0.43 * 10.0 * 100.0) / gamaF3,
                cortante = 0.0,
            ),
        ),
        interpolador = interpoladorEsforco
    )

    val diagramasDeEsforcos = Grafico(
        pontos = (0..vao step 10)
            .map { it.toDouble() }
            .map { x -> diagramaDeEsforcos.ponto(x = x) },
        interpolador = interpoladorEsforco
    )

    val secao = { x: Double ->
        SecaoPilar(
            area = 40.0 * 26.0,
            inercia = 40.0 * 26.0.pow(3) / 12,
            moduloDeDeformacao = 25.gpa,
        )
    }

    val esforcosResistentes = { x: Double ->
        { deformacaoCG: Double, curvatura: Double ->
            val ea = 40.0 * 26.0 * 25.gpa
            val ei = 576.5 * 10.0 * 100.0.pow(2)
            EsforcosFlexaoReta(
                normal = deformacaoCG * ea,
                momento = curvatura * ei
            )
//            val secao=SecaoPilar(
//                area = 60.0 * 30.0,
//                inercia = 60.0 * 30.0.pow(3) / 12,
//                moduloDeDeformacao = 25.gpa,
//            )
//            EsforcosFlexaoReta(
//                normal = deformacaoCG * secao.area * secao.moduloDeDeformacao,
//                momento = curvatura * secao.inercia * secao.moduloDeDeformacao
//            )
        }
    }

    val barras = barras(
        diagramasDeEsforcos = diagramasDeEsforcos,
        secao = secao,
        esforcosResistentes = esforcosResistentes
    )

    val topoDoPilar = barras.flatMap { it.nos }.maxByOrNull { it.x }!!
    val cargas = mapOf(
        Carga(magnitude = 1.4 * -570.0, dof = DOF.UX) to listOf(topoDoPilar),
        Carga(magnitude = 1.4 * (0.29 * 10.0), dof = DOF.UY) to listOf(topoDoPilar),
        Carga(magnitude = 1.4 * (0.43 * 10.0 * 100.0), dof = DOF.RZ) to listOf(topoDoPilar)
    )

    val molaRestricao = Mola(
        no = No2D(x = 0.0, y = 0.0),
        kTx = 10e30, kTy = 10e30, kRz = 10e30
    )

    val resultados = analiseSolverNaoLinearBarra2D(
        elementos = barras + molaRestricao,
        restricoes = emptyMap(),
        cargas = cargas,
//        dofsAtivos = dofsAtivosBarra2D
    )

    println(resultados.deslocamento(no = topoDoPilar))

    val barraBase = barras.first()
    val deslocamentoNo1 = resultados.deslocamento(barraBase.noInicial)
    val deslocamentoNo2 = resultados.deslocamento(barraBase.noFinal)
    val reacoesBarraBase = barraBase.forcasInternas(
        deslocamentos = doubleArrayOf(
            deslocamentoNo1.ux, deslocamentoNo1.uy, deslocamentoNo1.rz,
            deslocamentoNo2.ux, deslocamentoNo2.uy, deslocamentoNo2.rz,
        )
    )
    println(reacoesBarraBase.toList())

}

fun exemploTesteJorgeLuizCeccon() {
    //Pág XII-21 da tese
    val comprimentoTotal = 178.47
    val qtdBarras = 20
    val delta = comprimentoTotal / qtdBarras.toDouble()
    val barras = (0 until qtdBarras).map { indiceBarra ->
        BarraPilar(
            secao = SecaoPilar(
                area = 19.0 * 48.0,
                inercia = 19.0 * 48.0.pow(3) / 12.0,
                moduloDeDeformacao = 25.gpa
            ),
            noInicial = No2D(x = indiceBarra * delta, y = 0.0),
            noFinal = No2D(x = (indiceBarra + 1) * delta, y = 0.0),
            esforcosResistentes = { deformacaoCG, curvatura ->
                val ea = 48.0 * 19.0 * 25.gpa
                val ei = 3_686.01 * 100.0.pow(2)
                EsforcosFlexaoReta(
                    normal = deformacaoCG * ea,
                    momento = curvatura * ei
                )
            }
        )
    }

    val molaRestricao = Mola(
        no = No2D(x = 0.0, y = 0.0),
        kTx = 10e30, kTy = 10e30, kRz = 10e30
    )

    val gamaF3 = 1.1
    val topoDoPilar = barras.flatMap { it.nos }.maxByOrNull { it.x }!!
    val cargas = mapOf(
        Carga(magnitude = -1021.0 / gamaF3, dof = DOF.UX) to listOf(topoDoPilar),
        Carga(magnitude = 5.49 / gamaF3, dof = DOF.UY) to listOf(topoDoPilar),
        Carga(magnitude = 980.0 / gamaF3, dof = DOF.RZ) to listOf(topoDoPilar)
    )

    val resultados = analiseSolverNaoLinearBarra2D(
        elementos = barras + molaRestricao,
        restricoes = emptyMap(),
        cargas = cargas,
        limiteIteracoes = 500
//        dofsAtivos = dofsAtivosBarra2D,
    )

    val barraBase = barras.first()
    val deslocamentoNo1 = resultados.deslocamento(barraBase.noInicial)
    val deslocamentoNo2 = resultados.deslocamento(barraBase.noFinal)
    val reacoesBarraBase = barraBase.forcasInternas(
        deslocamentos = doubleArrayOf(
            deslocamentoNo1.ux, deslocamentoNo1.uy, deslocamentoNo1.rz,
            deslocamentoNo2.ux, deslocamentoNo2.uy, deslocamentoNo2.rz,
        )
    )

    println("Resultados Tese:")
    println("   Deslocamento topo=0.943cm")
    println("   Momento base=26.5627kN.m")
    println("Resultados Programa:")
    println("   Deslocamento topo=${resultados.deslocamento(topoDoPilar).uy}cm")
    println("   Momento base=${reacoesBarraBase[2]/100.0}kN.m")
}

fun barras(
    diagramasDeEsforcos: Grafico<Esforco>,
    secao: (x: Double) -> SecaoPilar,
    esforcosResistentes: (x: Double) -> (deformacaoCG: Double, curvatura: Double) -> EsforcosFlexaoReta
): List<BarraPilar> {
    //TODO No momento, pega a seção e os esforços resistentes pra x do ponto1. No futuro, melhorar pra dicretizar a barra onde tem variação
    val barras = diagramasDeEsforcos.trechos
        .map { trecho ->
            BarraPilar(
                secao = secao(trecho.ponto1.x),
                esforcosResistentes = esforcosResistentes(trecho.ponto1.x),
                noInicial = No2D(x = trecho.ponto1.x, y = 0.0),
                noFinal = No2D(x = trecho.ponto2.x, y = 0.0),
            )
        }
    return barras
}

fun valor(x: Double, x1: Double, valor1: Double, x2: Double, valor2: Double): Double {
    return (x - x1) * (valor2 - valor1) / (x2 - x1) + valor1
}

val interpoladorEsforco = { x: Double, ponto1: PontoGrafico<Esforco>, ponto2: PontoGrafico<Esforco> ->
    Esforco(
        normal = valor(
            x = x, x1 = ponto1.x, valor1 = ponto1.valor.normal, x2 = ponto2.x, valor2 = ponto2.valor.normal
        ),
        cortante = valor(
            x = x, x1 = ponto1.x, valor1 = ponto1.valor.cortante, x2 = ponto2.x, valor2 = ponto2.valor.cortante
        ),
        momento = valor(
            x = x, x1 = ponto1.x, valor1 = ponto1.valor.momento, x2 = ponto2.x, valor2 = ponto2.valor.momento
        ),
    )
}

data class Esforco(val normal: Double, val momento: Double, val cortante: Double)

data class PontoGrafico<T>(val x: Double, val valor: T)

data class TrechoGrafico<T>(
    val ponto1: PontoGrafico<T>,
    val ponto2: PontoGrafico<T>,
    val interpolador: (x: Double, ponto1: PontoGrafico<T>, ponto2: PontoGrafico<T>) -> T
) {
    val x1: Double get() = ponto1.x
    val valor1: T get() = ponto1.valor
    val x2: Double get() = ponto2.x
    val valor2: T get() = ponto2.valor
    fun valor(x: Double) = interpolador(x, ponto1, ponto2)
    fun ponto(x: Double) = PontoGrafico<T>(x = x, valor = valor(x))
}

class Grafico<T>(
    pontos: List<PontoGrafico<T>>,
    val interpolador: (x: Double, ponto1: PontoGrafico<T>, ponto2: PontoGrafico<T>) -> T
) {
    val pontos = pontos.sortedBy { it.x }
    val trechos = pontos
        .zipWithNext { p1, p2 ->
            TrechoGrafico<T>(ponto1 = p1, ponto2 = p2, interpolador = interpolador)
        }
        .filter { it.x2 > it.x1 }
}