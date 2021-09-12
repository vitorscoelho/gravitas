package vitorscoelho.gravitas.fundacao.recalque

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import vitorscoelho.gravitas.utils.gpa
import vitorscoelho.gravitas.utils.metros

internal class MindlinKtTest {
    /**
     * 1° exercício do capítulo 7 do livro Dimensionamento de Fundações Profundas de Urbano Rodriguez Alonso
     */
    @Test
    fun exemplo1LivroAlonso1() {
        val n1 = 5
        val n2 = 5
        val n3 = 5
        val moduloDeDeformacaoConcreto = 21.gpa
        val secao = SecaoCircular(
            diametro = 50.0, divisoesCircunferenciaArea = n1, divisoesRaioArea = n2, divisoesPerimetro = n1
        )
        val estacaTrecho1 = TrechoDeEstaca(
            secao = secao, moduloDeDeformacao = moduloDeDeformacaoConcreto,
            qtdDivisoes = n3,
            comprimento = 4.metros, atritoLateralTopo = 0.0, atritoLateralBase = 0.2
        )
        val estacaTrecho2 = estacaTrecho1.copy(
            comprimento = 5.metros, atritoLateralTopo = 0.3, atritoLateralBase = 0.3
        )
        val estacaTrecho3 = estacaTrecho1.copy(
            comprimento = 4.metros, atritoLateralTopo = 0.5, atritoLateralBase = 0.6
        )

        val estacaPrismatica = EstacaPrismatica(
            topo = Vetor3D(x = 100.0, y = 200.0, z = 300.0),
            trechos = listOf(estacaTrecho1, estacaTrecho2, estacaTrecho3),
            cargaNaPonta = 890.0
        )

        val camadas = listOf(
            Camada(profundidade = 1900.0, moduloDeYoung = 10.0, poisson = 0.3),
            Camada(profundidade = 2300.0, moduloDeYoung = 22.0, poisson = 0.25),
        )

        val pontoRecalque1 = Vetor3D(x = 100.0, y = 200.0, z = 1600.0)

        val pontosRecalque = listOf(pontoRecalque1)

        val recalquePelasForcasDoFuste = recalqueMindlinSteinbrenner(
            forcas = estacaPrismatica.forcasDoFuste,
            pontosRecalque = pontosRecalque,
            camadas = camadas
        )

        val recalquePelasForcasDaPonta = recalqueMindlinSteinbrenner(
            forcas = estacaPrismatica.forcasDaPonta,
            pontosRecalque = pontosRecalque,
            camadas = camadas
        )
        val delta = 0.000001
        assertEquals(0.031645, recalquePelasForcasDoFuste[pontoRecalque1]!!, delta)//Alonso=0.033
        assertEquals(0.851887, recalquePelasForcasDaPonta[pontoRecalque1]!!, delta)//Alonso=0.852
    }

    /**
     * 2° exercício do capítulo 7 do livro Dimensionamento de Fundações Profundas de Urbano Rodriguez Alonso
     */
    @Test
    fun exemplo1LivroAlonso2() {
        val secao = SecaoCircular(
            diametro = 50.0, divisoesCircunferenciaArea = 5, divisoesRaioArea = 5, divisoesPerimetro = 5
        )
        val trechoEstacaA = TrechoDeEstaca(
            secao = secao, moduloDeDeformacao = 2100.0,
            qtdDivisoes = 5, atritoLateralTopo = 0.42, atritoLateralBase = 0.42,
            comprimento = 13.metros
        )
        val trechoEstacaBC = trechoEstacaA.copy(
            atritoLateralTopo = 0.27, atritoLateralBase = 0.27,
            comprimento = 20.metros
        )

        val estacaA = EstacaPrismatica(
            topo = Vetor3D(x = 0.0, y = 108.5, z = 0.0),
            trechos = listOf(trechoEstacaA),
            cargaNaPonta = 540.0
        )
        val estacaB = EstacaPrismatica(
            topo = Vetor3D(x = 125.0, y = 108.5, z = 0.0),
            trechos = listOf(trechoEstacaBC),
            cargaNaPonta = 540.0
        )
        val estacaC = EstacaPrismatica(
            topo = Vetor3D(x = 62.5, y = 0.0, z = 0.0),
            trechos = listOf(trechoEstacaBC),
            cargaNaPonta = 540.0
        )
        val estacas = listOf(estacaA, estacaB, estacaC)

        val camadas = listOf(
            Camada(profundidade = 1300.0, moduloDeYoung = 4.0, poisson = 0.35),
            Camada(profundidade = 1500.0, moduloDeYoung = 6.0, poisson = 0.30),
            Camada(profundidade = 2000.0, moduloDeYoung = 4.0, poisson = 0.35),
            Camada(profundidade = 2300.0, moduloDeYoung = 6.0, poisson = 0.30),
            Camada(profundidade = 3000.0, moduloDeYoung = 4.0, poisson = 0.35),
        )

        val pontoRecalque1 = Vetor3D(x = 0.0, y = 108.5, z = 1300.0)
        val pontoRecalque2 = Vetor3D(x = 125.0, y = 108.5, z = 2000.0)
        val pontosRecalque = listOf(pontoRecalque1, pontoRecalque2)

        val forcasDeFuste = estacas.asSequence().flatMap { it.forcasDoFuste }
        val forcasDePonta = estacas.asSequence().flatMap { it.forcasDaPonta }

        val recalquePelasForcasDeFuste = recalqueMindlinSteinbrenner(
            forcas = forcasDeFuste, pontosRecalque = pontosRecalque, camadas = camadas
        )
        val recalquePelasForcasDePonta = recalqueMindlinSteinbrenner(
            forcas = forcasDePonta, pontosRecalque = pontosRecalque, camadas = camadas
        )

        val delta = 0.000001
        //Ponto1
        assertEquals(0.190538, recalquePelasForcasDeFuste[pontoRecalque1]!!, delta)//Alonso=0.199
        assertEquals(1.142798, recalquePelasForcasDePonta[pontoRecalque1]!!, delta)//Alonso=1.139

        //Ponto2
        assertEquals(0.071339, recalquePelasForcasDeFuste[pontoRecalque2]!!, delta)//Alonso=0.073
        assertEquals(1.036229, recalquePelasForcasDePonta[pontoRecalque2]!!, delta)//Alonso=1.036
    }
}