package vitorscoelho.gravitas.utils

inline fun <T> Iterable<T>.sumOfIndexed(selector: (index: Int, element: T) -> Double): Double {
    var somaParcial = 0.0
    forEachIndexed { index, element -> somaParcial += selector(index, element) }
    return somaParcial
}