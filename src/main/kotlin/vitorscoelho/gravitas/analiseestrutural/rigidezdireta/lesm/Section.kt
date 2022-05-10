package vitorscoelho.gravitas.analiseestrutural.rigidezdireta.lesm

/**
 * Defines cross-section objects in the LESM (Linear
 * Elements Structure Model) program.
 *
 * All cross-sections in LESM are considered to be of a generic type,
 * which means that their shapes are not specified, only their geometric
 * properties are provided, such as area, moment of inertia and height.
 * @author Luiz Fernando Martha, Rafael Lopez Rangel and Pedro Cortez Lopes
 * @property area_x area relative to local x-axis (full area)
 * @property area_y area relative to local y-axis (effective shear area)
 * @property area_z area relative to local z-axis (effective shear area)
 * @property inertia_x moment of inertia relative to local x-axis (torsion inertia)
 * @property inertia_y moment of inertia relative to local y-axis (bending inertia)
 * @property inertia_z moment of inertia relative to local z-axis (bending inertia)
 * @property height_y height relative to local y-axis
 * @property height_z relative to local z-axis
 */
class Section(
    val area_x: Double,
    val area_y: Double,
    val area_z: Double,
    val inertia_x: Double,
    val inertia_y: Double,
    val inertia_z: Double,
    val height_y: Double,
    val height_z: Double,
)