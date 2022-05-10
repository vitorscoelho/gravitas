package vitorscoelho.gravitas.concretoarmado.flexaoobliqua

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Representa um vetor de duas dimensões
 * @property x Abscissa
 * @property y Ordenada
 * */
class Vetor2D(val x: Double, val y: Double) {
    /**Norma (módulo)*/
    val norma: Double by lazy { sqrt(x * x + y * y) }

    /**
     * Retorna uma instância de um vetor com as coordenadas com os valores:
     * * x = this.x + deltaX
     * * y = this.y + deltaY
     * @param deltaX a variação aplicada à abscissa
     * @param deltaY a variação aplicada à ordenada
     */
    fun transladar(deltaX: Double = 0.0, deltaY: Double = 0.0) = Vetor2D(x = x + deltaX, y = y + deltaY)

    /**
     * Retorna uma instância a partir de uma rotação efetuada em **this**
     * @param pivo ponto que será o eixo da rotação. Ou seja, é o ponto que não tem suas coordenadas alteradas com a rotação
     * @param rotacao ângulo de rotação, em radianos. A rotação é positiva no sentido anti-horário
     */
    fun rotacionar(pivo: Vetor2D, rotacao: Double): Vetor2D {
        val senoTeta = sin(rotacao)
        val cossenoTeta = cos(rotacao)
        return rotacionar(
            xOriginal = x, yOriginal = y,
            xPivo = pivo.x, yPivo = pivo.y,
            senoTeta = senoTeta, cossenoTeta = cossenoTeta
        )
    }

    fun rotacionar(pivo: Vetor2D, senoTeta: Double, cossenoTeta: Double): Vetor2D {
        return rotacionar(
            xOriginal = x, yOriginal = y,
            xPivo = pivo.x, yPivo = pivo.y,
            senoTeta = senoTeta, cossenoTeta = cossenoTeta
        )
    }

    /**
     * Transforma o vetor para um novo sistema de coordenadas
     * @param novaOrigem posição da nova origem do sistema de coordenadas em relação ao sistema original
     * @param rotacaoNovaOrigem  ângulo de rotação, em radianos, entre o novo sistema de coordenadas e o original.
     * A rotação é positiva no sentido anti-horário. A orientação é partindo do sistema original para o novo
     * @return uma nova instância de [Vetor2D] que representa **this** no novo sistema de coordenadas
     */
    fun mudarSistemaDeCoordenadas(novaOrigem: Vetor2D, rotacaoNovaOrigem: Double): Vetor2D =
        (this - novaOrigem).rotacionar(pivo = ZERO, rotacao = -rotacaoNovaOrigem)

    operator fun plus(outroVetor: Vetor2D) = transladar(deltaX = outroVetor.x, deltaY = outroVetor.y)
    operator fun minus(outroVetor: Vetor2D) = transladar(deltaX = -outroVetor.x, deltaY = -outroVetor.y)

    override fun toString(): String = "Vetor2D [$x, $y]"
    fun distancia(x: Double, y: Double): Double {
        val dx = this.x - x
        val dy = this.y - y
        return sqrt(dx * dx + dy * dy)
    }

    companion object {
        /**Instância de [Vetor2D] com x=0 e y=0*/
        val ZERO: Vetor2D by lazy { Vetor2D(x = 0.0, y = 0.0) }

        /**
         * Retorna uma instância a partir de uma rotação efetuada em um vetor com as coordenadas ([xOriginal], [yOriginal])
         * em torno de um ponto (chamado de pivo) com as coordenadas ([xPivo], [yPivo])
         * @param xOriginal abcissa do ponto antes da rotação
         * @param yOriginal ordenada do ponto antes da rotação
         * @param xPivo abscissa do pivo
         * @param yPivo ordenada do pivo
         * @param senoTeta seno do ângulo de rotação
         * @param cossenoTeta cosseno do ângulo de rotação
         */
        fun rotacionar(
            xOriginal: Double, yOriginal: Double,
            xPivo: Double, yPivo: Double,
            senoTeta: Double, cossenoTeta: Double
        ): Vetor2D {
            val deltaX = xOriginal - xPivo
            val deltaY = yOriginal - yPivo
            val x = xPivo + deltaX * cossenoTeta - deltaY * senoTeta
            val y = yPivo + deltaX * senoTeta + deltaY * cossenoTeta
            return Vetor2D(x = x, y = y)
        }

        fun medio(v1: Vetor2D, v2: Vetor2D) = Vetor2D(x = (v1.x + v2.x) / 2.0, y = (v1.y + v2.y) / 2.0)
    }
}

/**
 * Retorna uma lista com novos vetores que representam os vetores da lista original rotacionados a partir de um pivô.
 * Em uma lista, é mais efetivo utilizar esta função ao invés da função do próprio objeto, pois os valores de seno
 * e cosseno serão calculados apenas uma vez
 * @param pivo ponto que será o eixo da rotação. Ou seja, é o ponto que não tem suas coordenadas alteradas com a rotação
 * @param rotacao ângulo de rotação, em radianos. A rotação é positiva no sentido anti-horário
 */
fun List<Vetor2D>.rotacionar(pivo: Vetor2D, rotacao: Double): List<Vetor2D> {
    val senoTeta = sin(rotacao)
    val cossenoTeta = cos(rotacao)
    return this.map {
        Vetor2D.rotacionar(
            xOriginal = it.x, yOriginal = it.y,
            xPivo = pivo.x, yPivo = pivo.y,
            senoTeta = senoTeta, cossenoTeta = cossenoTeta
        )
    }
}

/**
 * Retorna uma lista com novos vetores que representam os vetores da lista original transformados para um novo sistema
 * de coordenadas.
 * Em uma lista, é mais efetivo utilizar esta função ao invés da função do próprio objeto, pois os valores de seno
 * e cosseno serão calculados apenas uma vez
 * @param novaOrigem posição da nova origem do sistema de coordenadas em relação ao sistema original
 * @param rotacaoNovaOrigem  ângulo de rotação, em radianos, entre o novo sistema de coordenadas e o original.
 * A rotação é positiva no sentido anti-horário. A orientação é partindo do sistema original para o novo
 */
fun List<Vetor2D>.mudarSistemaDeCoordenadas(novaOrigem: Vetor2D, rotacaoNovaOrigem: Double): List<Vetor2D> {
    val senoTeta = sin(-rotacaoNovaOrigem)
    val cossenoTeta = cos(-rotacaoNovaOrigem)
    return this.map {
        Vetor2D.rotacionar(
            xOriginal = it.x - novaOrigem.x, yOriginal = it.y - novaOrigem.y,
            xPivo = 0.0, yPivo = 0.0,
            senoTeta = senoTeta, cossenoTeta = cossenoTeta
        )
    }
}