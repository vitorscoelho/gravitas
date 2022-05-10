package vitorscoelho.gravitas.gui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun <T> Lista(
    lista: List<T>,
    onClickElement: (index: Int) -> Unit,
    indicesSelecionados: Set<Int>,
    composable: @Composable (item: T) -> Unit,
) {
    Column {
        lista.forEachIndexed { index, element ->
            val color = if (indicesSelecionados.contains(index)) Color.Red else Color.White
            Box(
                modifier = Modifier
                    .background(color = color)
                    .clickable { onClickElement(index) }
            ) {
                composable(element)
            }
        }
    }
}