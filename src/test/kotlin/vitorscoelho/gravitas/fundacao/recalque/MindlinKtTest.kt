package vitorscoelho.gravitas.fundacao.recalque

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import vitorscoelho.gravitas.utils.gpa
import vitorscoelho.gravitas.utils.metros
import vitorscoelho.gravitas.utils.mpa
import kotlin.math.pow

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
        assertEquals(0.03172500123025457, recalquePelasForcasDoFuste[pontoRecalque1]!!)//Alonso=0.033
        assertEquals(0.8518878807539629, recalquePelasForcasDaPonta[pontoRecalque1]!!)//Alonso=0.852
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

        //Ponto1
        assertEquals(0.1905384203535907, recalquePelasForcasDeFuste[pontoRecalque1]!!)//Alonso=0.199
        assertEquals(1.142798093519414, recalquePelasForcasDePonta[pontoRecalque1]!!)//Alonso=1.139

        //Ponto2
        assertEquals(0.07133995730202591, recalquePelasForcasDeFuste[pontoRecalque2]!!)//Alonso=0.073
        assertEquals(1.0362294980271745, recalquePelasForcasDePonta[pontoRecalque2]!!)//Alonso=1.036
    }

    /**
     * Exemplo do Apêndice A do livro Fundações em Estacas de Bernadete Ragoni Danziger e Francisco De Rezende Lopes
     */
    @Test
    fun exemploDanzigerELopes() {
        val secao = SecaoCircular(
            diametro = 60.0, divisoesCircunferenciaArea = 5, divisoesRaioArea = 5, divisoesPerimetro = 5
        )

        fun atritoLateral(pressaoLateralEmKnPorM2: Double) = pressaoLateralEmKnPorM2 * secao.perimetro / 100.0.pow(2)

        val atrito1 = atritoLateral(10.0)
        val atrito2 = atritoLateral(15.0)
        val atrito3 = atritoLateral(25.0)
        val trecho1 = TrechoDeEstaca(
            secao = secao, moduloDeDeformacao = 2100.0,
            qtdDivisoes = 10, atritoLateralTopo = atrito1, atritoLateralBase = atrito1,
            comprimento = 10.metros
        )
        val trecho2 = TrechoDeEstaca(
            secao = secao, moduloDeDeformacao = 2100.0,
            qtdDivisoes = 10, atritoLateralTopo = atrito2, atritoLateralBase = atrito3,
            comprimento = 10.metros
        )

        val estaca = EstacaPrismatica(
            topo = Vetor3D.ZERO,
            trechos = listOf(trecho1, trecho2),
            cargaNaPonta = 434.5
        )

        val camada1 = Camada(profundidade = 10.metros, moduloDeYoung = 10.mpa, poisson = 0.3)
        val camada2 = Camada(profundidade = 20.metros, moduloDeYoung = 15.mpa, poisson = 0.35)
        val camada3 = Camada(profundidade = 25.metros, moduloDeYoung = 40.mpa, poisson = 0.4)
        val camadas = listOf(camada1, camada2, camada3)

        val pontoRecalque = Vetor3D.ZERO.copy(z = 20.metros)

        val recalquePelasForcasDeFuste = recalqueMindlinSteinbrenner(
            forcas = estaca.forcasDoFuste, pontosRecalque = listOf(pontoRecalque), camadas = camadas
        )
        val recalquePelasForcasDePonta = recalqueMindlinSteinbrenner(
            forcas = estaca.forcasDaPonta, pontosRecalque = listOf(pontoRecalque), camadas = camadas
        )

        //Estaca isolada
        assertEquals(0.06901327720536186, recalquePelasForcasDeFuste[pontoRecalque]!!)//No livro, é 0.08
        assertEquals(0.8438271556356041, recalquePelasForcasDePonta[pontoRecalque]!!)

        //Estaca central em grupo de nove estacas espaçadas em 180cm
        val grupoDeEstacas = listOf(-180.0, 0.0, 180.0).flatMap { x ->
            listOf(-180.0, 0.0, 180.0).map { y -> estaca.copy(topo = Vetor3D(x = x, y = y, z = 0.0)) }
        }

        val forcasFusteGrupo = grupoDeEstacas.asSequence().flatMap { it.forcasDoFuste }
        val forcasPontaGrupo = grupoDeEstacas.asSequence().flatMap { it.forcasDaPonta }

        val recalquePelasForcasDeFusteGrupo = recalqueMindlinSteinbrenner(
            forcas = forcasFusteGrupo, pontosRecalque = listOf(pontoRecalque), camadas = camadas
        )
        val recalquePelasForcasDePontaGrupo = recalqueMindlinSteinbrenner(
            forcas = forcasPontaGrupo, pontosRecalque = listOf(pontoRecalque), camadas = camadas
        )
        assertEquals(0.24786087499576462, recalquePelasForcasDeFusteGrupo[pontoRecalque]!!)//No livro, é 0.28
        assertEquals(1.0585596726046522, recalquePelasForcasDePontaGrupo[pontoRecalque]!!)
    }
}