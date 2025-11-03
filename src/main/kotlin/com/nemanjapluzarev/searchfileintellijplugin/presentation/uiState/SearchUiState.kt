package com.nemanjapluzarev.searchfileintellijplugin.presentation.uiState

import com.nemanjapluzarev.searchfileintellijplugin.domain.model.Occurrence

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object Loading : SearchUiState()
    data class Success(val results: List<Occurrence>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}