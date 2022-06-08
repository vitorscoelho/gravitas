package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5

import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import vitorscoelho.gravitas.concretoarmado.metodogeralpilar.Mola
import kotlin.math.abs
import kotlin.math.max

fun analiseNosDaEstruturaIndiceGlobal(elementos: List<ElementoFinito>): Map<No2D, Int> {
    return elementos
        .asSequence()
        .flatMap { it.nos }
        .distinct()
        .mapIndexed { index, no2D -> Pair(no2D, index) }
        .toMap()
}

fun analiseDofsIndicesGlobais(dofsAtivos: DOFsAtivos, nosDaEstrutura: Map<No2D, Int>): (no: No2D, dof: DOF) -> Int {
    val dofsIndices = dofsAtivos.mapIndexed { index, dof -> Pair(dof, index) }.toMap()
    return { no: No2D, dof: DOF ->
        val indiceGlobalNo = nosDaEstrutura[no]!!
        val indiceLocalDOF = dofsIndices[dof]!!
        dofsAtivos.size * indiceGlobalNo + indiceLocalDOF
    }
}

fun analiseQdtDOFsEstrutura(nosDaEstrutura: Map<No2D, Int>, dofsAtivos: DOFsAtivos): Int {
    return nosDaEstrutura.size * dofsAtivos.size
}

fun RealMatrix.maiorValorDaDiagonal(): Double {
    var maior = 0.0
    (0 until this.rowDimension).forEach { linha ->
        maior = max(maior, abs(this[linha, linha]))
    }
    return maior
}

fun rigidezMolaRestricao(matrizDeRigidezGlobal: RealMatrix): Double {
    //TODO ver outra maneira de fazer este processo
    return matrizDeRigidezGlobal.maiorValorDaDiagonal() * 10e6
}

fun analiseMatrizDeRigidezGlobal(
    elementos: List<ElementoFinito>,
    nosDaEstrutura: Map<No2D, Int>,
    restricoes: Map<No2D, List<DOF>>,
    dofsAtivos: DOFsAtivos,
    dofIndiceGlobal: (no: No2D, dof: DOF) -> Int,
): RealMatrix {
    val qtdDOFsEstrutura = analiseQdtDOFsEstrutura(nosDaEstrutura, dofsAtivos)
    val matrizDeRigidezGlobal = OpenMapRealMatrix(qtdDOFsEstrutura, qtdDOFsEstrutura)
    matrizDeRigidezGlobal.adicionarElementos(
        elementos = elementos, dofsAtivos = dofsAtivos, dofIndiceGlobal = dofIndiceGlobal
    )
    val rigidezMolaRestricoes = rigidezMolaRestricao(matrizDeRigidezGlobal = matrizDeRigidezGlobal)
    //Molas criadas apenas para capturar as reações de apoio
    val molasRestricoes = restricoes.map { (no, dofs) ->
        Mola(
            no = no,
            kTx = if (dofs.contains(DOF.UX)) rigidezMolaRestricoes else 0.0,
            kTy = if (dofs.contains(DOF.UY)) rigidezMolaRestricoes else 0.0,
            kTz = if (dofs.contains(DOF.UZ)) rigidezMolaRestricoes else 0.0,
            kRx = if (dofs.contains(DOF.RX)) rigidezMolaRestricoes else 0.0,
            kRy = if (dofs.contains(DOF.RY)) rigidezMolaRestricoes else 0.0,
            kRz = if (dofs.contains(DOF.RZ)) rigidezMolaRestricoes else 0.0,
        )
    }
    matrizDeRigidezGlobal.adicionarElementos(
        elementos = molasRestricoes, dofsAtivos = dofsAtivos, dofIndiceGlobal = dofIndiceGlobal,
        zerarAntesDeAdicionar = true
    )

    //TODO Retirar o trecho abaixo quando implementar matriz simétrica. Pois só serve pra terminar de preencher matriz do Apache Commons Math
    matrizDeRigidezGlobal.tornarSimetrica()

    return matrizDeRigidezGlobal
}

fun analiseVetorForcasNodais(
    nosDaEstrutura: Map<No2D, Int>,
    cargas: Map<Carga, List<No2D>>,
    dofsAtivos: DOFsAtivos,
    dofIndiceGlobal: (no: No2D, dof: DOF) -> Int,
): RealMatrix {
    val qtdDOFsEstrutura = analiseQdtDOFsEstrutura(nosDaEstrutura, dofsAtivos)
    val vetorDeForcasNodais: RealMatrix = OpenMapRealMatrix(qtdDOFsEstrutura, 1)
    cargas.forEach { (carga, nos) ->
        nos.forEach { no ->
            if (dofsAtivos.contains(carga.dof)) {
                val indiceGlobalDof = dofIndiceGlobal(no, carga.dof)
                vetorDeForcasNodais[indiceGlobalDof, 0] = carga.magnitude
            }
        }
    }
    return vetorDeForcasNodais
}

