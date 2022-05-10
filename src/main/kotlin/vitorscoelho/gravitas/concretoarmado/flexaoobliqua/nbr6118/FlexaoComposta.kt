package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.nbr6118

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.DeformadaFlexaoReta
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.FuncaoTensaoDeformacao
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math.newtonRaphson
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math.raizMetodoNewtonRaphson
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.secaotransversal.SecaoDiscreta
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.secaotransversal.SecaoTransversal
import kotlin.math.min

/*
funcaoTensaoDeformacao: FuncaoTensaoDeformacao,
        armaduras: List<SecaoDiscreta>,
        funcaoTensaoDeformacaoArmaduras: (armadura: SecaoDiscreta) -> FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
 */
fun deformada(
    secaoBruta: SecaoTransversal,
    funcaoTensaoDeformacaoSecaoBruta: FuncaoTensaoDeformacao,
    armaduras: List<SecaoDiscreta>,
    funcaoTensaoDeformacaoArmaduras: (armadura: SecaoDiscreta) -> FuncaoTensaoDeformacao,
    yNormal: Double,
    normal: Double,
    momento: Double
): DeformadaFlexaoReta {
    val funcaoEquilibrio = { deformacaoCurvatura: DoubleArray ->
        val deformada = DeformadaFlexaoReta.criar(
            y = 0.0, deformacaoEmY = deformacaoCurvatura[0], curvatura = deformacaoCurvatura[1]
        )
        val esforcosResistentes = secaoBruta.esforcosResistentes(
            funcaoTensaoDeformacao = funcaoTensaoDeformacaoSecaoBruta,
            armaduras = armaduras,
            funcaoTensaoDeformacaoArmaduras = funcaoTensaoDeformacaoArmaduras,
            deformada = deformada
        )
        val somatorioNormal = esforcosResistentes.normal - normal
        val somatorioMomento = esforcosResistentes.momento - momento - normal * yNormal
        doubleArrayOf(somatorioNormal, somatorioMomento)
    }
    val vetorDeformada = newtonRaphson(
        funcao = funcaoEquilibrio,
        aproximacaoInicial = doubleArrayOf(0.0, 0.0),
        nMaximoIteracoes = 400,
        tolerancia = doubleArrayOf(0.01, 0.01)
    )
    return DeformadaFlexaoReta.criar(
        y = 0.0, deformacaoEmY = vetorDeformada[0], curvatura = vetorDeformada[1]
    )
}

private fun curvaturaELU(
    y: Double,
    deformacaoEmY: Double,
    alturaSecao: Double,
    yMinArmadura: Double,
    ec2: Double,
    ecu: Double,
    esTLim: Double
): Double {
    /*
    Região I — correspondente ao polo de ruína B (encurtamento limite do concreto).
    Região II — correspondente ao polo de ruína A (encurtamento limite do concreto).
    Região III — correspondente ao polo de ruína C (alongamento excessivo da armadura).
     */
    val y5: Double by lazy { alturaSecao * (1.0 - (ecu - ec2) / ecu) }
    val curvaturaRegiao1 = DeformadaFlexaoReta.curvatura(
        y1 = y, deformacaoEmY1 = deformacaoEmY, y2 = y5, deformacaoEmY2 = ec2
    )
    val curvaturaRegiao2 = DeformadaFlexaoReta.curvatura(
        y1 = y, deformacaoEmY1 = deformacaoEmY, y2 = alturaSecao, deformacaoEmY2 = ecu
    )
    val curvaturaRegiao3 = DeformadaFlexaoReta.curvatura(
        y1 = y, deformacaoEmY1 = deformacaoEmY, y2 = yMinArmadura, deformacaoEmY2 = esTLim
    )
    return min(curvaturaRegiao1, min(curvaturaRegiao2, curvaturaRegiao3))
}

fun momentoPositivoResistenteELU(
    secaoBruta: SecaoTransversal,
    yMinSecaoBruta: Double,
    yMaxSecaoBruta: Double,
    concreto: Concreto,
    armaduras: List<SecaoDiscreta>,
    aco: AcoCA,
    yNormal: Double,
    normal: Double
): Double {
    val alturaSecaoBruta = yMaxSecaoBruta - yMinSecaoBruta
    val yMinArmadura = armaduras.minOf { it.posicao.y }

    /**Ordenada que define a transição da ruptura da região 1 para a 2*/
    val y5 = alturaSecaoBruta * (1.0 - (concreto.ecu - concreto.ec2) / concreto.ecu)

    /**
     * Ordenada usada para variar durante o processo iterativo.
     * Fica abaixo de y5 para que as curvaturas no ELU sejam sempre positiva
     */
    val yControle = y5 / 2.0
    var momentoResistente = Double.NaN
    val funcaoEquilibrio = { deformacaoYControle: Double ->
        val curvaturaELU = curvaturaELU(
            y = yControle, deformacaoEmY = deformacaoYControle,
            alturaSecao = alturaSecaoBruta, yMinArmadura = yMinArmadura,
            ec2 = concreto.ec2, ecu = concreto.ecu,
            esTLim = aco.esTLim
        )
        val deformada = DeformadaFlexaoReta.criar(
            y = yControle, deformacaoEmY = deformacaoYControle, curvatura = curvaturaELU
        )
        val esforcos = secaoBruta.esforcosResistentes(
            funcaoTensaoDeformacao = concreto.funcaoTensaoDeformacaoEstadio3,
            armaduras = armaduras,
            funcaoTensaoDeformacaoArmaduras = { aco.funcaoTensaoDeformacaoELU },
            deformada = deformada
        )
        momentoResistente = esforcos.momento - normal * yNormal
        esforcos.normal - normal
    }
    raizMetodoNewtonRaphson(
        xPrimeiraAproximacao = 0.0,
        acuraciaAbsoluta = 0.001,
        limiteDeIteracoes = 300,
        funcao = funcaoEquilibrio
    )
    return momentoResistente
}