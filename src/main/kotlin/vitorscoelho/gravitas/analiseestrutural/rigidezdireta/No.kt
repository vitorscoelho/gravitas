package vitorscoelho.gravitas.analiseestrutural.rigidezdireta

class No(val coordenadas: Coordenadas){
    fun distancia(outro:No) = coordenadas.distancia(outro.coordenadas)
}