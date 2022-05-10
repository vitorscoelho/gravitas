package vitorscoelho.gravitas.analiseestrutural.rigidezdireta.lesm

class Coordinates(val x: Double, val y: Double, val z: Double)

//[dx dy dz rx ry rz]: -1 = fictitious, 0 = free, 1 = fixed, 2 = spring
class BoundaryCondition(
    val dx: BoundaryConditionType,
    val dy: BoundaryConditionType,
    val dz: BoundaryConditionType,
    val rx: BoundaryConditionType,
    val ry: BoundaryConditionType,
    val rz: BoundaryConditionType,
)

class NodalLoad(
    val fx: Double, val fy: Double, val fz: Double, val mx: Double, val my: Double, val mz: Double
)

class Displacement(
    val dx: Double, val dy: Double, val dz: Double, val rx: Double, val ry: Double, val rz: Double
)

class SpringStiff(
    val kx: Double, val ky: Double, val kz: Double, val krx: Double, val kry: Double, val krz: Double
)