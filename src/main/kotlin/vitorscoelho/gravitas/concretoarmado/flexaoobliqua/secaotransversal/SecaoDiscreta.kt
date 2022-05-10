package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.secaotransversal

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.DeformadaFlexaoReta
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.EsforcosFlexaoReta
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.FuncaoTensaoDeformacao
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.Vetor2D

class SecaoDiscreta(val posicao: Vetor2D, val area: Double) {
    fun esforcosResistentes(
        funcaoTensaoDeformacao: (deformacao: Double) -> Double,
        deformada: DeformadaFlexaoReta,
    ): EsforcosFlexaoReta {
        val forca = area * funcaoTensaoDeformacao(deformada.deformacao(y = posicao.y))
        val momento = forca * posicao.y
        return EsforcosFlexaoReta(normal = forca, momento = momento)
    }

    fun esforcosResistentes(
        funcaoTensaoDeformacao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
    ): EsforcosFlexaoReta {
        return esforcosResistentes(
            funcaoTensaoDeformacao = { deformacao -> funcaoTensaoDeformacao.tensao(deformacao = deformacao) },
            deformada = deformada
        )
    }
}
