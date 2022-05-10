package vitorscoelho.gravitas.gui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class Input<T>(
    val label: String,
    val state: MutableState<String> = mutableStateOf(""),
    val validation: (value: String) -> (Result<T>)
) {
    val result = derivedStateOf { state.value.let { validation(it) } }
}

@Composable
fun <T> InputTextField(input: Input<T>) {
    InputTextField(
        label = input.label,
        state = input.state,
        isError = input.result.value.isFailure,
        errorMessage = input.result.value.exceptionOrNull()?.message ?: ""
    )
}

@Composable
fun InputTextField(label: String, state: MutableState<String>, isError: Boolean, errorMessage: String = "") {
    Column(modifier = Modifier.height(90.dp)) {
        OutlinedTextField(
            value = state.value,
            onValueChange = { state.value = it },
            label = { Text(text = label) },
            singleLine = true,
            isError = isError,
        )
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption
            )
        }
    }
}