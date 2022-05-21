package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5

import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import vitorscoelho.gravitas.concretoarmado.metodogeralpilar.Mola
import kotlin.math.abs
import kotlin.math.max

class AnaliseOutraTentativa2(
    val elementos: List<ElementoFinito>,
    val restricoes: Map<No2D, List<DOF>>,
    val cargas: Map<Carga, List<No2D>>,
    val dofsAtivos: DOFsAtivos,
) {
    fun solve(): ResultadosAnalise {
        val nosDaEstrutura = elementos
            .asSequence()
            .flatMap { it.nos }
            .distinct()
            .mapIndexed { index, no2D -> Pair(no2D, index) }
            .toMap()

        val dofsIndices = dofsAtivos.mapIndexed { index, dof -> Pair(dof, index) }.toMap()
        val dofIndiceGlobal = { no: No2D, dof: DOF ->
            val indiceGlobalNo = nosDaEstrutura[no]!!
            val indiceLocalDOF = dofsIndices[dof]!!
            dofsAtivos.size * indiceGlobalNo + indiceLocalDOF
        }

        val qtdDOFsEstrutura = nosDaEstrutura.size * dofsAtivos.size
        val matrizDeRigidezGlobal = OpenMapRealMatrix(qtdDOFsEstrutura, qtdDOFsEstrutura)
        matrizDeRigidezGlobal.adicionarElementos(
            elementos = elementos, dofsAtivos = dofsAtivos, dofIndiceGlobal = dofIndiceGlobal
        )

        //TODO ver outra maneira de fazer este processo
        val rigidezMolaRestricoes = run {
            var maior = 0.0
            (0 until matrizDeRigidezGlobal.rowDimension).forEach { linha ->
                maior = max(maior, abs(matrizDeRigidezGlobal[linha, linha]))
            }
            maior * 10e6
        }
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
            elementos = molasRestricoes, dofsAtivos = dofsAtivos, dofIndiceGlobal = dofIndiceGlobal
        )

        //TODO Retirar o trecho abaixo quando implementar matriz simétrica. Pois só serve pra terminar de preencher matriz do Apache Commons Math
        matrizDeRigidezGlobal.tornarSimetrica()

        val vetorDeForcasNodais: RealMatrix = OpenMapRealMatrix(qtdDOFsEstrutura, 1)
        cargas.forEach { (carga, nos) ->
            nos.forEach { no ->
                if (dofsAtivos.contains(carga.dof)) {
                    val indiceGlobalDof = dofIndiceGlobal(no, carga.dof)
                    vetorDeForcasNodais[indiceGlobalDof, 0] = carga.magnitude
                }
            }
        }

        matrizDeRigidezGlobal.imprimir()

        val vetorDeslocamentosNodais = MatrixUtils.inverse(matrizDeRigidezGlobal).multiply(vetorDeForcasNodais)

        vetorDeslocamentosNodais.imprimir()

        val resultadosDeslocamentos = object : ResultadosDeslocamentos {
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

        val resultadosReacoesDeApoio = object : ResultadosReacoesDeApoio {
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
        return ResultadosAnalise(
            deslocamentos = resultadosDeslocamentos,
            reacoesDeApoio = resultadosReacoesDeApoio
        )
    }
}

interface ResultadosDeslocamentos {
    fun deslocamento(no: No2D): Deslocamento
}

interface ResultadosReacoesDeApoio {
    fun reacao(no: No2D): CargaNodal
}

data class ResultadosAnalise(
    private val deslocamentos: ResultadosDeslocamentos,
    private val reacoesDeApoio: ResultadosReacoesDeApoio
) : ResultadosDeslocamentos by deslocamentos, ResultadosReacoesDeApoio by reacoesDeApoio {

}

//class ResultadosDeslocamentos(private val map: Map<No2D, Deslocamento>) {
//    fun deslocamento(no: No2D) = map[no]!!
//}
//
//class ResultadosReacoesDeApoio(private val map: Map<No2D, CargaNodal>) {
//    fun reacao(no: No2D) = map[no]!!
//}

private operator fun RealMatrix.get(linha: Int, coluna: Int): Double = this.getEntry(linha, coluna)
private operator fun RealMatrix.set(linha: Int, coluna: Int, valor: Double) = this.setEntry(linha, coluna, valor)
private fun RealMatrix.adicionarElementos(
    elementos: List<ElementoFinito>,
    dofsAtivos: DOFsAtivos,
    dofIndiceGlobal: (No2D, DOF) -> Int
) {
    elementos.forEach { elemento ->
        val ke = elemento.matrizDeRigidez()
        elemento.nos.forEachIndexed { indiceLocalNoEfeito, noEfeito ->
            elemento.nos.forEachIndexed { indiceLocalNoCausa, noCausa ->
                dofsAtivos.forEach { dofEfeito ->
                    dofsAtivos.forEach { dofCausa ->
                        val indiceDofEfeito = dofIndiceGlobal(
                            noEfeito, dofEfeito
                        )
                        val indiceDofCausa = dofIndiceGlobal(
                            noCausa, dofCausa
                        )
                        if (indiceDofCausa >= indiceDofEfeito) {
                            val valor = ke.valor(
                                indiceLocalNoEfeito = indiceLocalNoEfeito,
                                dofEfeito = dofEfeito,
                                indiceLocalNoCausa = indiceLocalNoCausa,
                                dofCausa = dofCausa
                            )
                            if (valor != 0.0) {
                                this[indiceDofEfeito, indiceDofCausa] += valor
                            }
                        }
                    }
                }
            }
        }
    }
}