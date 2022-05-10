package vitorscoelho.gravitas.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import vitorscoelho.gravitas.fundacao.recalque.Camada
import vitorscoelho.gravitas.gui.componentes.*

private class CamadaModel() {
    val profundidade = Input(
        label = "Profundidade (m)",
        validation = { it.isDouble().moreThanZero() }
    )
    val young = Input(
        label = "Módulo de deformação (MPa)",
        validation = { it.isDouble().moreThanZero() }
    )
    val poisson = Input(
        label = "Coeficiente de poisson",
        validation = { it.isDouble().moreThanZero().lessThan(1.0) }
    )

    val isValid = derivedStateOf {
        sequenceOf(profundidade.result, young.result, poisson.result).any { it.value.isFailure }.not()
    }

    fun toCamada(): Camada {
        return Camada(
            profundidade = profundidade.result.value.getOrNull()!!,
            moduloDeYoung = young.result.value.getOrNull()!!,
            poisson = poisson.result.value.getOrNull()!!,
        )
    }
}

@Composable
fun SecaoCamadas() {
    val modelCamada = remember { CamadaModel() }
    val listaCamadas = remember { mutableStateListOf<Camada>() }
    val indicesSelecionados = remember { mutableStateOf(setOf<Int>()) }
    Column {

        InputTextField(modelCamada.profundidade)
        InputTextField(modelCamada.young)
        InputTextField(modelCamada.poisson)
        Row {
            Button(
                onClick = {
                    val camada = modelCamada.toCamada()
                    println(camada)
                    listaCamadas.add(camada)
                    listaCamadas.sortBy { it.profundidade }
                },
                enabled = modelCamada.isValid.value
            ) { Text("Adicionar") }
            Button(
                onClick = {
                    indicesSelecionados.value.forEach {
                        if (it <= listaCamadas.lastIndex) {
                            listaCamadas.removeAt(it)
                            indicesSelecionados.value = emptySet()
                        }
                    }
                },
                enabled = listaCamadas.isNotEmpty() && indicesSelecionados.value.isNotEmpty()
            ) { Text("Remover") }
        }
        Lista(
            lista = listaCamadas,
            onClickElement = { index ->
                indicesSelecionados.value = setOf(index)
                val element = listaCamadas[index]
                modelCamada.profundidade.state.value = element.profundidade.toString()
                modelCamada.young.state.value = element.moduloDeYoung.toString()
                modelCamada.poisson.state.value = element.poisson.toString()
            },
            indicesSelecionados = indicesSelecionados.value
        ) {
            Text(it.toString())
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = 300.dp, height = 600.dp)
    ) {
        Row {
            SecaoCamadas()
        }
    }
}