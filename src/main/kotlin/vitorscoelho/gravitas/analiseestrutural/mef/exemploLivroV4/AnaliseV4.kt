package vitorscoelho.gravitas.analiseestrutural.mef.exemploLivroV4

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix

enum class DOF { UX, UY, UZ, RX, RY, RZ; }

/**
 * Lista com os [DOF] ativos.
 * A ordem da lista sempre respeitará a ordem UX, UY, UZ, RX, RY, RZ
 */
class DOFsAtivos(
    ux: Boolean, uy: Boolean, uz: Boolean,
    rx: Boolean, ry: Boolean, rz: Boolean,
) : List<DOF> by criarImplementacaoDeList(ux = ux, uy = uy, uz = uz, rx = rx, ry = ry, rz = rz) {
    //    fun indiceGlobal(indiceGlobalNo: Int, indiceLocalDOF: Int): Int = this.size * indiceGlobalNo + indiceLocalDOF
    fun indiceGlobal(indiceGlobalNo: Int, dof: DOF): Int {
        val indiceDOF = this.indexOf(dof)
        require(indiceDOF >= 0) { "$dof não é um DOF ativo" }
        return this.size * indiceGlobalNo + indiceDOF
    }

    companion object {
        private fun criarImplementacaoDeList(
            ux: Boolean, uy: Boolean, uz: Boolean,
            rx: Boolean, ry: Boolean, rz: Boolean,
        ): List<DOF> {
            val lista = listOf(ux, uy, uz, rx, ry, rz)
            return DOF.values().filterIndexed { index, _ -> lista[index] }
        }

        fun portico3d() = DOFsAtivos(
            ux = true, uy = true, uz = true,
            rx = true, ry = true, rz = true,
        )

        fun portico2d() = DOFsAtivos(
            ux = true, uy = false, uz = true,
            rx = false, ry = true, rz = false,
        )

        fun grelha() = DOFsAtivos(
            ux = false, uy = false, uz = true,
            rx = true, ry = true, rz = false,
        )

        fun trelica3d() = DOFsAtivos(
            ux = true, uy = true, uz = true,
            rx = false, ry = false, rz = false,
        )

        fun trelica2d() = DOFsAtivos(
            ux = true, uy = false, uz = true,
            rx = false, ry = false, rz = false,
        )
    }
}

data class Deslocamento(
    val ux: Double, val uy: Double, val uz: Double,
    val rx: Double, val ry: Double, val rz: Double,
)

class Carga(val magnitude: Double, val dof: DOF)

class AnaliseTesteElementosCST(
    val elementos: List<ElementoCST>,
    val restricoes: Map<No2D, List<DOF>>,
    val cargas: Map<Carga, List<No2D>>,
    val dofsAtivos: DOFsAtivos
) {
    fun solve(): Map<No2D, Deslocamento> {
        val nosDaEstrutura = elementos
            .asSequence()
            .flatMap { it.nos }
            .distinct()
            .mapIndexed { index, no2D -> Pair(no2D, index) }
            .toMap()

        val qtdGrausDeLiberdade = nosDaEstrutura.size * dofsAtivos.size

        val matrizDeRigidezGlobal = Array2DRowRealMatrix(qtdGrausDeLiberdade, qtdGrausDeLiberdade)
        elementos
            .forEach { elementoCST ->
                val keElemento = elementoCST.matrizDeRigidezKe()
                elementoCST.nos.forEach { noEfeito ->
                    dofsAtivos.forEach { dofEfeito ->
                        elementoCST.nos.forEach { noCausa ->
                            dofsAtivos.forEach { dofCausa ->
                                val valor = keElemento.valor(
                                    noEfeito = noEfeito, dofEfeito = dofEfeito,
                                    noCausa = noCausa, dofCausa = dofCausa,
                                )
                                val indiceGlobalDOFEfeito = dofsAtivos.indiceGlobal(
                                    indiceGlobalNo = nosDaEstrutura[noEfeito]!!,
                                    dof = dofEfeito,
                                )
                                val indiceGlobalDOFCausa = dofsAtivos.indiceGlobal(
                                    indiceGlobalNo = nosDaEstrutura[noCausa]!!,
                                    dof = dofCausa,
                                )
                                matrizDeRigidezGlobal.addToEntry(
                                    indiceGlobalDOFEfeito,
                                    indiceGlobalDOFCausa,
                                    valor
                                )
                            }
                        }
                    }
                }
            }
        //Alterando a matriz de rigidez global para considerar as restrições de apoio
        //TODO ver outra maneira de fazer este processo
        restricoes.forEach { (no, restricoesNo) ->
            val indiceGlobalNoRestricao = nosDaEstrutura[no]!!
            restricoesNo.forEach { dofRestricao ->
                val indiceDOFGlobalRestricao = dofsAtivos.indiceGlobal(
                    indiceGlobalNo = indiceGlobalNoRestricao,
                    dof = dofRestricao
                )
                matrizDeRigidezGlobal.setEntry(indiceDOFGlobalRestricao, indiceDOFGlobalRestricao, 10e12)
            }
        }

        matrizDeRigidezGlobal.imprimir()

        val vetorDeForcasNodais: RealMatrix = OpenMapRealMatrix(qtdGrausDeLiberdade, 1).apply {
            cargas.forEach { (carga, nos) ->
                nos.forEach { no ->
                    val indiceGlobalDOF = dofsAtivos.indiceGlobal(
                        indiceGlobalNo = nosDaEstrutura[no]!!,
                        dof = carga.dof
                    )
                    addToEntry(indiceGlobalDOF, 0, carga.magnitude)
                }
            }
        }

        val vetorDeslocamentosNodais = MatrixUtils.inverse(matrizDeRigidezGlobal) * vetorDeForcasNodais
        vetorDeslocamentosNodais.imprimir()

        fun getDeslocamento(indiceGlobalNo: Int, dof: DOF): Double {
            if (!dofsAtivos.contains(dof)) return 0.0
            val indiceGlobalDOF = dofsAtivos.indiceGlobal(
                indiceGlobalNo = indiceGlobalNo,
                dof = dof
            )
            return vetorDeslocamentosNodais.getEntry(indiceGlobalDOF, 0)
        }

        return hashMapOf<No2D, Deslocamento>().apply {
            nosDaEstrutura.forEach { (no, indiceNo) ->
                this[no] = Deslocamento(
                    ux = getDeslocamento(indiceGlobalNo = indiceNo, dof = DOF.UX),
                    uy = getDeslocamento(indiceGlobalNo = indiceNo, dof = DOF.UY),
                    uz = getDeslocamento(indiceGlobalNo = indiceNo, dof = DOF.UZ),
                    rx = getDeslocamento(indiceGlobalNo = indiceNo, dof = DOF.RX),
                    ry = getDeslocamento(indiceGlobalNo = indiceNo, dof = DOF.RY),
                    rz = getDeslocamento(indiceGlobalNo = indiceNo, dof = DOF.RZ),
                )
            }
        }
    }
}