package vitorscoelho.gravitas.analiseestrutural.rigidezdireta

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.ejml.data.DMatrixSparse
import org.ejml.data.DMatrixSparseCSC

class Coordenadas(val x: Double, val y: Double, val z: Double) {
    private val apacheVector = Vector3D(x, y, z)

    fun distancia(outra: Coordenadas) = apacheVector.distance(outra.apacheVector)

    companion object {
        fun distancia(c1: Coordenadas, c2: Coordenadas) = c1.apacheVector.distance(c2.apacheVector)
    }
}

class MatrizQuadradaEsparsaSimetrica private constructor(private val ejmlMatrix: DMatrixSparse) {
    operator fun get(linha: Int, coluna: Int): Double {
        //TODO substituir 'ejmlMatrix.get' por 'ejmlMatrix.unsafe_get'
        return if (linha > coluna) ejmlMatrix.get(coluna, linha) else ejmlMatrix.get(linha, coluna)
    }

    companion object {
        class Builder constructor(size: Int, arrayLenght: Int) {
            private val ejmlMatrix = DMatrixSparseCSC(size, size, arrayLenght)

            operator fun set(linha: Int, coluna: Int, valor: Double) {
                if (linha > coluna) ejmlMatrix.set(coluna, linha, valor) else ejmlMatrix.set(linha, coluna, valor)
            }

            fun build() = MatrizQuadradaEsparsaSimetrica(ejmlMatrix)
        }

        fun build(size: Int, elemQtd: Int, op: Builder.() -> Unit): MatrizQuadradaEsparsaSimetrica {
            val builder = Builder(size = size, arrayLenght = elemQtd)
            op(builder)
            return builder.build()
        }

        fun build(size: Int, op: Builder.() -> Unit) = build(size = size, elemQtd = 0, op = op)
    }
}