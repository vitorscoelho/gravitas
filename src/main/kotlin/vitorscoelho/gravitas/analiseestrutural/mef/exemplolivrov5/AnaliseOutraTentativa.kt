package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix

class AnaliseOutraTentativa(
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
        val qtdDOFsRestritos = restricoes.values.sumOf { it.size }
        val qtdDOFsLivres = qtdDOFsEstrutura - qtdDOFsRestritos

        val vetorDeEspalhamentoDOFS = Array(size = nosDaEstrutura.size) {
            IntArray(size = dofsAtivos.size)
        }.apply {
            var indiceDOFLivre = 0
            var indiceDOFRestrito = qtdDOFsLivres
            nosDaEstrutura.forEach { (no, indiceGlobalNo) ->
                val vetorDeEspalhamento = this[indiceGlobalNo]
                val restricoesNo = restricoes[no] ?: emptyList()
                dofsAtivos.forEachIndexed { indiceLocalDOF, dof ->
                    val dofRestrito = restricoesNo.contains(dof)
                    vetorDeEspalhamento[indiceLocalDOF] = if (dofRestrito) indiceDOFRestrito++ else indiceDOFLivre++
                }
            }
        }

        val matrizDeRigidezGlobalDOFsLivres = Array2DRowRealMatrix(qtdDOFsLivres, qtdDOFsLivres)
        val matrizDeRigidezGlobalDOFsRestritos = Array2DRowRealMatrix(qtdDOFsRestritos, qtdDOFsLivres)

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
                (indicesGlobaisDofs.indices).forEach { linhaCausa ->
                    val valor = ke.valor(
                        indiceLocalDOFEfeito = indicesLocaisDofs[linhaEfeito],
                        indiceLocalDOFCausa = indicesLocaisDofs[linhaCausa]
                    )
                    if (valor != 0.0) {
                        val indiceGlobalDOFEFeito = indicesGlobaisDofs[linhaEfeito]
                        val indiceGlobalDOFCausa = indicesGlobaisDofs[linhaCausa]
                        if (indiceGlobalDOFCausa < qtdDOFsLivres) {
                            if (indiceGlobalDOFEFeito <= indiceGlobalDOFCausa) {
                                matrizDeRigidezGlobalDOFsLivres.addToEntry(
                                    indiceGlobalDOFEFeito,
                                    indiceGlobalDOFCausa,
                                    valor
                                )
                            } else if (indiceGlobalDOFEFeito >= qtdDOFsLivres) {
                                matrizDeRigidezGlobalDOFsRestritos.addToEntry(
                                    indiceGlobalDOFEFeito - qtdDOFsLivres,
                                    indiceGlobalDOFCausa,
                                    valor
                                )
                            }
                        }
                    }
                }
            }
        }

        //TODO Retirar o trecho abaixo quando implementar matriz simétrica. Pois só serve pra terminar de preencher matriz do Apache Commons Math
        matrizDeRigidezGlobalDOFsLivres.tornarSimetrica()
//        matrizDeRigidezGlobalDOFsRestritos.tornarSimetrica()


        val vetorDeForcasDOFsLivres = OpenMapRealMatrix(qtdDOFsLivres, 1)
        val vetorDeForcasDOFsRestritos = OpenMapRealMatrix(qtdDOFsRestritos, 1)
        cargas.forEach { (carga, nos) ->
            nos.forEach { no ->
                val indiceGlobalNo = nosDaEstrutura[no]!!
                val indiceGlobalDOF = vetorDeEspalhamentoDOFS[indiceGlobalNo][dofsAtivos.indexOf(carga.dof)]
                if (indiceGlobalDOF < qtdDOFsLivres) {
                    vetorDeForcasDOFsLivres.addToEntry(
                        indiceGlobalDOF, 0,
                        carga.magnitude
                    )
                } else {
                    vetorDeForcasDOFsRestritos.addToEntry(
                        indiceGlobalDOF - qtdDOFsLivres, 0,
                        carga.magnitude
                    )
                }
            }
        }

        val vetorDeslocamentosNodais =
            MatrixUtils.inverse(matrizDeRigidezGlobalDOFsLivres).multiply(vetorDeForcasDOFsLivres)

        matrizDeRigidezGlobalDOFsLivres.imprimir()
        vetorDeslocamentosNodais.imprimir()

        val deslocamentos = hashMapOf<No2D, Deslocamento>().apply {
            nosDaEstrutura.forEach { (no, indiceNo) ->
                val mapDeslocamentos = hashMapOf<DOF, Double>()
                dofsAtivos.forEachIndexed { index, dof ->
                    val indiceGlobalDOF = vetorDeEspalhamentoDOFS[indiceNo][index]
                    if (indiceGlobalDOF < qtdDOFsLivres) {
                        mapDeslocamentos[dof] = vetorDeslocamentosNodais.getEntry(indiceGlobalDOF, 0)
                    }
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

        val vetorReacoesDeApoio = matrizDeRigidezGlobalDOFsRestritos
            .multiply(vetorDeslocamentosNodais)
            .subtract(vetorDeForcasDOFsRestritos)

        vetorReacoesDeApoio.imprimir()

        val reacoesDeApoio = hashMapOf<No2D, CargaNodal>().apply {
            val mapReacoes = hashMapOf<DOF, Double>()
            restricoes.forEach { (no, dofsRestritos) ->
                val indiceGlobalNo = nosDaEstrutura[no]!!
                dofsAtivos.forEachIndexed { index, dof ->
                    val indiceGlobalDOF = vetorDeEspalhamentoDOFS[indiceGlobalNo][index]
                    if (indiceGlobalDOF >= qtdDOFsLivres) {
                        mapReacoes[dof] = vetorReacoesDeApoio.getEntry(
                            indiceGlobalDOF - qtdDOFsLivres,
                            0
                        )
                    }
                }
                this[no] = CargaNodal(
                    fx = mapReacoes[DOF.UX] ?: 0.0,
                    fy = mapReacoes[DOF.UY] ?: 0.0,
                    fz = mapReacoes[DOF.UZ] ?: 0.0,
                    mx = mapReacoes[DOF.RX] ?: 0.0,
                    my = mapReacoes[DOF.RY] ?: 0.0,
                    mz = mapReacoes[DOF.RZ] ?: 0.0,
                )
            }
        }

        reacoesDeApoio.forEach { (no, reacao) ->
            println("Nó $no")
            println(reacao)
        }

        return deslocamentos
    }
}

fun RealMatrix.tornarSimetrica() {
    (0 until columnDimension).forEach { coluna ->
        (coluna until columnDimension).forEach { linha ->
            setEntry(linha, coluna, getEntry(coluna, linha))
        }
    }
}