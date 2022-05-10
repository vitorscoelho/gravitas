package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math

import kotlin.math.abs

/**
 * Encontra a raíz da função fornecida utilizando o Método Pégaso.
 * Os valores de [xEsquerda] e [xDireita] devem ser adotados de forma que
 * funcao(xEsquerda) e funcao(xDireita) retornem valores com sinais opostos.
 * Caso esta medida não seja adotada, ainda pode-se chegar à raiz, porém a possibilidade
 * fica menos garantida
 * @param xEsquerda abcissa à esquerda da raiz
 * @param fxEsquerda resultado para funcao(xEsquerda)
 * @param xDireita abcissa à direita da raiz
 * @param fxDireita resultado para funcao(xDireita)
 * @param acuraciaAbsoluta tolerância aceitável para considerar [funcao] igual a zero
 * @param limiteDeIteracoes quantidade máxima de iterações permitidas. Deve ser maior ou igual a 1
 * @return uma abscissa onde [funcao] retorna zero
 * @throws IllegalArgumentException se [xEsquerda] não for menor do que [xDireita]
 * @throws IllegalArgumentException se [limiteDeIteracoes] for menor do que 1
 * @throws IllegalStateException se o número de iterações exceder [limiteDeIteracoes]
 */
fun raizMetodoPegaso(
    xEsquerda: Double,
    fxEsquerda: Double,
    xDireita: Double,
    fxDireita: Double,
    acuraciaAbsoluta: Double,
    limiteDeIteracoes: Int,
    funcao: (x: Double) -> Double
): Double {
    require(limiteDeIteracoes >= 1) { "|limiteDeIteracoes| não pode ser menor do que 1. limiteDeIteracoes=$limiteDeIteracoes" }
    require(xEsquerda < xDireita) { "|xEsquerda| deve ser menor do que |xDireita|. Porém: xEsquerda=$xEsquerda e xDireita=$xDireita" }
    var xEsquerdaVariavel = xEsquerda
    var fxEsquerdaVariavel = fxEsquerda
    var xDireitaVariavel = xDireita
    var fxDireitaVariavel = fxDireita
    var xAproximacaoVariavel: Double
    var fxAproximacaoVariavel: Double
    var nIteracao = 0

    do {
        check(nIteracao <= limiteDeIteracoes) {
            "Não foi possível encontrar a raiz dentro do número de iterações informados. limiteDeIteracoes=$limiteDeIteracoes"
        }

        val delta1 = xDireitaVariavel - xEsquerdaVariavel
        val delta2 = fxDireitaVariavel - fxEsquerdaVariavel
        xAproximacaoVariavel = xDireitaVariavel - fxDireitaVariavel * delta1 / delta2
        fxAproximacaoVariavel = funcao(xAproximacaoVariavel)
        if (fxAproximacaoVariavel * fxDireitaVariavel < 0.0) {
            xEsquerdaVariavel = xDireitaVariavel
            fxEsquerdaVariavel = fxDireitaVariavel
        } else {
            fxEsquerdaVariavel = fxEsquerdaVariavel * fxDireitaVariavel / (fxDireitaVariavel + fxAproximacaoVariavel)
        }
        xDireitaVariavel = xAproximacaoVariavel
        fxDireitaVariavel = fxAproximacaoVariavel

        nIteracao++
    } while (abs(fxAproximacaoVariavel) >= acuraciaAbsoluta)
    return xAproximacaoVariavel
}

/**
 * Encontra a raíz da função fornecida utilizando o Método Pégaso.
 * Os valores de [xEsquerda] e [xDireita] devem ser adotados de forma que
 * funcao(xEsquerda) e funcao(xDireita) retornem valores com sinais opostos.
 * Caso esta medida não seja adotada, ainda pode-se chegar à raiz, porém a possibilidade
 * fica menos garantida
 * @param xEsquerda abcissa à esquerda da raiz
 * @param xDireita abcissa à direita da raiz
 * @param acuraciaAbsoluta tolerância aceitável para considerar [funcao] igual a zero
 * @param limiteDeIteracoes quantidade máxima de iterações permitidas. Deve ser maior ou igual a 1
 * @return uma abscissa onde [funcao] retorna zero
 * @throws IllegalArgumentException se [xEsquerda] não for menor do que [xDireita]
 * @throws IllegalArgumentException se [limiteDeIteracoes] for menor do que 1
 * @throws IllegalStateException se o número de iterações exceder [limiteDeIteracoes]
 */
