package vitorscoelho.gravitas.analiseestrutural.rigidezdireta.lesm

/**
 * Defines material objects in the LESM (Linear Elements
 * Structure Model) program.
 *
 * All materials in LESM are considered to have linear elastic behavior.
 * In addition, homogeneous and isotropic properties are also considered,
 * that is, all materials have the same properties at every point and in
 * all directions.
 * @author Luiz Fernando Martha, Rafael Lopez Rangel and Pedro Cortez Lopes
 * @property elasticity elasticity modulus
 * @property poisson poisson ratio
 * @property shear shear modulus
 * @property thermExp thermal expansion coefficient
 */

class Material(
    val elasticity: Double,
    val poisson: Double,
    val shear: Double,
    val thermExp: Double,
)