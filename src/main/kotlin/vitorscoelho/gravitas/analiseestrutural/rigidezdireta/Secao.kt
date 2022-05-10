package vitorscoelho.gravitas.analiseestrutural.rigidezdireta

interface Secao {
    /**area relative to local x-axis (full area)*/
    val area: Double

    /**area relative to local y-axis (effective shear area)*/
    val shearAreaY: Double

    /**area relative to local z-axis (effective shear area)*/
    val shearAreaZ: Double

    /**moment of inertia relative to local x-axis (torsion inertia)*/
    val inertiaX: Double

    /**moment of inertia relative to local y-axis (bending inertia)*/
    val inertiaY: Double

    /**moment of inertia relative to local z-axis (bending inertia)*/
    val inertiaZ: Double

    /**height relative to local y-axis*/
    val heightY: Double

    /**relative to local z-axis*/
    val heightZ: Double

    /**elasticity modulus*/
    val elasticity: Double

    /**poisson ratio*/
    val poisson: Double

    /**shear modulus*/
    val shear: Double

    /**thermal expansion coefficient*/
    val thermExp: Double
}