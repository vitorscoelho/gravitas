package vitorscoelho.gravitas.concretoarmado.flexaoobliqua

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.nbr6118.AcoCA
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.nbr6118.Concreto
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.secaotransversal.SecaoDiscreta
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.secaotransversal.SecaoPoligonal
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.nbr6118.deformada
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.nbr6118.momentoPositivoResistenteELU

fun main() {
    /*
    Testar isso. Não pode existir
    vertices = listOf(
            Vetor2D(x = 0.0, y = 0.0),
            Vetor2D(x = 0.0, y = 60.0),
            Vetor2D(x = 25.0, y = 0.0),
            Vetor2D(x = 25.0, y = 90.0),
        )
     */
    val poligono = SecaoPoligonal(
        vertices = listOf(
            Vetor2D(x = 0.0, y = 0.0),
            Vetor2D(x = 25.0, y = 0.0),
            Vetor2D(x = 25.0, y = 60.0),
            Vetor2D(x = 0.0, y = 60.0),
        )
    )
    val armaduraInferior = SecaoDiscreta(posicao = Vetor2D(x = 12.5, y = 5.0), area = 10.0)
    val armaduraSuperior = SecaoDiscreta(posicao = Vetor2D(x = 12.5, y = 55.0), area = 5.0)

    val concreto = Concreto(fck = 2.5, gamaC = 1.4)
    val funcaoTensaoDeformacaoEstadio3 = concreto.funcaoTensaoDeformacaoEstadio3
    val aco = AcoCA(fyk = 50.0, gamaS = 1.15, moduloDeDeformacao = 21_000.0)
    val funcaoTensaoDeformacaoAco = aco.funcaoTensaoDeformacaoELU
    val funcaoTensaoDeformacaoEfetivaAco = object : FuncaoTensaoDeformacao {
        override val deformacoesInflexao: List<Double> = funcaoTensaoDeformacaoAco.deformacoesInflexao

        override fun tensao(deformacao: Double): Double {
            val tensaoAco = funcaoTensaoDeformacaoAco.tensao(deformacao = deformacao)
            val tensaoConcreto = funcaoTensaoDeformacaoEstadio3.tensao(deformacao = deformacao)
            return tensaoAco - tensaoConcreto
        }
    }

    val deformada = DeformadaFlexaoReta.criar(
        y1 = 60.0, deformacaoEmY1 = 0.0035,
        y2 = 5.0, deformacaoEmY2 = -0.00377362808493272
    )
//    val esforcosResistentesConcreto = poligono.esforcosResistentes(
//        funcaoTensaoDeformacao = funcaoTensaoDeformacaoEstadio3,
//        deformada = deformada
//    )
//    val esforcosResistentesArmaduraInferior = armaduraInferior.esforcosResistentes(
//        funcaoTensaoDeformacao = funcaoTensaoDeformacaoEfetivaAco,
//        deformada = deformada
//    )
//    val esforcosResistentesArmaduraSuperior = armaduraSuperior.esforcosResistentes(
//        funcaoTensaoDeformacao = funcaoTensaoDeformacaoEfetivaAco,
//        deformada = deformada
//    )
//    val esforcosResistentes =
//        esforcosResistentesConcreto + esforcosResistentesArmaduraInferior + esforcosResistentesArmaduraSuperior
    val esforcosResistentes = poligono.esforcosResistentes(
        funcaoTensaoDeformacao = funcaoTensaoDeformacaoEstadio3,
        armaduras = listOf(armaduraInferior, armaduraSuperior),
        funcaoTensaoDeformacaoArmaduras = { _ -> funcaoTensaoDeformacaoAco },
        deformada = deformada
    )
//    fun curvaturaELU(ecg: Double, d: Double): Double {
//        val x5 = (concreto.ecu - concreto.ec2) * secao.diametro / concreto.ecu
//        val curvatura1 = (concreto.ec2 - ecg) / (0.5 * secao.diametro - x5)
//        val curvatura2 = (concreto.ecu - ecg) / (0.5 * secao.diametro)
//        val curvatura3 = (ecg + ALONGAMENTO_LIMITE_ACO) / (d - 0.5 * secao.diametro)
//        return doubleArrayOf(curvatura1, curvatura2, curvatura3).min()!!
//    }
    println(esforcosResistentes)
    println(60.0 - deformada.y(deformacao = 0.0))
    println((esforcosResistentes.normal * (-60.0 / 2.0) + esforcosResistentes.momento) / 100.0)

    val deformadaEstimada = deformada(
        secaoBruta = poligono,
        funcaoTensaoDeformacaoSecaoBruta = funcaoTensaoDeformacaoEstadio3,
        armaduras = listOf(armaduraInferior, armaduraSuperior),
        funcaoTensaoDeformacaoArmaduras = { _ -> funcaoTensaoDeformacaoAco },
        yNormal = 60.0 / 2.0,
        normal = 588.0,
        momento = 49194.13597084635 - 588.0 * 60.0 / 2.0
    )
    println(deformada)
    println(deformadaEstimada)

    println(
        momentoPositivoResistenteELU(
            secaoBruta = poligono,
            yMinSecaoBruta = 0.0, yMaxSecaoBruta = 60.0,
            concreto = concreto,
            armaduras = listOf(armaduraInferior, armaduraSuperior), aco = aco,
            yNormal = 60.0 / 2.0,
            normal = 588.0
        ) / 100.0
    )
}