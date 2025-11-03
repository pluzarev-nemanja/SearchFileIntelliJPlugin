package com.nemanjapluzarev.searchfileintellijplugin.domain.viewModel

import com.nemanjapluzarev.searchfileintellijplugin.presentation.uiState.SearchUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path

interface SearchViewModel {
    val uiState: StateFlow<SearchUiState>
    val scope: CoroutineScope
    fun startSearch(directory: Path, query: String)
    fun cancelSearch()
}