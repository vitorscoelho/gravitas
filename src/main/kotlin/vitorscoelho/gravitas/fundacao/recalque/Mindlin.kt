package vitorscoelho.gravitas.fundacao.recalque

import vitorscoelho.gravitas.utils.sumOfIndexed
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

fun recalqueMindlinSteinbrenner(
    forcas: Sequence<Forca>,
    pontosRecalque: List<Vetor3D>,
    camadas: List<Camada>
): Map<Vetor3D, Double> {
    //TODO testar as maneiras comentadas abaixo e comparar qual tem melhor performance e consumo de memoria
//    return forcas
//        .associate { forca ->
//            pontosRecalque.sumOf { pontoRecalque ->
//                recalqueMindlinSteinbrenner(
//                    forca = forca,
//                    pontoRecalque = pontoRecalque,
//                    camadas = camadas
//                )
//            }
//        }
//    return pontosRecalque.associateWith { pontoRecalque ->
//        forcas.sumOf { forca ->
//            pontosRecalque.sumOf { pontoRecalque ->
//                recalqueMindlinSteinbrenner(
//                    forca = forca,
//                    pontoRecalque = pontoRecalque,
//                    camadas = camadas
//                )
//            }
//        }
//    }
    val retorno = pontosRecalque.associateWith { 0.0 }.toMutableMap()
    forcas.forEach { forca ->
        pontosRecalque.forEach { pontoRecalque ->
            retorno[pontoRecalque] = retorno[pontoRecalque]!! + recalqueMindlinSteinbrenner(
                forca = forca,
                pontoRecalque = pontoRecalque,
                camadas = camadas
            )
        }
    }
    return retorno
}

fun recalqueMindlinSteinbrenner(forca: Forca, pontoRecalque: Vetor3D, camadas: List<Camada>): Double {
    //TODO ver o que fazer quando o pontoRecalque tiver as mesma coordenadas pontoForca. Atribuir um valor bem pequeno para R1 resolve? Acho que não, pois tende ao infinito. Calcular para uma profundidade um pouco abaixo da força?
    /*
    TODO analisar se faz o que está abaixo
    pontoForca e pontoRecalque deve ter profundidade maior que zero (se zero, usar 0.0001)
    O que fazer se pontoRecalque estiver acima de pontoForca?
    Se recalque der menor que zero, retornar zero, como faz o Alonso?
     */
    /*
    TODO Tentar melhorar o código pra não precisar fazer operações com listas para encontrar a camada onde estar a força
    Talvez uma simples verificação entre o nível da camada e da carga já seja o suficiente
     */

    val indiceDaCamadaOndeEstaOPontoRecalque = camadas.indexOfFirst { camada ->
        pontoRecalque.z < camada.profundidade
    }
    if (indiceDaCamadaOndeEstaOPontoRecalque == -1) return 0.0//Se for nulo, é porque está abaixo do indeslocável
    val camadasEmAnalise = camadas.slice(indiceDaCamadaOndeEstaOPontoRecalque..camadas.lastIndex)
    val profundidadesEmAnalise =
        listOf(pontoRecalque.z) + camadasEmAnalise.map { it.profundidade }
    val recalque = camadasEmAnalise
        .sumOfIndexed { index, camada ->
            val profundidadeTopo = profundidadesEmAnalise[index]
            val profundidadeBase = profundidadesEmAnalise[index + 1]
            val x = pontoRecalque.x - forca.x
            val y = pontoRecalque.y - forca.y
            val recalqueTopo = w(
                q = forca.magnitude,
                e = camada.moduloDeYoung, v = camada.poisson,
                c = forca.z,
                x = x, y = y, z = profundidadeTopo
            )
            val recalqueBase = w(
                q = forca.magnitude,
                e = camada.moduloDeYoung, v = camada.poisson,
                c = forca.z,
                x = x, y = y, z = profundidadeBase
            )
            val recalqueTotal = recalqueTopo - recalqueBase
            max(recalqueTotal, 0.0)
        }
    return recalque
}

private fun w(q: Double, e: Double, v: Double, c: Double, x: Double, y: Double, z: Double): Double {
    val r = sqrt(x * x + y * y)
    val r1 = sqrt(r * r + (z - c).pow(2))
    val r2 = sqrt(r * r + (z + c).pow(2))
    val p1 = q * (1.0 + v) / (8.0 * PI * e * (1.0 - v))
    val p2 = (3.0 - 4.0 * v) / r1
    val p3 = (8.0 * (1.0 - v).pow(2) - (3.0 - 4.0 * v)) / r2
    val p4 = (z - c).pow(2) / r1.pow(3)
    val p5 = ((3.0 - 4.0 * v) * (z + c).pow(2) - 2.0 * c * z) / r2.pow(3)
    val p6 = (6.0 * c * z * (z + c).pow(2)) / r2.pow(5)
    return p1 * (p2 + p3 + p4 + p5 + p6)
}