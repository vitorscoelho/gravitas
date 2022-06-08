package vitorscoelho.gravitas.concretoarmado.metodogeralpilar

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.OpenMapRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5.*
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.EsforcosFlexaoReta
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math.simpson
import vitorscoelho.gravitas.utils.gpa
import kotlin.math.pow
import kotlin.math.sqrt

fun main() {
    val secao = SecaoPilar(
        area = 20.0 * 40.0,
        inercia = 20.0 * 40.0.pow(3) / 12.0,
        moduloDeDeformacao = 25.gpa
    )
    val abscissas = run {
        val lista = mutableListOf<Double>()
        val delta = 10
        var x = 0
        while (x <= 400) {
            lista += x.toDouble()
            x += delta
        }
        lista
    }
//    val abscissas = listOf(0.0, 100.0, 200.0, 300.0, 400.0)
    val nos = abscissas.map { x -> No2D(x = x, y = 0.0) }
    val barras = nos.zipWithNext { no1, no2 ->
        BarraPilar(secao = secao, noInicial = no1, noFinal = no2)
    }
    val restricoes = mapOf<No2D, List<DOF>>(
        //nos[0] to listOf(DOF.UX, DOF.UY, DOF.RZ)
    )
    val cargas = mapOf(
        Carga(magnitude = 100.0, dof = DOF.UY) to listOf(nos.last()),
        Carga(magnitude = -400.0, dof = DOF.UX) to listOf(nos.last()),
    )
    val mola = Mola(
        no = nos.last(),
        kTx = 0.0, kTy = 200.0 / 100.0, kRz = 2000.0 * 100.0
    )
    val molaRestricao = Mola(
        no = nos.first(),
        kTx = 10e8, kTy = 10e8, kRz = 10e8
    )

//    val resultados = analiseSolverLinear(
//        elementos = barras + mola, restricoes = restricoes, cargas = cargas,
//        dofsAtivos = DOFsAtivos(
//            ux = true, uy = true, uz = false,
//            rx = false, ry = false, rz = true
//        )//TODO testar as outras maneiras
//    )
    val resultados = analiseSolverNaoLinearBarra2D(
        elementos = barras + mola + molaRestricao,
        restricoes = restricoes,
        cargas = cargas
    )

    println(resultados.deslocamento(nos.last()))
    println(resultados.reacao(nos.first()))
//    println(molaApoio.forcas(deslocamento = resultados[nos.first()]!!))

    val vetorForcasNaoLineares = DoubleArray(size = nos.size * 3)
    barras.forEach { barra ->
        val deslocamentoNo1 = resultados.deslocamento(no = barra.noInicial)
        val deslocamentoNo2 = resultados.deslocamento(no = barra.noFinal)
        val deslocamentos = doubleArrayOf(
            deslocamentoNo1.ux, deslocamentoNo1.uy, deslocamentoNo1.rz,
            deslocamentoNo2.ux, deslocamentoNo2.uy, deslocamentoNo2.rz,
        )
        val f = barra.forcasInternas(
            deslocamentos = deslocamentos
        ).toList()
        println(f)
        val indiceGlobalNoInicial = nos.indexOf(barra.noInicial)
        val indiceGlobalNoFinal = nos.indexOf(barra.noFinal)
        vetorForcasNaoLineares[3 * indiceGlobalNoInicial] += f[0]
        vetorForcasNaoLineares[3 * indiceGlobalNoInicial + 1] += f[1]
        vetorForcasNaoLineares[3 * indiceGlobalNoInicial + 2] += f[2]
        vetorForcasNaoLineares[3 * indiceGlobalNoFinal] += f[3]
        vetorForcasNaoLineares[3 * indiceGlobalNoFinal + 1] += f[4]
        vetorForcasNaoLineares[3 * indiceGlobalNoFinal + 2] += f[5]
    }
    val forcasMola = mola.forcas(deslocamento = resultados.deslocamento(nos.last()))
    val indiceGlobalNoMola = nos.indexOf(nos.last())
    vetorForcasNaoLineares[3 * indiceGlobalNoMola] += forcasMola.fx
    vetorForcasNaoLineares[3 * indiceGlobalNoMola + 1] += forcasMola.fy
    vetorForcasNaoLineares[3 * indiceGlobalNoMola + 2] += forcasMola.mz
    nos.forEach { no ->
        val indiceGlobalNoReacaoDeApoio = nos.indexOf(no)
        val reacaoDeApoio = resultados.reacao(no = no)
        vetorForcasNaoLineares[3 * indiceGlobalNoReacaoDeApoio] += reacaoDeApoio.fx
        vetorForcasNaoLineares[3 * indiceGlobalNoReacaoDeApoio + 1] += reacaoDeApoio.fy
        vetorForcasNaoLineares[3 * indiceGlobalNoReacaoDeApoio + 2] += reacaoDeApoio.mz
    }
    println(vetorForcasNaoLineares.toList())
}

