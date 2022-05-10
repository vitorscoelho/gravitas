package vitorscoelho.gravitas.concretoarmado.flexaoobliqua

/**
 * Representa uma regra capaz de determinar a deformação em qualquer ponto do plano cartesiano.
 * @property deformacaoZero deformação específica na origem do plano cartesiano (convencionada positiva se for um encurtamento)
 * @property curvatura é a curvatura, em cm⁻¹, da barra em torno do eixo baricêntrico X (convencionada positiva quando o vetor aponta para a "esquerda")
 */
class DeformadaFlexaoReta private constructor(
    private val deformacaoZero: Double, val curvatura: Double
) {
    /**
     * Retorna a deformação específica em uma determinada ordenada. Positiva para encurtamento e negativa para alongamento.
     * @param y a ordenada do ponto que se deseja saber a deformação específica
     */
    fun deformacao(y: Double): Double = deformacaoZero + curvatura * y

    fun y(deformacao: Double): Double {
        if (curvatura == 0.0) throw IllegalArgumentException("Impossível determinar a ordenada para deformação quando a curvatura é igual a zero")
        return (deformacao - deformacaoZero) / curvatura
    }

    override fun toString(): String = "{e0: $deformacaoZero, curvatura: $curvatura}"

    companion object {
        /**
         * Cria uma instância de [DeformadaFlexaoReta] a partir da deformação de uma ordenada qualquer e da curvatura
         * @param y ordenada qualquer com a deformação conhecida
         * @param deformacaoEmY deformação específica na ordenada [y]. Positivo para encurtamento e negativo para alongamento
         * @param curvatura curvatura da seção em torno do eixo baricêntrico X (convencionada positiva quando o vetor aponta para a "esquerda")
         */
        fun criar(y: Double, deformacaoEmY: Double, curvatura: Double): DeformadaFlexaoReta {
            val deformacaoZero = deformacaoEmY - curvatura * y
            return DeformadaFlexaoReta(deformacaoZero = deformacaoZero, curvatura = curvatura)
        }

        fun criar(y1: Double, deformacaoEmY1: Double, y2: Double, deformacaoEmY2: Double): DeformadaFlexaoReta {
            val curvatura = curvatura(
                y1 = y1, deformacaoEmY1 = deformacaoEmY1, y2 = y2, deformacaoEmY2 = deformacaoEmY2
            )
            return criar(y = y1, deformacaoEmY = deformacaoEmY1, curvatura = curvatura)
        }

        fun curvatura(y1: Double, deformacaoEmY1: Double, y2: Double, deformacaoEmY2: Double): Double {
            require(y1 != y2) { "'y1' e 'y2' devem ter valores diferentes. y1=y2=$y1" }
            if (deformacaoEmY1 == deformacaoEmY2) return 0.0
            val yMin: Double
            val yMax: Double
            val deformacaoEmYMin: Double
            val deformacaoEmYMax: Double
            if (y2 > y1) {
                yMin = y1
                yMax = y2
                deformacaoEmYMin = deformacaoEmY1
                deformacaoEmYMax = deformacaoEmY2
            } else {
                yMin = y2
                yMax = y1
                deformacaoEmYMin = deformacaoEmY2
                deformacaoEmYMax = deformacaoEmY1
            }
            return (deformacaoEmYMax - deformacaoEmYMin) / (yMax - yMin)
        }
    }
}