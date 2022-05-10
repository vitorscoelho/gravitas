package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math

import kotlin.math.absoluteValue

fun errosAceitaveis(erro: DoubleArray, delta: DoubleArray): Boolean {
    var todosErrosAceitaveis = true
    erro.forEachIndexed { indexErro, valorErro ->
        val erroAceitavel = valorErro.absoluteValue < delta[indexErro]
        if (!erroAceitavel) {
            todosErrosAceitaveis = false
            return@forEachIndexed
        }
    }
    return todosErrosAceitaveis
}