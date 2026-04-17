package com.example.viagourmet.Presentacion.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CantidadSelector(
    cantidad: Int,
    onCantidadChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minCantidad: Int = 1,
    maxCantidad: Int = 10
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Disminuir
        IconButton(
            onClick = {
                if (cantidad > minCantidad) {
                    onCantidadChange(cantidad - 1)
                }
            },
            enabled = cantidad > minCantidad
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Disminuir cantidad"
            )
        }

        // Cantidad actual
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Aumentar
        IconButton(
            onClick = {
                if (cantidad < maxCantidad) {
                    onCantidadChange(cantidad + 1)
                }
            },
            enabled = cantidad < maxCantidad
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Aumentar cantidad"
            )
        }
    }
}