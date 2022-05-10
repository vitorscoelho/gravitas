package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5

interface MatrizDeRigidez {
    fun valor(indiceLocalDOFEfeito: Int, indiceLocalDOFCausa: Int): Double
    fun valor(indiceLocalNoEfeito: Int, dofEfeito: DOF, indiceLocalNoCausa: Int, dofCausa: DOF): Double {
        //2 * matrizDeIncidencia[index][2], 2 * matrizDeIncidencia[index][2] + 1,
        val indiceDOFEfeito = 6 * indiceLocalNoEfeito + dofEfeito.indiceLocal
        val indiceDOFCausa = 6 * indiceLocalNoCausa + dofCausa.indiceLocal
        return valor(indiceLocalDOFEfeito = indiceDOFEfeito, indiceLocalDOFCausa = indiceDOFCausa)
    }

//    fun valor(noEfeito: No2D, dofEfeito: DOF, noCausa: No2D, dofCausa: DOF): Double
}