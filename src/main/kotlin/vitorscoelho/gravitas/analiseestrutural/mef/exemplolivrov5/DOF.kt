package vitorscoelho.gravitas.analiseestrutural.mef.exemplolivrov5

enum class DOF {
    UX, UY, UZ, RX, RY, RZ;

    val indiceLocal = this.ordinal
    
    fun indiceLocalElemento(indiceLocalNo:Int) = 6 * indiceLocalNo + this.indiceLocal
}

/**
 * Lista com os [DOF] ativos.
 * A ordem da lista sempre respeitará a ordem UX, UY, UZ, RX, RY, RZ
 */
class DOFsAtivos(
    ux: Boolean, uy: Boolean, uz: Boolean,
    rx: Boolean, ry: Boolean, rz: Boolean,
) : List<DOF> by criarImplementacaoDeList(ux = ux, uy = uy, uz = uz, rx = rx, ry = ry, rz = rz) {
    companion object {
        private fun criarImplementacaoDeList(
            ux: Boolean, uy: Boolean, uz: Boolean,
            rx: Boolean, ry: Boolean, rz: Boolean,
        ): List<DOF> {
            val lista = listOf(ux, uy, uz, rx, ry, rz)
            return DOF.values().filterIndexed { index, _ -> lista[index] }
        }

        fun portico3d() = DOFsAtivos(
            ux = true, uy = true, uz = true,
            rx = true, ry = true, rz = true,
        )

        fun portico2d() = DOFsAtivos(
            ux = true, uy = false, uz = true,
            rx = false, ry = true, rz = false,
        )

        fun grelha() = DOFsAtivos(
            ux = false, uy = false, uz = true,
            rx = true, ry = true, rz = false,
        )

        fun trelica3d() = DOFsAtivos(
            ux = true, uy = true, uz = true,
            rx = false, ry = false, rz = false,
        )

        fun trelica2d() = DOFsAtivos(
            ux = true, uy = false, uz = true,
            rx = false, ry = false, rz = false,
        )
    }
}