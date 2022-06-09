package vitorscoelho.gravitas.concretoarmado.metodogeralpilar.linhaelastica

import vitorscoelho.gravitas.utils.*
import kotlin.math.sqrt

fun main() {
//    val diagrama = Diagrama(
//        pontos = listOf(
//            PontoDiagrama.criar(x = 2.0, valorAEsquerda = -5.0, valorAdireita = 3.0),
//            PontoDiagrama.criar(x = 5.0, valorAEsquerda = -10.0, valorAdireita = 14.0),
//        ),
//        toleranciaX = 0.01
//    )
//    println(diagrama.valor(x = 2.01))

    val pontosMomento = pontosMomento1
    val pontosNormal = listOf(
        PontoDiagrama.criar(x = 0.0, valor = 1021.0),
        PontoDiagrama.criar(x = pontosMomento.last().x, valor = 1021.0)
    )

//    val cargaNormal = listOf(
//        CargaNormal(x = pontosMomento.last().x, magnitude = 1021.kN)
//    )
    val cargaNormal = cargasNormais(diagramaNormal = Diagrama(pontosNormal, toleranciaAbscissas))

    val rigidez = { x: Double -> 368.601.tfm2 }
    val iteracoes = solverNaoLinear(
        pontosMomento = pontosMomento,
        cargaNormal = cargaNormal,
        rigidezAFlexao = rigidez,
    )
    println(iteracoes.last().deslocamento)

    check(iteracoes.last().momentoFletor.valor(x = 0.0).valorAEsquerda == 3086.1260039054205)
    check(iteracoes.last().deslocamento.valor(x = pontosMomento.last().x).valorAEsquerda == 1.1031593574000202)
}

const val toleranciaAbscissas = 0.001

fun solverNaoLinear(
    pontosMomento: List<PontoDiagrama>,
    cargaNormal: List<CargaNormal>,
    rigidezAFlexao: (x: Double) -> Double,
    qtdMinimaIteracoes: Int = 60,
    qtdMaximaIteracoes: Int = 1_000,
    deltaConvergenciaDeslocamento: Double = 1e-4,
    deltaConvergenciaMomento: Double = 1e-4
): List<DiagramasBarra> {
    require(qtdMinimaIteracoes > 0)
    require(qtdMaximaIteracoes > qtdMinimaIteracoes)
    require(deltaConvergenciaDeslocamento > 0.0)
    val iteracoes = mutableListOf<DiagramasBarra>()
    iteracoes += solverLinear(pontosMomento = pontosMomento, rigidezAFlexao = rigidezAFlexao)
    var deltaDeslocamento = Double.MAX_VALUE
    var deltaMomento = Double.MAX_VALUE
    while (
        iteracoes.size <= qtdMinimaIteracoes ||
        deltaDeslocamento > deltaConvergenciaDeslocamento ||
        deltaMomento > deltaConvergenciaMomento
    ) {
        check(iteracoes.size <= qtdMaximaIteracoes) { "Limite de iterações ($qtdMaximaIteracoes) ultrapassado sem atingir a convergência" }
        val novosDeslocamentos = iteracoes.last().deslocamento
        val cargaNormalDeslocamento = cargaNormal.associateWith { normal ->
            novosDeslocamentos.valor(x = normal.x).valorAEsquerda
        }
        val pontosMomentoMajoradosPelaNormal = pontosMomento.map { pontoMomento ->
            val deslocamentoNoPontoMomento = novosDeslocamentos.valor(x = pontoMomento.x).valorAEsquerda
            var momentoAEsquerda = pontoMomento.valorAEsquerda
            var momentoADireita = pontoMomento.valorAdireita
            cargaNormal.forEach { normal ->
                val deslocamentoNormal = cargaNormalDeslocamento[normal]!!
                val momentoAdicional = normal.magnitude * (deslocamentoNormal - deslocamentoNoPontoMomento)
                momentoAEsquerda += momentoAdicional
                momentoADireita += momentoAdicional
            }
            PontoDiagrama.criar(
                x = pontoMomento.x,
                valorAEsquerda = momentoAEsquerda,
                valorAdireita = momentoADireita,
            )
        }
        iteracoes += solverLinear(
            pontosMomento = pontosMomentoMajoradosPelaNormal,
            rigidezAFlexao = rigidezAFlexao
        )

        deltaDeslocamento = delta(
            diagramaIteracaoAtual = iteracoes.last().deslocamento,
            diagramaIteracaoAnterior = iteracoes[iteracoes.lastIndex - 1].deslocamento
        )
        deltaMomento = delta(
            diagramaIteracaoAtual = iteracoes.last().momentoFletor,
            diagramaIteracaoAnterior = iteracoes[iteracoes.lastIndex - 1].momentoFletor
        )
    }
    return iteracoes
}

