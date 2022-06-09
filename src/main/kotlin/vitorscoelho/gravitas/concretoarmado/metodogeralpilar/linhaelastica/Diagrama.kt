package vitorscoelho.gravitas.concretoarmado.metodogeralpilar.linhaelastica

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math.interpolarY
import java.util.TreeSet
import kotlin.Comparator
import kotlin.math.abs

fun diagramaSap(x: List<Double>, valores: List<Double>, toleranciaX: Double): Diagrama {
    return Diagrama(
        pontos = pontosSAP(x = x, valores = valores),
        toleranciaX = toleranciaX,
    )
}

/**
 * ATENÇÃO: os valores devem estar ordenados em relação ao [x]
 */
fun pontosSAP(x: List<Double>, valores: List<Double>): List<PontoDiagrama> {
    require(x.size == valores.size)
    require(x.size > 1)
    val pontos = mutableListOf<PontoDiagrama>()
    var index = 0
    while (index <= x.lastIndex) {
        val xAtual = x[index]
        if (index == x.lastIndex) {
            pontos += PontoDiagrama.criar(x = xAtual, valor = valores[index])
            break
        }
        val xProximo = x[index + 1]
        if (xAtual != xProximo) {
            pontos += PontoDiagrama.criar(x = xAtual, valor = valores[index])
            index++
        } else {
            pontos += PontoDiagrama.criar(
                x = xAtual,
                valorAEsquerda = valores[index],
                valorAdireita = valores[index + 1]
            )
            index += 2
        }
    }
    return pontos
}

class Diagrama(pontos: List<PontoDiagrama>, val toleranciaX: Double) {
    val comparator = Comparator { ponto1: PontoDiagrama, ponto2: PontoDiagrama ->
        if (abs(ponto1.x - ponto2.x) < toleranciaX) {
            0
        } else {
            ponto1.x.compareTo(ponto2.x)
        }
    }
    private val set = TreeSet(comparator).apply { this.addAll(pontos) }
    val pontos: Set<PontoDiagrama> get() = set

    init {
        require(pontos.size == set.size) { "Não podem existir mais de um ponto com a mesma abscissa" }
        require(toleranciaX > 0.0) { "toleranciaX deve ser maior que 0" }
        require(pontos.isNotEmpty()) { "pontos tem que ter pelo menos um elemento" }
    }

    fun valor(x: Double): PontoDiagrama {
        val (pontoAEsquerda, pontoADireita) = pontoAEsquerdaPontoADireita(x = x)
        if (pontoAEsquerda == null) {
            return PontoDiagrama.criar(x = x, valor = pontoADireita!!.valorAEsquerda)
        }
        if (pontoADireita == null) {
            return PontoDiagrama.criar(x = x, valor = pontoAEsquerda.valorAdireita)
        }
        if (pontoAEsquerda == pontoADireita) return pontoAEsquerda
        val valor = interpolarY(
            x = x,
            x1 = pontoAEsquerda.x, y1 = pontoAEsquerda.valorAdireita,
            x2 = pontoADireita.x, y2 = pontoADireita.valorAEsquerda
        )
        return PontoDiagrama.criar(x = x, valor = valor)
    }

    private fun ponto(x: Double): PontoDiagrama? {
        val pontoFalso = pontoFalso(x = x)
        val contem = set.contains(pontoFalso)
        return if (contem) {
            set.ceiling(pontoFalso)
        } else {
            null
        }
    }

    /*
    Comparando valores de abscissas x:
    lower - retorna o maior elemento que é menor que o elemento informado
    floor - retorna o maior elemento que é menor ou igual ao elemento informado
    higher - retorna o menor elemento que é maior que o elemento informado
    ceiling - retorna o menor elemento que é maior ou igual ao elemento informado
     */
    /**
     * Retorna o ponto à esquerda e o ponto à direita de [x]
     * Se existir um ponto no diagrama com abscissa igual a [x], este ponto é considerado à direita e à esquerda
     */
    private fun pontoAEsquerdaPontoADireita(x: Double): Pair<PontoDiagrama?, PontoDiagrama?> {
        val pontoFalso = pontoFalso(x = x)
        val pontoAEsquerda = set.floor(pontoFalso)
        val pontoADireita = set.ceiling(pontoFalso)
        return Pair(pontoAEsquerda, pontoADireita)
    }

    override fun toString(): String = set.toString()

    companion object {
        private fun pontoFalso(x: Double) = PontoDiagrama.criar(x = x, valor = 0.0)
    }
}