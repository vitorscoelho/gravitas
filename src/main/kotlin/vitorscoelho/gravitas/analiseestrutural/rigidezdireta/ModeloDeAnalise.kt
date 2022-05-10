package vitorscoelho.gravitas.analiseestrutural.rigidezdireta

interface ModeloDeAnalise {
    fun matrizDeRigidezLocal(barra: Barra): MatrizQuadradaEsparsaSimetrica
}

class PorticoPlano : ModeloDeAnalise {
    override fun matrizDeRigidezLocal(barra: Barra): MatrizQuadradaEsparsaSimetrica {
        val axial = barra.submatrizRigidezLocalAxial()
        val flexaoXY = barra.submatrizRigidezLocalFlexaoXY()
        return MatrizQuadradaEsparsaSimetrica
            .build(size = 6, elemQtd = 13) {
                this[0, 0] = axial[0, 0]
                this[0, 3] = axial[0, 1]
                this[1, 1] = flexaoXY[0, 0]
                this[1, 2] = flexaoXY[0, 1]
                this[1, 4] = flexaoXY[0, 2]
                this[1, 5] = flexaoXY[0, 3]
                this[2, 2] = flexaoXY[1, 1]
                this[2, 4] = flexaoXY[1, 2]
                this[2, 5] = flexaoXY[1, 3]
                this[3, 3] = axial[1, 1]
                this[4, 4] = flexaoXY[2, 2]
                this[4, 5] = flexaoXY[2, 3]
                this[5, 5] = flexaoXY[3, 3]
            }
    }
}

class TrelicaPlana : ModeloDeAnalise {
    override fun matrizDeRigidezLocal(barra: Barra): MatrizQuadradaEsparsaSimetrica {
        val axial = barra.submatrizRigidezLocalAxial()
        listOf<Int>().zipWithNext()
        return MatrizQuadradaEsparsaSimetrica
            .build(size = 4, elemQtd = 3) {
                this[0, 0] = axial[0, 0]
                this[0, 2] = axial[0, 1]
                this[2, 2] = axial[1, 1]
            }
    }
}

class Grelha : ModeloDeAnalise {
    override fun matrizDeRigidezLocal(barra: Barra): MatrizQuadradaEsparsaSimetrica {
        val torcao = barra.submatrizRigidezLocalTorcao()
        val flexao = barra.submatrizRigidezLocalFlexaoXZ()
        return MatrizQuadradaEsparsaSimetrica
            .build(size = 6, elemQtd = 13) {
                this[0, 0] = torcao[0, 0]
                this[0, 3] = torcao[0, 1]
                this[1, 1] = flexao[1, 1]
                this[1, 2] = flexao[1, 0]
                this[1, 4] = flexao[1, 3]
                this[1, 5] = flexao[1, 2]
                this[2, 2] = flexao[0, 0]
                this[2, 4] = flexao[0, 3]
                this[2, 5] = flexao[0, 2]
                this[3, 3] = torcao[1, 1]
                this[4, 4] = flexao[3, 3]
                this[4, 5] = flexao[3, 2]
                this[5, 5] = flexao[2, 2]
            }
    }
}

class TrelicaEspacial : ModeloDeAnalise {
    override fun matrizDeRigidezLocal(barra: Barra): MatrizQuadradaEsparsaSimetrica {
        val axial = barra.submatrizRigidezLocalAxial()
        return MatrizQuadradaEsparsaSimetrica
            .build(size = 6, elemQtd = 3) {
                this[0, 0] = axial[0, 0]
                this[0, 3] = axial[1, 0]
                this[3, 3] = axial[1, 1]
            }
    }
}

class PorticoEspacial : ModeloDeAnalise {
    override fun matrizDeRigidezLocal(barra: Barra): MatrizQuadradaEsparsaSimetrica {
        val axial = barra.submatrizRigidezLocalAxial()
        val flexaoXY = barra.submatrizRigidezLocalFlexaoXY()
        val flexaoXZ = barra.submatrizRigidezLocalFlexaoXZ()
        val torcao = barra.submatrizRigidezLocalTorcao()
        return MatrizQuadradaEsparsaSimetrica
            .build(size = 12, elemQtd = 26) {
                this[0, 0] = axial[0, 0]
                this[1, 1] = flexaoXY[0, 0]
                this[2, 2] = flexaoXZ[0, 0]
                this[3, 3] = torcao[0, 0]
                this[4, 4] = flexaoXZ[1, 1]
                this[5, 5] = flexaoXY[1, 1]
                this[6, 6] = axial[1, 1]
                this[7, 7] = flexaoXY[2, 2]
                this[8, 8] = flexaoXZ[2, 2]
                this[9, 9] = torcao[1, 1]
                this[10, 10] = flexaoXZ[3, 3]
                this[11, 11] = flexaoXY[3, 3]
                this[0, 6] = axial[0, 1]
                this[1, 5] = flexaoXY[0, 1]
                this[1, 7] = flexaoXY[0, 2]
                this[1, 11] = flexaoXY[0, 3]
                this[2, 4] = flexaoXZ[0, 1]
                this[2, 8] = flexaoXZ[0, 2]
                this[2, 10] = flexaoXZ[0, 3]
                this[3, 9] = torcao[0, 1]
                this[4, 8] = flexaoXZ[1, 2]
                this[4, 10] = flexaoXZ[1, 3]
                this[5, 7] = flexaoXY[1, 2]
                this[5, 11] = flexaoXY[1, 3]
                this[7, 11] = flexaoXY[2, 3]
                this[8, 10] = flexaoXZ[2, 3]
            }
    }
}