fun raizMetodoPegaso(
    xEsquerda: Double,
    xDireita: Double,
    acuraciaAbsoluta: Double,
    limiteDeIteracoes: Int,
    funcao: (x: Double) -> Double
): Double = raizMetodoPegaso(
    xEsquerda = xEsquerda, fxEsquerda = funcao(xEsquerda),
    xDireita = xDireita, fxDireita = funcao(xDireita),
    acuraciaAbsoluta = acuraciaAbsoluta,
    limiteDeIteracoes = limiteDeIteracoes,
    funcao = funcao
)

fun raizMetodoSecantes(
    xEsquerda: Double,
    fxEsquerda: Double,
    xDireita: Double,
    fxDireita: Double,
    acuraciaAbsoluta: Double,
    limiteDeIteracoes: Int,
    funcao: (x: Double) -> Double
): Double {
    var xEsquerdaVariavel = xEsquerda
    var xDireitaVariavel = xDireita
    var fxEsquerdaVariavel = fxEsquerda
    var fxDireitaVariavel = fxDireita
    var xAproximacaoVariavel: Double
    var fxAproximacao: Double
    var nIteracao = 1
    do {
        check(nIteracao <= limiteDeIteracoes) {
            "Não foi possível encontrar a raiz dentro do número de iterações informados. limiteDeIteracoes=$limiteDeIteracoes"
        }

        val delta1 = xDireitaVariavel - xEsquerdaVariavel
        val delta2 = fxDireitaVariavel - fxEsquerdaVariavel
        xAproximacaoVariavel = xDireitaVariavel - fxDireitaVariavel * delta1 / delta2
        fxAproximacao = funcao(xAproximacaoVariavel)

        xEsquerdaVariavel = xDireitaVariavel
        fxEsquerdaVariavel = fxDireitaVariavel
        xDireitaVariavel = xAproximacaoVariavel
        fxDireitaVariavel = fxAproximacao

        nIteracao++
    } while (abs(fxAproximacao) >= acuraciaAbsoluta)
    return xAproximacaoVariavel
}

fun raizMetodoSecantes(
    xEsquerda: Double,
    xDireita: Double,
    acuraciaAbsoluta: Double,
    limiteDeIteracoes: Int,
    funcao: (x: Double) -> Double
): Double = raizMetodoSecantes(
    xEsquerda = xEsquerda, fxEsquerda = funcao(xEsquerda),
    xDireita = xDireita, fxDireita = funcao(xDireita),
    acuraciaAbsoluta = acuraciaAbsoluta,
    limiteDeIteracoes = limiteDeIteracoes,
    funcao = funcao
)

fun raizMetodoNewtonRaphson(
    xPrimeiraAproximacao: Double,
    fxPrimeiraAproximacao: Double,
    acuraciaAbsoluta: Double,
    limiteDeIteracoes: Int,
    funcao: (x: Double) -> Double
): Double {
    var xAproximacaoVariavel = xPrimeiraAproximacao
    var fxAproximacaoVariavel = fxPrimeiraAproximacao
    var nIteracao = 0
    do {
        check(nIteracao <= limiteDeIteracoes) {
            "Não foi possível encontrar a raiz dentro do número de iterações informados. limiteDeIteracoes=$limiteDeIteracoes"
        }

        val derivada = derivada(x = xAproximacaoVariavel, fx = funcao)
        xAproximacaoVariavel -= fxAproximacaoVariavel / derivada
        fxAproximacaoVariavel = funcao(xAproximacaoVariavel)

        nIteracao++
    } while (abs(fxAproximacaoVariavel) >= acuraciaAbsoluta)
    return xAproximacaoVariavel
}

fun raizMetodoNewtonRaphson(
    xPrimeiraAproximacao: Double,
    acuraciaAbsoluta: Double,
    limiteDeIteracoes: Int,
    funcao: (x: Double) -> Double
): Double = raizMetodoNewtonRaphson(
    xPrimeiraAproximacao = xPrimeiraAproximacao,
    fxPrimeiraAproximacao = funcao(xPrimeiraAproximacao),
    acuraciaAbsoluta = acuraciaAbsoluta,
    limiteDeIteracoes = limiteDeIteracoes,
    funcao = funcao
)