fun delta(diagramaIteracaoAtual: Diagrama, diagramaIteracaoAnterior: Diagrama): Double {
    return delta(
        vetorIteracaoAtual = diagramaIteracaoAtual.pontos.flatMap { listOf(it.valorAEsquerda, it.valorAdireita) },
        vetorIteracaoAnterior = diagramaIteracaoAnterior.pontos.flatMap { listOf(it.valorAEsquerda, it.valorAdireita) },
    )
}

fun delta(vetorIteracaoAtual: List<Double>, vetorIteracaoAnterior: List<Double>): Double {
    require(vetorIteracaoAtual.size == vetorIteracaoAnterior.size)
    val normaVetorIteracaoAtual = normaVetor(vetor = vetorIteracaoAtual)
    val normaVetorIteracaoAnterior = normaVetor(vetor = vetorIteracaoAnterior)
    return (normaVetorIteracaoAtual - normaVetorIteracaoAnterior) / normaVetorIteracaoAnterior
}

fun normaVetor(vetor: List<Double>): Double = sqrt(vetor.sumOf { it * it })

fun cargasNormais(diagramaNormal: Diagrama): List<CargaNormal> {
    val pontos = diagramaNormal.pontos.toList()
    val cargas = mutableListOf<CargaNormal>()
    pontos.indices.forEach { index ->
        val pontoAtual = pontos[index]
        if (index == pontos.lastIndex) {
            cargas += CargaNormal(
                x = pontoAtual.x,
                magnitude = pontoAtual.valorAEsquerda
            )
        } else {
            val pontoProximo = pontos[index + 1]
            cargas += CargaNormal(
                x = pontoAtual.x,
                magnitude = pontoAtual.valorAEsquerda - pontoAtual.valorAdireita
            )
            cargas += CargaNormal(
                x = pontoAtual.x,
                magnitude = pontoAtual.valorAdireita - pontoProximo.valorAEsquerda
            )
        }
    }
    return cargas
        .groupBy { it.x }
        .map { (x, listaCargas) ->
            CargaNormal(x = x, magnitude = listaCargas.sumOf { it.magnitude })
        }
        .filter { it.magnitude != 0.0 }
}

fun solverLinear(pontosMomento: List<PontoDiagrama>, rigidezAFlexao: (x: Double) -> Double): DiagramasBarra {
    val pontosCurvatura = pontosMomento
        .map { ponto ->
            PontoDiagrama.criar(
                x = ponto.x,
                valorAEsquerda = ponto.valorAEsquerda / rigidezAFlexao(ponto.x),
                valorAdireita = ponto.valorAdireita / rigidezAFlexao(ponto.x),
            )
        }
    val pontosRotacao = integrarPontos(pontosParaIntegrar = pontosCurvatura)
    val pontosDeslocamento = integrarPontos(pontosParaIntegrar = pontosRotacao)
    return DiagramasBarra(
        momentoFletor = Diagrama(pontos = pontosMomento, toleranciaX = toleranciaAbscissas),
        curvatura = Diagrama(pontos = pontosCurvatura, toleranciaX = toleranciaAbscissas),
        rotacao = Diagrama(pontos = pontosRotacao, toleranciaX = toleranciaAbscissas),
        deslocamento = Diagrama(pontos = pontosDeslocamento, toleranciaX = toleranciaAbscissas),
    )
}

fun integrarPontos(pontosParaIntegrar: List<PontoDiagrama>): List<PontoDiagrama> {
    val pontosIntegrados = mutableListOf<PontoDiagrama>()
    pontosParaIntegrar.indices.forEach { index ->
        if (index == 0) {
            pontosIntegrados += PontoDiagrama.criar(
                x = pontosParaIntegrar[index].x,
                valor = 0.0
            )
        } else {
            val pontoAnterior = pontosParaIntegrar[index - 1]
            val pontoAtual = pontosParaIntegrar[index]
            val valor1 = pontoAnterior.valorAdireita
            val valor2 = pontoAtual.valorAEsquerda
            val comprimento = pontoAtual.x - pontoAnterior.x
            val pontoIntegradoAnterior = pontosIntegrados[index - 1].valorAEsquerda //Tanto faz o lado
            pontosIntegrados += PontoDiagrama.criar(
                x = pontoAtual.x,
                valor = 0.5 * (valor1 + valor2) * comprimento + pontoIntegradoAnterior
            )
        }
    }
    return pontosIntegrados
}

val pontosMomento1 = run {
    val vao = 1.7847.metros
    val divisoes = 100
    val momentoTopo = 9.8.kNm
    val horizontalTopo = 5.49.kN

    val delta = vao / divisoes
    val abscissas = mutableListOf<Double>()
    (0 until divisoes).forEach { index -> abscissas += index * delta }
    abscissas += vao

    abscissas.map { x ->
        PontoDiagrama.criar(x = x, valor = momentoTopo + (vao - x) * horizontalTopo)
    }
}

data class CargaNormal(val x: Double, val magnitude: Double)

data class DiagramasBarra(
    val momentoFletor: Diagrama,
    val curvatura: Diagrama,
    val rotacao: Diagrama,
    val deslocamento: Diagrama
)