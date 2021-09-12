package vitorscoelho.gravitas.utils

val Double.metros: Double get() = this * 100.0
val Double.mpa: Double get() = this / 10.0
val Double.gpa: Double get() = this * 100.0
val Int.metros: Double get() = this.toDouble().metros
val Int.mpa: Double get() = this.toDouble().mpa
val Int.gpa: Double get() = this.toDouble().gpa