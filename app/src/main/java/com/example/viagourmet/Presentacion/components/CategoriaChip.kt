package com.example.viagourmet.Presentacion.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.viagourmet.domain.model.Categoria

// ERROR FIX 4: MenuScreen llama CategoriaChip(nombre = "Todos", seleccionado = ...)
// pero el composable original solo aceptaba CategoriaChip(categoria, isSelected, onClick)
// Se añade una segunda sobrecarga que acepta nombre: String y seleccionado: Boolean

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaChip(
    nombre: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(nombre) },
        modifier = modifier
    )
}