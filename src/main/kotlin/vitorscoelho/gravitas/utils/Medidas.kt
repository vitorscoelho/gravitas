package vitorscoelho.gravitas.utils

inline val Double.cm: Double get() = this
inline val Double.metros: Double get() = this * 100.0
inline val Double.mpa: Double get() = this / 10.0
inline val Double.gpa: Double get() = this * 100.0
inline val Int.cm: Double get() = this.toDouble()
inline val Int.metros: Double get() = this.toDouble().metros
inline val Int.mpa: Double get() = this.toDouble().mpa
inline val Int.gpa: Double get() = this.toDouble().gpa

inline val Double.kNcm2: Double get() = this
inline val Double.kNm2: Double get() = this * 10_000.0
inline val Double.tfm2: Double get() = this * 100_000.0
inline val Int.kNcm2: Double get() = this.toDouble()
inline val Int.kNm2: Double get() = this.toDouble().kNm2
inline val Int.tfm2: Double get() = this.toDouble().tfm2

inline val Double.kNcm: Double get() = this
inline val Double.kNm: Double get() = this * 100.0
inline val Double.tfm: Double get() = this * 1_000.0
inline val Int.kNcm: Double get() = this.toDouble()
inline val Int.kNm: Double get() = this.toDouble().kNm
inline val Int.tfm: Double get() = this.toDouble().tfm

inline val Double.kN: Double get() = this
inline val Double.tf: Double get() = this * 10.0
inline val Int.kN: Double get() = this.toDouble()
inline val Int.tf: Double get() = this.toDouble().tf