data class SecaoPilar(val area: Double, val inercia: Double, val moduloDeDeformacao: Double)

data class Restricao(
    val no: No2D,
    val ux: Boolean = false, val uy: Boolean = false, val uz: Boolean = false,
    val rx: Boolean = false, val ry: Boolean = false, val rz: Boolean = false,
) : ElementoFinito {
    override val nos: List<No2D> = listOf(no)

    override fun matrizDeRigidez(): MatrizDeRigidez {
        TODO("Not yet implemented")
    }
}

data class Mola(
    val no: No2D,
    val kTx: Double = 0.0, val kTy: Double = 0.0, val kTz: Double = 0.0,
    val kRx: Double = 0.0, val kRy: Double = 0.0, val kRz: Double = 0.0
) : ElementoFinitoComForcasInternas {
    override val nos: List<No2D> = listOf(no)

    override fun matrizDeRigidez() = object : MatrizDeRigidez {
        private val valores = doubleArrayOf(kTx, kTy, kTz, kRx, kRy, kRz)
        override fun valor(indiceLocalDOFEfeito: Int, indiceLocalDOFCausa: Int): Double {
            if (indiceLocalDOFEfeito == indiceLocalDOFCausa) return valores[indiceLocalDOFEfeito]
            return 0.0
        }
    }

    fun forcas(deslocamento: Deslocamento) = CargaNodal(
        fx = kTx * deslocamento.ux,
        fy = kTy * deslocamento.uy,
        fz = kTz * deslocamento.uz,
        mx = kRx * deslocamento.rx,
        my = kRy * deslocamento.ry,
        mz = kRz * deslocamento.rz
    )

    override fun forcasInternas(vetorDeslocamento: VetorDeslocamento): VetorForca {
        val array = DoubleArray(size = 6)
        array[0] = kTx * vetorDeslocamento.valor(0)
        array[1] = kTy * vetorDeslocamento.valor(1)
        array[2] = kTz * vetorDeslocamento.valor(2)
        array[3] = kRx * vetorDeslocamento.valor(3)
        array[4] = kRy * vetorDeslocamento.valor(4)
        array[5] = kRz * vetorDeslocamento.valor(5)
        return object : VetorForca {
            override fun valor(indiceLocalDOF: Int): Double {
                return array[indiceLocalDOF]
            }
        }
    }
}

