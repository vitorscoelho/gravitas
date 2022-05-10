package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.secaotransversal

import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.*
import vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math.simpson
import kotlin.math.max
import kotlin.math.min

interface SecaoTransversal {
    /**
     * Retorna o esforço resistente em relação à ordenada y=0
     */
    fun esforcosResistentes(
        funcaoTensaoDeformacao: FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
    ): EsforcosFlexaoReta

    /**
     * Retorna o esforço resistente em relação à ordenada y=0
     */
    fun esforcosResistentes(
        funcaoTensaoDeformacao: FuncaoTensaoDeformacao,
        armaduras: List<SecaoDiscreta>,
        funcaoTensaoDeformacaoArmaduras: (armadura: SecaoDiscreta) -> FuncaoTensaoDeformacao,
        deformada: DeformadaFlexaoReta,
    ): EsforcosFlexaoReta {
        //TODO verificar se todas as armaduras estão dentro da seção. Caso contrário, lançar exception

        val esforcoSecaoBruta = esforcosResistentes(
            funcaoTensaoDeformacao = funcaoTensaoDeformacao,
            deformada = deformada
        )
        val esforcoArmaduras: EsforcosFlexaoReta = armaduras
            .map { armadura ->
                armadura.esforcosResistentes(
                    funcaoTensaoDeformacao = { deformacao ->
                        val tensaoArmadura = funcaoTensaoDeformacaoArmaduras(armadura).tensao(deformacao = deformacao)
                        val tensaoSecaoBruta = funcaoTensaoDeformacao.tensao(deformacao = deformacao)
                        tensaoArmadura - tensaoSecaoBruta
                    },
                    deformada = deformada
                )
            }.reduce { esforcoTotal, esforcoArmadura ->
                esforcoTotal + esforcoArmadura
            }
        return esforcoSecaoBruta + esforcoArmaduras
    }
}
