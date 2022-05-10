package vitorscoelho.gravitas.gui.componentes

private fun <T> failureIllegal(message: String) = Result.failure<T>(IllegalArgumentException(message))

/**
 * Se isFailure==true, retorna **this**. Caso contrário, retorna o resultado de [op]
 */
fun <T, S> Result<T>.ifSuccess(op: (valor: T) -> (Result<S>)): Result<S> = fold(
    onSuccess = { op(it) },
    onFailure = { failureIllegal(it.message ?: "") }
)

fun <T, S> Result<T>.ifNotNull(op: (valor: T) -> (Result<S?>)): Result<S?> = ifSuccess { valorResult ->
    if (valorResult == null) Result.success(null) else op(valorResult)
}

fun String.anyValue(): Result<String> = Result.success(value = this)
fun String.optional(): Result<String?> = Result.success(value = ifBlank { null })
fun Result<String?>.morethanZero(): Result<Int?> = ifNotNull { valorResult ->
    valorResult!!.toIntOrNull()?.let { Result.success(it) } ?: failureIllegal("Deve ser um número inteiro")
}

fun String.required(): Result<String> =
    if (isNotBlank()) Result.success(this) else failureIllegal("O campo não pode ser vazio")

fun Result<String>.isInt(): Result<Int> = ifSuccess {
    it.toIntOrNull()?.let { intValue -> Result.success(intValue) } ?: failureIllegal("Deve ser um número inteiro")
}

fun String.isInt(): Result<Int> = required().isInt()

fun Result<String>.isDouble(): Result<Double> = ifSuccess {
    it.toDoubleOrNull()?.let { doubleValue -> Result.success(doubleValue) } ?: failureIllegal("Deve ser um número")
}

fun String.isDouble(): Result<Double> = required().isDouble()

fun Result<Int>.moreThan(value: Int): Result<Int> = ifSuccess {
    if (it > value) Result.success(it) else failureIllegal("Deve ser maior do que $value")
}

@JvmName("moreThanZeroInt")
fun Result<Int>.moreThanZero(): Result<Int> = this.moreThan(value = 0)

fun Result<Double>.moreThan(value: Double): Result<Double> = ifSuccess {
    if (it > value) Result.success(it) else failureIllegal("Deve ser maior do que $value")
}

@JvmName("moreThanZeroDouble")
fun Result<Double>.moreThanZero(): Result<Double> = this.moreThan(value = 0.0)

fun Result<Int>.lessThan(value: Int): Result<Int> = ifSuccess {
    if (it < value) Result.success(it) else failureIllegal("Deve ser menor do que $value")
}

fun Result<Double>.lessThan(value: Double): Result<Double> = ifSuccess {
    if (it < value) Result.success(it) else failureIllegal("Deve ser menor do que $value")
}