data class BarraPilar(
    val secao: SecaoPilar,
    val noInicial: No2D,
    val noFinal: No2D,
    val esforcosResistentes: (deformacaoCG: Double, curvatura: Double) -> EsforcosFlexaoReta = { deformacaoCG: Double, curvatura: Double ->
        EsforcosFlexaoReta(
            normal = deformacaoCG * secao.area * secao.moduloDeDeformacao,
            momento = curvatura * secao.inercia * secao.moduloDeDeformacao
        )
    },
) : ElementoFinitoComForcasInternas {
    override val nos: List<No2D> = listOf(noInicial, noFinal)

    private val l = run {
        val deltaX = noFinal.x - noInicial.x
        val deltaY = noFinal.y - noInicial.y
        sqrt(deltaX * deltaX + deltaY * deltaY)
    }

//    private fun deslocamento(
//        x: Double,
//        deslocamentoPontoInicial: Deslocamento,
//        deslocamentoPontofinal: Deslocamento
//    ): Deslocamento {
//        val fi = funcoesDeForma(x = x)
//        val (u1, u2, u3) = with(deslocamentoPontoInicial) { doubleArrayOf(tx, ty, r) }
//        val (u4, u5, u6) = with(deslocamentoPontofinal) { doubleArrayOf(tx, ty, r) }
//        return Deslocamento(
//            tx = fi[0] * u1 + fi[3] * u4,
//            ty = fi[1] * u2 + fi[2] * u3 + fi[4] * u5 + fi[5] * u6,
//            r = 0.0
//        )
//    }

    private fun u0Generico(forma: DoubleArray, deslocamentos: DoubleArray): Double {
        return forma[0] * deslocamentos[0] + forma[3] * deslocamentos[3]
    }

    private fun wGenerico(forma: DoubleArray, deslocamentos: DoubleArray): Double {
        return forma[1] * deslocamentos[1] + forma[2] * deslocamentos[2] + forma[4] * deslocamentos[4] + forma[5] * deslocamentos[5]
    }

    /**
     * @param x abscissa de estudo
     * @param normal esforço normal na abscissa [x]
     * @param momento momento fletor na abscissa [x]
     * @param deslocamentos deslocamentos nas extremidades da barra
     */
    private fun forcasInternasParcial(
        x: Double,
        deslocamentos: DoubleArray
    ): DoubleArray {
        val forma = funcoesDeForma(x = x)
        val forma1 = funcoesDeFormaPrimeiraDerivada(x = x)
        val forma2 = funcoesDeFormaSegundaDerivada(x = x)

        val u0 = u0Generico(forma = forma, deslocamentos = deslocamentos)
        val u0x = u0Generico(forma = forma1, deslocamentos = deslocamentos)
        val w = wGenerico(forma = forma, deslocamentos = deslocamentos)
        val wx = wGenerico(forma = forma1, deslocamentos = deslocamentos)
        val wxx = wGenerico(forma = forma2, deslocamentos = deslocamentos)

        val deformacaoCG =
            u0x + 0.5 * wx * wx //TODO tirar comentário para considerar a não-linearidade geométrica. Tem que criar o processo iterativo pra poder chegar na resposta final
        val curvatura = -wxx

        val esforcoInterno = esforcosResistentes(deformacaoCG, curvatura)
        val normal = esforcoInterno.normal
        val momento = esforcoInterno.momento

        val parcelaNormal =
            normal * (forma1[1] * deslocamentos[1] + forma1[2] * deslocamentos[2] + forma1[4] * deslocamentos[4] + forma1[5] * deslocamentos[5])
        return doubleArrayOf(
            normal * forma1[0],
            -momento * forma2[1] + parcelaNormal * forma1[1],
            -momento * forma2[2] + parcelaNormal * forma1[2],
            normal * forma1[3],
            -momento * forma2[4] + parcelaNormal * forma1[4],
            -momento * forma2[5] + parcelaNormal * forma1[5]
        )
    }

    fun forcasInternas(
        deslocamentos: DoubleArray
    ): DoubleArray {
        return simpson(
            f = { x: Double ->
                forcasInternasParcial(x = x, deslocamentos = deslocamentos)
            },
            a = 0.0, b = l,
            delta = DoubleArray(size = 6) { 0.001 }
        )
    }

    override fun forcasInternas(vetorDeslocamento: VetorDeslocamento): VetorForca {
        val arrayForcasInternas = forcasInternas(
            deslocamentos = doubleArrayOf(
                vetorDeslocamento.valor(0),
                vetorDeslocamento.valor(1),
                vetorDeslocamento.valor(5),
                vetorDeslocamento.valor(6),
                vetorDeslocamento.valor(7),
                vetorDeslocamento.valor(11),
            )
        )
        val arrayRedimensionado = DoubleArray(size = 12)
        arrayRedimensionado[0] = arrayForcasInternas[0]
        arrayRedimensionado[1] = arrayForcasInternas[1]
        arrayRedimensionado[5] = arrayForcasInternas[2]
        arrayRedimensionado[6] = arrayForcasInternas[3]
        arrayRedimensionado[7] = arrayForcasInternas[4]
        arrayRedimensionado[11] = arrayForcasInternas[5]

        return object : VetorForca {
            override fun valor(indiceLocalDOF: Int): Double {
                return arrayRedimensionado[indiceLocalDOF]
            }
        }
    }

    private fun funcoesDeFormaGenerico(x: Double, xl0: Double, xl1: Double, xl2: Double, xl3: Double): DoubleArray {
        return doubleArrayOf(
            xl0 - xl1,
            2.0 * xl3 - 3.0 * xl2 + xl0,
            l * (xl3 - 2.0 * xl2 + xl1),
            xl1,
            -2.0 * xl3 + 3.0 * xl2,
            l * (xl3 - xl2)
        )
    }

    private fun funcoesDeForma(x: Double): DoubleArray {
        return funcoesDeFormaGenerico(
            x = x,
            xl0 = 1.0,
            xl1 = x / l,
            xl2 = x * x / (l * l),
            xl3 = x * x * x / (l * l * l)
        )
    }

    private fun funcoesDeFormaPrimeiraDerivada(x: Double): DoubleArray {
        return funcoesDeFormaGenerico(
            x = x,
            xl0 = 0.0,
            xl1 = 1.0 / l,
            xl2 = 2.0 * x / (l * l),
            xl3 = 3.0 * x * x / (l * l * l)
        )
    }

    private fun funcoesDeFormaSegundaDerivada(x: Double): DoubleArray {
        return funcoesDeFormaGenerico(
            x = x,
            xl0 = 0.0,
            xl1 = 0.0,
            xl2 = 2.0 / (l * l),
            xl3 = 6.0 * x / (l * l * l)
        )
    }

    override fun matrizDeRigidez(): MatrizDeRigidez {
        val matriz6x6 = matrizDeRigidez6x6()
        val vetorDeEspalhamento = intArrayOf(0, 1, 5, 6, 7, 11)
        val matriz12x12 = OpenMapRealMatrix(18, 18)
        (0..5).forEach { linha ->
            (0..5).forEach { coluna ->
                val linhaFinal = vetorDeEspalhamento[linha]
                val colunaFinal = vetorDeEspalhamento[coluna]
                matriz12x12.setEntry(
                    linhaFinal, colunaFinal,
                    matriz6x6.getEntry(linha, coluna)
                )
            }
        }
        return object : MatrizDeRigidez {
            override fun valor(indiceLocalDOFEfeito: Int, indiceLocalDOFCausa: Int): Double {
                return matriz12x12.getEntry(indiceLocalDOFEfeito, indiceLocalDOFCausa)
            }
        }
    }

    private fun matrizDeRigidez6x6(): RealMatrix {
        val matriz = Array2DRowRealMatrix(6, 6)
        val (area, inercia, moduloDeDeformacao) = with(secao) { doubleArrayOf(area, inercia, moduloDeDeformacao) }
        matriz[0, 0] = area * l * l / inercia
        matriz[0, 3] = -area * l * l / inercia
        matriz[1, 1] = 12.0
        matriz[1, 2] = 6.0 * l
        matriz[1, 4] = -12.0
        matriz[1, 5] = 6.0 * l
        matriz[2, 2] = 4.0 * l * l
        matriz[2, 4] = -6.0 * l
        matriz[2, 5] = 2.0 * l * l
        matriz[3, 3] = area * l * l / inercia
        matriz[4, 4] = 12.0
        matriz[4, 5] = -6.0 * l
        matriz[5, 5] = 4.0 * l * l

        return matriz.scalarMultiply(moduloDeDeformacao * inercia / l.pow(3)).tornarSimetrica()
    }
}

private operator fun RealMatrix.get(linha: Int, coluna: Int): Double = this.getEntry(linha, coluna)
private operator fun RealMatrix.set(linha: Int, coluna: Int, valor: Double) = this.setEntry(linha, coluna, valor)

private fun RealMatrix.tornarSimetrica(): RealMatrix {
    (1 until columnDimension).forEach { coluna ->
        (0 until coluna).forEach { linha ->
            this[coluna, linha] = this[linha, coluna]
        }
    }
    return this
}

interface ElementoFinitoComForcasInternas : ElementoFinito {
    fun forcasInternas(vetorDeslocamento: VetorDeslocamento): VetorForca
}