fun analiseCalcularVetorDeslocamento(
    matrizDeRigidezGlobal: RealMatrix,
    vetorDeForcasNodais: RealMatrix
): RealMatrix {
    return MatrixUtils.inverse(matrizDeRigidezGlobal).multiply(vetorDeForcasNodais)
}

fun criarResultadosDeslocamentos(
    vetorDeslocamentosNodais: RealMatrix,
    dofsAtivos: DOFsAtivos,
    dofIndiceGlobal: (no: No2D, dof: DOF) -> Int,
): ResultadosDeslocamentos {
    return object : ResultadosDeslocamentos {
        override fun deslocamento(no: No2D): Deslocamento {
            val mapDeslocamentos = hashMapOf<DOF, Double>()
            dofsAtivos.forEach { dof ->
                val indiceGlobalDOF = dofIndiceGlobal(no, dof)
                mapDeslocamentos[dof] = vetorDeslocamentosNodais.getEntry(indiceGlobalDOF, 0)
            }
            return Deslocamento(
                ux = mapDeslocamentos[DOF.UX] ?: 0.0,
                uy = mapDeslocamentos[DOF.UY] ?: 0.0,
                uz = mapDeslocamentos[DOF.UZ] ?: 0.0,
                rx = mapDeslocamentos[DOF.RX] ?: 0.0,
                ry = mapDeslocamentos[DOF.RY] ?: 0.0,
                rz = mapDeslocamentos[DOF.RZ] ?: 0.0,
            )
        }
    }
}

fun criarResultadosReacoesDeApoio(
    restricoes: Map<No2D, List<DOF>>,
    resultadosDeslocamentos: ResultadosDeslocamentos,
    matrizDeRigidezGlobal: RealMatrix,
): ResultadosReacoesDeApoio {
    //TODO ver outra maneira de fazer este processo
    val rigidezMolaRestricoes = matrizDeRigidezGlobal.maiorValorDaDiagonal()
    return object : ResultadosReacoesDeApoio {
        override fun reacao(no: No2D): CargaNodal {
            if (!restricoes.contains(no)) return CargaNodal.NULA
            val deslocamento = resultadosDeslocamentos.deslocamento(no = no)
            return CargaNodal(
                fx = rigidezMolaRestricoes * deslocamento.ux,
                fy = rigidezMolaRestricoes * deslocamento.uy,
                fz = rigidezMolaRestricoes * deslocamento.uz,
                mx = rigidezMolaRestricoes * deslocamento.rx,
                my = rigidezMolaRestricoes * deslocamento.ry,
                mz = rigidezMolaRestricoes * deslocamento.rz,
            )
        }
    }
}

fun criarResultadoAnalise(
    vetorDeslocamentosNodais: RealMatrix,
    dofsAtivos: DOFsAtivos,
    dofIndiceGlobal: (no: No2D, dof: DOF) -> Int,
    restricoes: Map<No2D, List<DOF>>,
    matrizDeRigidezGlobal: RealMatrix,
): ResultadosAnalise {
    val resultadosDeslocamentos = criarResultadosDeslocamentos(
        vetorDeslocamentosNodais = vetorDeslocamentosNodais,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal
    )
    val resultadosReacoesDeApoio = criarResultadosReacoesDeApoio(
        restricoes = restricoes,
        resultadosDeslocamentos = resultadosDeslocamentos,
        matrizDeRigidezGlobal = matrizDeRigidezGlobal,
    )
    return ResultadosAnalise(
        deslocamentos = resultadosDeslocamentos,
        reacoesDeApoio = resultadosReacoesDeApoio
    )
}

fun analiseSolverLinear(
    elementos: List<ElementoFinito>,
    restricoes: Map<No2D, List<DOF>>,
    cargas: Map<Carga, List<No2D>>,
    dofsAtivos: DOFsAtivos,
): ResultadosAnalise {
    val nosDaEstrutura = analiseNosDaEstruturaIndiceGlobal(elementos = elementos)
    val dofIndiceGlobal = analiseDofsIndicesGlobais(
        dofsAtivos = dofsAtivos,
        nosDaEstrutura = nosDaEstrutura
    )
    val matrizDeRigidezGlobal = analiseMatrizDeRigidezGlobal(
        elementos = elementos,
        nosDaEstrutura = nosDaEstrutura,
        restricoes = restricoes,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal
    )
    val vetorDeForcasNodais = analiseVetorForcasNodais(
        nosDaEstrutura = nosDaEstrutura,
        cargas = cargas,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal,
    )
    val vetorDeslocamentos = analiseCalcularVetorDeslocamento(
        matrizDeRigidezGlobal = matrizDeRigidezGlobal,
        vetorDeForcasNodais = vetorDeForcasNodais
    )
    return criarResultadoAnalise(
        vetorDeslocamentosNodais = vetorDeslocamentos,
        dofsAtivos = dofsAtivos,
        dofIndiceGlobal = dofIndiceGlobal,
        restricoes = restricoes,
        matrizDeRigidezGlobal = matrizDeRigidezGlobal
    )
}