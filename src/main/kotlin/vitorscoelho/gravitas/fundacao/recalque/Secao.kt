package vitorscoelho.gravitas.fundacao.recalque

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

interface Secao {
    val area: Double
    val perimetro: Double

    fun forcasPerimetro(forcasResultantes: Sequence<Forca>): Sequence<Forca>
    fun forcasPonta(forcasResultantes: Sequence<Forca>): Sequence<Forca>

    fun forcasPonta(forcaResultante: Forca): Sequence<Forca> = forcasPonta(sequenceOf(forcaResultante))
}


class SecaoCircular(
    val diametro: Double,
    val divisoesCircunferenciaArea: Int,
    val divisoesRaioArea: Int,
    val divisoesPerimetro: Int
) : Secao {
    init {
        require(diametro > 0.0)
        require(divisoesCircunferenciaArea > 1)
        require(divisoesRaioArea > 1)
        require(divisoesPerimetro > 1)
    }

    val raio: Double get() = 0.5 * diametro
    override val area = PI * diametro * diametro / 4.0
    override val perimetro = PI * diametro

    /**Posições das forças discretizadas no perímetro considerando que o eixo está no 0,0*/
    private val posicaoForcasPerimetro = (1..divisoesPerimetro).map {
        val teta = 2.0 * PI * it / divisoesPerimetro
        Vetor2D(x = raio * cos(teta), y = raio * sin(teta))
    }

    /**Posições das forças discretizadas em área considerando que o eixo está no 0,0*/
    private val posicaoForcasArea = run {
        val teta = PI / divisoesCircunferenciaArea
        val constanteRo = (2.0 * sin(teta) / (3.0 * teta)) * (raio / sqrt(divisoesRaioArea.toDouble()))
        (1..divisoesCircunferenciaArea).flatMap { i/*divisaoCircunferencia*/ ->
            (1..divisoesRaioArea).map { j/*divisaoRaio*/ ->
                val ro = constanteRo * (j * sqrt(j.toDouble()) - (j - 1.0) * sqrt(j - 1.0))
                val beta = PI * (2.0 * i - 1.0) / divisoesCircunferenciaArea
                Vetor2D(x = ro * cos(beta), y = ro * sin(beta))
            }
        }
    }

    override fun forcasPerimetro(forcasResultantes: Sequence<Forca>): Sequence<Forca> =
        forcasDiscretizadas(
            forcasResultantes = forcasResultantes,
            posicoesForcasDiscretizadas = posicaoForcasPerimetro.asSequence(),
            magnitudeForcaDiscretizada = { it.magnitude / divisoesPerimetro }
        )

    override fun forcasPonta(forcasResultantes: Sequence<Forca>): Sequence<Forca> =
        forcasDiscretizadas(
            forcasResultantes = forcasResultantes,
            posicoesForcasDiscretizadas = posicaoForcasArea.asSequence(),
            magnitudeForcaDiscretizada = { it.magnitude / (divisoesCircunferenciaArea * divisoesRaioArea) }
        )

}

fun forcasDiscretizadas(
    forcasResultantes: Sequence<Forca>,
    posicoesForcasDiscretizadas: Sequence<Vetor2D>,
    magnitudeForcaDiscretizada: (forcaResultante: Forca) -> Double
): Sequence<Forca> =
    forcasResultantes
        .flatMap { forcaResultante ->
            val magnitude = magnitudeForcaDiscretizada(forcaResultante)
            posicoesForcasDiscretizadas.asSequence().map { posicaoForcaDiscretizada ->
                Forca(
                    magnitude = magnitude,
                    posicao = Vetor3D(
                        x = posicaoForcaDiscretizada.x + forcaResultante.x,
                        y = posicaoForcaDiscretizada.y + forcaResultante.y,
                        z = forcaResultante.z
                    )
                )
            }
        }