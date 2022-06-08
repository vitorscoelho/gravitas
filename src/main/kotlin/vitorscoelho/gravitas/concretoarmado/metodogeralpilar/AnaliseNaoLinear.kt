package vitorscoelho.gravitas.concretoarmado.metodogeralpilar

import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5.*

private fun analiseVetorForcasInternasNodais(
    elementos: List<ElementoFinitoComForcasInternas>,
    vetorDeslocamento: RealMatrix,
    qtdDOFsEstrutura: Int,
    dofIndiceGlobal: (no: No2D, dof: DOF) -> Int,
): RealMatrix {
    val vetorForcasInternas: RealMatrix = OpenMapRealMatrix(qtdDOFsEstrutura, 1)
    val deslocamentos = criarResultadosDeslocamentos(
        vetorDeslocamentosNodais = vetorDeslocamento,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal
    )
    vetorForcasInternas.adicionarForcas(
        elementos = elementos,
        resultadosDeslocamentos = deslocamentos,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal,
    )

//        val indicesDofs = intArrayOf(
//            dofIndiceGlobal(barra.noInicial, DOF.UX),
//            dofIndiceGlobal(barra.noInicial, DOF.UY),
//            dofIndiceGlobal(barra.noInicial, DOF.RZ),
//            dofIndiceGlobal(barra.noFinal, DOF.UX),
//            dofIndiceGlobal(barra.noFinal, DOF.UY),
//            dofIndiceGlobal(barra.noFinal, DOF.RZ),
//        )
//        val vetorDeslocamentosLocal = DoubleArray(size = 6) { index -> vetorDeslocamento[indicesDofs[index]] }
//        val vetorForcasInternasLocais = barra.forcasInternas(
//            esforcosResistentes = { deformacaoCG: Double, curvatura: Double ->
//                EsforcosFlexaoReta(
//                    normal = deformacaoCG * barra.secao.area * barra.secao.moduloDeDeformacao,
//                    momento = curvatura * barra.secao.inercia * barra.secao.moduloDeDeformacao
//                )
//            },
//            deslocamentos = vetorDeslocamentosLocal
//        )
//        (0..5).forEach { index -> vetorForcasInternas[indicesDofs[index]] = vetorForcasInternasLocais[index] }
//    vetorForcasInternas[]
//}
    return vetorForcasInternas
}

private val toleranciaForcas = 1e-4
private val toleranciaDeslocamentos = 1e-4
private val dofsAtivos = DOFsAtivos(
    ux = true, uy = true, uz = false,
    rx = false, ry = false, rz = true
)

fun analiseVetorDeslocamentoSolverNaoLinearBarra2D(
    elementos: List<ElementoFinitoComForcasInternas>,
    restricoes: Map<No2D, List<DOF>>,
    cargas: Map<Carga, List<No2D>>,
): ResultadosAnalise {
    val nosDaEstrutura = analiseNosDaEstruturaIndiceGlobal(elementos = elementos)
    val dofIndiceGlobal = analiseDofsIndicesGlobais(
        dofsAtivos = dofsAtivos,
        nosDaEstrutura = nosDaEstrutura
    )
    val qtdDOFsEstrutura = analiseQdtDOFsEstrutura(
        nosDaEstrutura = nosDaEstrutura,
        dofsAtivos = dofsAtivos
    )
    val matrizDeRigidezGlobal = analiseMatrizDeRigidezGlobal(
        elementos = elementos,
        nosDaEstrutura = nosDaEstrutura,
        restricoes = restricoes,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal
    )
    val vetorDeForcasNodaisExternas = analiseVetorForcasNodais(
        nosDaEstrutura = nosDaEstrutura,
        cargas = cargas,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal,
    )

    var vetorDeslocamento = analiseCalcularVetorDeslocamento(
        matrizDeRigidezGlobal = matrizDeRigidezGlobal,
        vetorDeForcasNodais = vetorDeForcasNodaisExternas
    )

    while (true) {
        val vetorForcasInternas = analiseVetorForcasInternasNodais(
            elementos = elementos,
            vetorDeslocamento = vetorDeslocamento,
            qtdDOFsEstrutura = qtdDOFsEstrutura,
            dofIndiceGlobal = dofIndiceGlobal
        )
        val vetorDiferencaForcas = vetorDeForcasNodaisExternas.subtract(vetorForcasInternas)
        val vetorDiferencaDeslocamento = analiseCalcularVetorDeslocamento(
            matrizDeRigidezGlobal = matrizDeRigidezGlobal,
            vetorDeForcasNodais = vetorDiferencaForcas
        )
        vetorDeslocamento = vetorDeslocamento.add(vetorDiferencaDeslocamento)

        val diferencaDeForcas = vetorDiferencaForcas.norm / vetorDeForcasNodaisExternas.norm
        val diferencaDeslocamento = vetorDiferencaDeslocamento.norm / vetorDeslocamento.norm
        if (diferencaDeForcas <= toleranciaForcas && diferencaDeslocamento <= toleranciaDeslocamentos) break
    }

    return criarResultadoAnalise(
        vetorDeslocamentosNodais = vetorDeslocamento,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal,
        restricoes = restricoes,
        matrizDeRigidezGlobal = matrizDeRigidezGlobal
    )
}