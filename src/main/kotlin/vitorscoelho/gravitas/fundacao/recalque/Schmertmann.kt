package vitorscoelho.gravitas.fundacao.recalque

import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

//interface CamadaSchmertmann {
//    val cotaTopo: Double
//    val cotaBase: Double
//    val espessura: Double
//    val moduloDeDeformacacao: Double
//    val pesoEspecifico: Double
//}
//
//enum class TipoSapataSchmertmann(val multiplicadorCotaIzMax: Double, val izCotaApoio: Double) {
//    QUADRADA(multiplicadorCotaIzMax = 0.5, izCotaApoio = 0.1),
//    RETANGULAR(multiplicadorCotaIzMax = 0.75, izCotaApoio = 0.15),
//    CORRIDA(multiplicadorCotaIzMax = 1.0, izCotaApoio = 0.2),
//    ;
//}
//
//private class CalculadoraIz(val cotaApoioSapata: Double, val tipoSapataSchmertmann: TipoSapataSchmertmann) {
//    /**Profundidade a partir da base da sapata*/
//    fun cotaIzMax(cotaApoioSapata: Double, menorDimensaoSapata: Double): Double =
//        cotaApoioSapata + menorDimensaoSapata * tipoSapataSchmertmann.multiplicadorCotaIzMax
//
//    fun iz(cotaApoioSapata: Double)
//}
//
//class CamadasSchmertmann private constructor(val lista: List<CamadaSchmertmann>) {
//    val cotaTopo: Double get() = lista.first().cotaTopo
//    val cotaBase: Double get() = lista.last().cotaBase
//
//    fun cortar(cota1: Double, cota2: Double): CamadasSchmertmann {
//        require(cota1 != cota2)
//        val cotaMaxima = max(cota1, cota2)
//        val cotaMinima = min(cota1, cota2)
//        require(cotaMaxima > cotaBase)
//        require(cotaMinima < cotaTopo)
//
//    }
//
//    companion object {
//        class Builder(cotaTopo: Double) {
//            private val lista = mutableListOf<CamadaSchmertmann>()
//            private var cotaTopoAtual: Double = cotaTopo
//
//            fun add(espessura: Double, moduloDeDeformacao: Double, pesoEspecifico: Double) {
//                require(espessura > 0.0)
//                require(moduloDeDeformacao > 0.0)
//                require(pesoEspecifico > 0.0)
//                val cotaTopo = cotaTopoAtual
//                val cotaBase = cotaTopo - espessura
//                cotaTopoAtual = cotaBase
//                lista += object : CamadaSchmertmann {
//                    override val cotaTopo = cotaTopo
//                    override val cotaBase = cotaBase
//                    override val espessura = espessura
//                    override val moduloDeDeformacacao = moduloDeDeformacao
//                    override val pesoEspecifico = pesoEspecifico
//                }
//            }
//
//            fun build(): CamadasSchmertmann {
//                require(lista.isNotEmpty())
//                return CamadasSchmertmann(lista = lista)
//            }
//        }
//
//        fun build(cotaTopo: Double, op: Builder.() -> Unit): CamadasSchmertmann {
//            val builder = Builder(cotaTopo = cotaTopo)
//            op(builder)
//            return builder.build()
//        }
//    }
//}
//
//fun recalqueSchmertmann(
//    tensaoBaseSapata: Double,
//    cotaApoioSapata: Double,
//    menorDimensaoSapata: Double,
//    tipoSapataSchmertmann: TipoSapataSchmertmann,
//    camadas: CamadasSchmertmann,
//    anosParaRecalqueFinal: Double,
//    tensaoNoNivelDoPisoAcabado: Double,
//): Double {
//    require(tensaoBaseSapata > 0.0)
//    require(cotaApoioSapata <= camadas.cotaTopo)
//    require(menorDimensaoSapata > 0.0)
//    require(anosParaRecalqueFinal >= 0.0)
//    require(tensaoNoNivelDoPisoAcabado >= 0.0)
//
//    val camadasAcimaDaSapata = camadas.cortar(cota1 = cotaApoioSapata, cota2 = Double.MAX_VALUE)
//    val sobrecargaNaCotaDeApoio = camadas.lista.sumOf { it.espessura * it.pesoEspecifico } + tensaoNoNivelDoPisoAcabado
//    val tensaoLiquida = max(tensaoBaseSapata - sobrecargaNaCotaDeApoio, 0.0)
//
//    val cotaIzMax = cotaIzMax(cotaDeApoio = cotaApoioSapata)
//    val camadasAcimaDoIzMaximo = camadas.cortar(cota1 = cotaIzMax, cota2 = cotaApoioSapata)
//    val camadasAbaixoDoIzMaximo = camadas.cortar(cota1 = Double.MIN_VALUE, cota2 = cotaIzMax)
//    val areaIz = (camadasAcimaDoIzMaximo.lista + camadasAbaixoDoIzMaximo.lista).sumOf { camada ->
//        val cotaMedia = (camada.cotaBase + camada.cotaTopo) / 2.0
//        val izMedio = tipoSapataSchmertmann.iz(cotaDeApoio = cotaApoioSapata, cotaIz = cotaMedia)
//        izMedio * camada.espessura / camada.moduloDeDeformacacao
//    }
//
//    val c1 = max(
//        1.0 - 0.5 * (sobrecargaNaCotaDeApoio / tensaoLiquida),
//        0.5
//    )
//    val c2 = 1.0 + 0.2 * log10(anosParaRecalqueFinal / 0.1)
//
//    return c1 * c2 * tensaoLiquida * areaIz
//}