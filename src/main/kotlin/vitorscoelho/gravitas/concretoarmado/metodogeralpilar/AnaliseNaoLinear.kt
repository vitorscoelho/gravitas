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
        dofsAtivos = dofsAtivosBarra2D,
        dofIndiceGlobal = dofIndiceGlobal
    )
    vetorForcasInternas.adicionarForcas(
        elementos = elementos,
        resultadosDeslocamentos = deslocamentos,
        dofsAtivos = dofsAtivosBarra2D,
        dofIndiceGlobal = dofIndiceGlobal,
    )
    return vetorForcasInternas
}

private val toleranciaForcas = 1e-4
private val toleranciaDeslocamentos = 1e-4
val dofsAtivosBarra2D = DOFsAtivos(
    ux = true, uy = true, uz = false,
    rx = false, ry = false, rz = true
)

fun analiseSolverNaoLinearBarra2D(
    elementos: List<ElementoFinitoComForcasInternas>,
    restricoes: Map<No2D, List<DOF>>,
    cargas: Map<Carga, List<No2D>>,
    limiteIteracoes: Int = 100
): ResultadosAnalise {
    require(limiteIteracoes > 1)
    val nosDaEstrutura = analiseNosDaEstruturaIndiceGlobal(elementos = elementos)
    val dofIndiceGlobal = analiseDofsIndicesGlobais(
        dofsAtivos = dofsAtivosBarra2D,
        nosDaEstrutura = nosDaEstrutura
    )
    val qtdDOFsEstrutura = analiseQdtDOFsEstrutura(
        nosDaEstrutura = nosDaEstrutura,
        dofsAtivos = dofsAtivosBarra2D
    )
    val matrizDeRigidezGlobal = analiseMatrizDeRigidezGlobal(
        elementos = elementos,
        nosDaEstrutura = nosDaEstrutura,
        restricoes = restricoes,
        dofsAtivos = dofsAtivosBarra2D,
        dofIndiceGlobal = dofIndiceGlobal
    )
    val vetorDeForcasNodaisExternas = analiseVetorForcasNodais(
        nosDaEstrutura = nosDaEstrutura,
        cargas = cargas,
        dofsAtivos = dofsAtivosBarra2D,
        dofIndiceGlobal = dofIndiceGlobal,
    )

    var vetorDeslocamento = analiseCalcularVetorDeslocamento(
        matrizDeRigidezGlobal = matrizDeRigidezGlobal,
        vetorDeForcasNodais = vetorDeForcasNodaisExternas
    )

    var iteracao = 0
    while (true) {
        check(iteracao++ <= limiteIteracoes) { "Limite de iterações ($limiteIteracoes) ultrapassado sem atingir a convergência" }
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
        dofsAtivos = dofsAtivosBarra2D,
        dofIndiceGlobal = dofIndiceGlobal,
        restricoes = restricoes,
        matrizDeRigidezGlobal = matrizDeRigidezGlobal
    )
}