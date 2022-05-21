package vitorscoelho.gravitas.concretoarmado.flexaoobliqua.math

fun interpolarY(x: Double, x1: Double, y1: Double, x2: Double, y2: Double): Double {
    require(x1 != x2)
    if (y1 == y2) return y1
    return (x - x1) * (y2 - y1) / (x2 - x1) + y1
}