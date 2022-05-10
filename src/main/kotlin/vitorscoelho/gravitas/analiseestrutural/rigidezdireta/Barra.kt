package vitorscoelho.gravitas.analiseestrutural.rigidezdireta

import kotlin.math.pow

class Barra(
    val noInicial: No,
    val noFinal: No,
    val articulacaoInicio: Boolean,
    val articulacaoFinal: Boolean,
    val secao: Secao,
    val timoshenko: Boolean
) {
    val comprimento: Double get() = noFinal.distancia(noInicial)
    fun submatrizRigidezLocalAxial(): MatrizQuadradaEsparsaSimetrica {
        val rigidezAxial = secao.elasticity * secao.area / comprimento
        return MatrizQuadradaEsparsaSimetrica
            .build(size = 2, elemQtd = 3) {
                set(0, 0, rigidezAxial)
                set(0, 1, -rigidezAxial)
                set(1, 1, rigidezAxial)
            }
    }

    fun submatrizRigidezLocalFlexaoXY(): MatrizQuadradaEsparsaSimetrica = submatrizRigidezLocalFlexao(
        shearArea = secao.shearAreaY, inertia = secao.inertiaY,
        timoshenko = timoshenko,
        articulacaoInicio = articulacaoInicio, articulacaoFinal = articulacaoFinal
    )

    fun submatrizRigidezLocalFlexaoXZ(): MatrizQuadradaEsparsaSimetrica = submatrizRigidezLocalFlexao(
        shearArea = secao.shearAreaZ, inertia = secao.inertiaZ,
        timoshenko = timoshenko,
        articulacaoInicio = articulacaoInicio, articulacaoFinal = articulacaoFinal
    )

    fun submatrizRigidezLocalTorcao(): MatrizQuadradaEsparsaSimetrica {
        val constante = secao.shear * secao.inertiaX / comprimento
        return MatrizQuadradaEsparsaSimetrica
            .build(size = 2, elemQtd = 3) {
                set(0, 0, constante)
                set(0, 1, -constante)
                set(1, 1, constante)
            }
    }

    private fun submatrizRigidezLocalFlexao(
        shearArea: Double,
        inertia: Double,
        timoshenko: Boolean,
        articulacaoInicio: Boolean,
        articulacaoFinal: Boolean
    ): MatrizQuadradaEsparsaSimetrica {
        val l = comprimento
        val omega = if (timoshenko) secao.elasticity * inertia / (secao.shear * shearArea * l.pow(2)) else 0.0
        val mi = 1.0 + 12.0 * omega
        val lambda = 1.0 + 3.0 * omega
        val gamma = 1.0 - 6.0 * omega

        if (!articulacaoInicio && !articulacaoFinal) {
            val constante = secao.elasticity * inertia / (mi * l.pow(3))
            return MatrizQuadradaEsparsaSimetrica
                .build(size = 4, elemQtd = 10) {
                    set(0, 0, 12.0 * constante)
                    set(0, 1, 6.0 * l * constante)
                    set(0, 2, -12.0 * constante)
                    set(0, 3, 6.0 * l * constante)
                    set(1, 1, 4.0 * lambda * l.pow(2) * constante)
                    set(1, 2, -6.0 * l * constante)
                    set(1, 3, 2.0 * gamma * l.pow(2) * constante)
                    set(2, 2, 12.0 * constante)
                    set(2, 3, -6.0 * l * constante)
                    set(3, 3, 4.0 * lambda * l.pow(2) * constante)
                }
        }

        if (articulacaoInicio && !articulacaoFinal) {
            val constante = 3.0 * secao.elasticity * inertia / (lambda * comprimento.pow(3))
            return MatrizQuadradaEsparsaSimetrica
                .build(size = 4, elemQtd = 6) {
                    set(0, 0, constante)
                    set(0, 2, -constante)
                    set(0, 3, l * constante)
                    set(2, 2, constante)
                    set(2, 3, -l * constante)
                    set(3, 3, l.pow(2) * constante)
                }
        }

        if (!articulacaoInicio && articulacaoFinal) {
            val constante = 3.0 * secao.elasticity * inertia / (lambda * comprimento.pow(3))
            return MatrizQuadradaEsparsaSimetrica
                .build(size = 4, elemQtd = 6) {
                    set(0, 0, constante)
                    set(0, 1, l * constante)
                    set(0, 2, -constante)
                    set(1, 1, l.pow(2) * constante)
                    set(1, 2, -l * constante)
                    set(2, 2, constante)
                }
        }

        return MatrizQuadradaEsparsaSimetrica.build(size = 4, elemQtd = 0) {}
    }
}
