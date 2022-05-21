package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5

import org.apache.commons.math3.linear.*

data class Deslocamento(
    val ux: Double, val uy: Double, val uz: Double,
    val rx: Double, val ry: Double, val rz: Double,
)

data class CargaNodal(
    val fx: Double, val fy: Double, val fz: Double,
    val mx: Double, val my: Double, val mz: Double,
) {
    companion object {
        val NULA = CargaNodal(
            fx = 0.0, fy = 0.0, fz = 0.0,
            mx = 0.0, my = 0.0, mz = 0.0
        )
    }
}

class Carga(val magnitude: Double, val dof: DOF)

class Analise(
    val elementos: List<ElementoFinito>,
    val restricoes: Map<No2D, List<DOF>>,
    val cargas: Map<Carga, List<No2D>>,
    val dofsAtivos: DOFsAtivos,
) {
    fun solve(): Map<No2D, Deslocamento> {
        val nosDaEstrutura = elementos
            .asSequence()
            .flatMap { it.nos }
            .distinct()
            .mapIndexed { index, no2D -> Pair(no2D, index) }
            .toMap()
        val qtdDOFsEstrutura = nosDaEstrutura.size * dofsAtivos.size
        val vetorDeEspalhamentoDOFS = Array(size = nosDaEstrutura.size) {
            IntArray(size = dofsAtivos.size)
        }.apply {
            var indiceDOFLivre = 0
            var indiceDOFRestrito = qtdDOFsEstrutura
            nosDaEstrutura.forEach { (no, indiceGlobalNo) ->
                val vetorDeEspalhamento = this[indiceGlobalNo]
                val restricoesNo = restricoes[no] ?: emptyList()
                dofsAtivos.forEachIndexed { indiceLocalDOF, dof ->
                    val dofRestrito = restricoesNo.contains(dof)
//              TODO      vetorDeEspalhamento[indiceLocalDOF] = if (dofRestrito) indiceDOFRestrito++ else indiceDOFLivre++
                    vetorDeEspalhamento[indiceLocalDOF] = indiceDOFLivre++
                }
            }
        }

        val matrizDeRigidezGlobal = Array2DRowRealMatrix(qtdDOFsEstrutura, qtdDOFsEstrutura)
        elementos.forEach { elemento ->
            val ke = elemento.matrizDeRigidez()

            val (indicesGlobaisDofs, indicesLocaisDofs) = run {
                val indicesGlobaisDofsDesordenados = elemento.nos
                    .flatMap { no ->
                        val indiceGlobalNo = nosDaEstrutura[no]!!
                        vetorDeEspalhamentoDOFS[indiceGlobalNo].toList()
                    }
                    .toIntArray()
                val indicesLocaisDofsDesordenados = elemento.nos
                    .flatMapIndexed { indiceLocalNo, _ ->
                        dofsAtivos.map { it.indiceLocalElemento(indiceLocalNo = indiceLocalNo) }
                    }
                    .toIntArray()
                val indicesParaOrdenarArrays = indicesGlobaisDofsDesordenados
                    .mapIndexed { index, i -> Pair(index, i) }
                    .sortedBy { it.second }
                    .map { it.first }
                val globais = IntArray(size = indicesGlobaisDofsDesordenados.size)
                val locais = IntArray(size = indicesGlobaisDofsDesordenados.size)
                (0..globais.lastIndex).forEach { index ->
                    val indiceOrdem = indicesParaOrdenarArrays[index]
                    globais[index] = indicesGlobaisDofsDesordenados[indiceOrdem]
                    locais[index] = indicesLocaisDofsDesordenados[indiceOrdem]
                }
                Pair(globais, locais)
            }

            (indicesGlobaisDofs.indices).forEach { linhaEfeito ->
                (linhaEfeito..indicesGlobaisDofs.lastIndex).forEach { linhaCausa ->
                    val valor = ke.valor(
                        indiceLocalDOFEfeito = indicesLocaisDofs[linhaEfeito],
                        indiceLocalDOFCausa = indicesLocaisDofs[linhaCausa]
                    )
                    if (valor != 0.0) {
                        val indiceGlobalDOFEFeito = indicesGlobaisDofs[linhaEfeito]
                        val indiceGlobalDOFCausa = indicesGlobaisDofs[linhaCausa]
                        matrizDeRigidezGlobal.addToEntry(
                            indiceGlobalDOFEFeito,
                            indiceGlobalDOFCausa,
                            valor
                        )
                    }
                }
            }
        }

        //TODO Retirar o trecho abaixo quando implementar matriz simétrica. Pois só serve pra terminar de preencher matriz do Apache Commons Math
        (0 until qtdDOFsEstrutura).forEach { coluna ->
            (coluna until qtdDOFsEstrutura).forEach { linha ->
                matrizDeRigidezGlobal.setEntry(
                    linha, coluna,
                    matrizDeRigidezGlobal.getEntry(coluna, linha)
                )
            }
        }

        //TODO ver outra maneira de fazer este processo
        restricoes.forEach { (no, restricoesNo) ->
            val indiceGlobalNo = nosDaEstrutura[no]!!
            restricoesNo.forEach { dofRestricao ->
                val indiceGlobalDOF = vetorDeEspalhamentoDOFS[indiceGlobalNo][dofsAtivos.indexOf(dofRestricao)]
                matrizDeRigidezGlobal.setEntry(indiceGlobalDOF, indiceGlobalDOF, 10e12)
            }
        }

        val vetorDeForcasNodais: RealMatrix = OpenMapRealMatrix(qtdDOFsEstrutura, 1).apply {
            cargas.forEach { (carga, nos) ->
                nos.forEach { no ->
                    val indiceGlobalNo = nosDaEstrutura[no]!!
                    val indiceGlobalDOF = vetorDeEspalhamentoDOFS[indiceGlobalNo][dofsAtivos.indexOf(carga.dof)]
                    addToEntry(
                        indiceGlobalDOF, 0,
                        carga.magnitude
                    )
                }
            }
        }

        val vetorDeslocamentosNodais = MatrixUtils.inverse(matrizDeRigidezGlobal).multiply(vetorDeForcasNodais)

        matrizDeRigidezGlobal.imprimir()
        vetorDeslocamentosNodais.imprimir()

        return hashMapOf<No2D, Deslocamento>().apply {
            nosDaEstrutura.forEach { (no, indiceNo) ->
                val mapDeslocamentos = hashMapOf<DOF, Double>()
                dofsAtivos.forEachIndexed { index, dof ->
                    val indiceGlobalDOF = vetorDeEspalhamentoDOFS[indiceNo][index]
                    mapDeslocamentos[dof] = vetorDeslocamentosNodais.getEntry(indiceGlobalDOF, 0)
                }
                this[no] = Deslocamento(
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
}