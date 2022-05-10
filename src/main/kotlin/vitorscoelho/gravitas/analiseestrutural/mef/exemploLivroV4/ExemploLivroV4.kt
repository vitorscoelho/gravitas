package vitorscoelho.gravitas.analiseestrutural.mef.exemploLivroV4

import org.apache.commons.math3.linear.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.absoluteValue

//Exemplo da página 114 do livro de MEF do Luiz Eloy Vaz
val matrixFormat = RealMatrixFormat(
    "", "", "", "\n", "", ", ",
    DecimalFormat().apply { this.decimalFormatSymbols = DecimalFormatSymbols().apply { decimalSeparator = '.' } }
)

fun RealMatrix.imprimir() = println(matrixFormat.format(this))
operator fun RealMatrix.times(other: RealMatrix): RealMatrix = this.multiply(other)
operator fun RealMatrix.times(scalar: Double): RealMatrix = this.scalarMultiply(scalar)
operator fun Double.times(matrix: RealMatrix): RealMatrix = matrix * this

fun main() {
    val nos = listOf(
        No2D(x = 0.0, y = 0.0),
        No2D(x = 0.0, y = 1.0),
        No2D(x = 2.0, y = 0.0),
        No2D(x = 2.0, y = 1.0),
        No2D(x = 4.0, y = 0.0),
        No2D(x = 4.0, y = 1.0),
    )
    val elementos = run {
        val secao = Secao(espessura = 1.0, moduloDeElasticidade = 20_000.0, poisson = 0.2)
        listOf(
            ElementoCST(no1 = nos[0], no2 = nos[3], no3 = nos[1], secao = secao),
            ElementoCST(no1 = nos[0], no2 = nos[2], no3 = nos[3], secao = secao),
            ElementoCST(no1 = nos[2], no2 = nos[5], no3 = nos[3], secao = secao),
            ElementoCST(no1 = nos[2], no2 = nos[4], no3 = nos[5], secao = secao),
        )
    }
    val restricoes = mapOf(
        elementos[0].nos[0] to listOf(DOF.UX, DOF.UZ),
        elementos[0].nos[2] to listOf(DOF.UX)
    )
    val cargas = mapOf(
        Carga(magnitude = 10.0, dof = DOF.UX) to listOf(elementos[3].nos[1], elementos[3].nos[2])
    )

    val analise = AnaliseTesteElementosCST(
        elementos = elementos, restricoes = restricoes, cargas = cargas,
        dofsAtivos = DOFsAtivos(
            ux = true, uy = false, uz = true,
            rx = false, ry = false, rz = false
        )//TODO testar as outras maneiras
    )

    val resultados = analise.solve()

    //Checagem da resposta
    val deslocamentoZero = Deslocamento(ux = 0.0, uy = 0.0, uz = 0.0, rx = 0.0, ry = 0.0, rz = 0.0)
    val deslocamentoRespostaLivro = mapOf(
        No2D(x = 0.0, y = 0.0) to deslocamentoZero,
        No2D(x = 0.0, y = 1.0) to deslocamentoZero.copy(uz = -0.0002),//Deslocamento(0.0, -0.0002),
        No2D(x = 2.0, y = 0.0) to deslocamentoZero.copy(ux = 0.002),//Deslocamento(0.002, -0.0),
        No2D(x = 2.0, y = 1.0) to deslocamentoZero.copy(ux = 0.002, uz = -0.0002),//Deslocamento(0.002, -0.0002),
        No2D(x = 4.0, y = 0.0) to deslocamentoZero.copy(ux = 0.004),//Deslocamento(0.004, 0.0),
        No2D(x = 4.0, y = 1.0) to deslocamentoZero.copy(ux = 0.004, uz = -0.0002),//Deslocamento(0.004, -0.0002),
    )
    nos.forEachIndexed { index, no ->
        val deslocamentoX = resultados[no]!!.ux
        val deslocamentoZ = resultados[no]!!.uz
        val deslocamentoLivro = deslocamentoRespostaLivro[no]!!
        val diferencaX = deslocamentoX - deslocamentoLivro.ux
        val diferencaZ = deslocamentoZ - deslocamentoLivro.uz
        check(diferencaX.absoluteValue <= 10e-8) {
            "Nó$index X: Programa:$deslocamentoX  Livro:${deslocamentoLivro.ux}"
        }
        check(diferencaZ.absoluteValue <= 10e-8) {
            "Nó$index Z: Programa:$deslocamentoZ  Livro:${deslocamentoLivro.uz}"
        }
    }
}
