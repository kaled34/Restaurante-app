package com.example.viagourmet.Presentacion.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.viagourmet.domain.model.Categoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaChip(
    categoria: Categoria,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(categoria.nombre) },
        modifier = modifier
    )
}