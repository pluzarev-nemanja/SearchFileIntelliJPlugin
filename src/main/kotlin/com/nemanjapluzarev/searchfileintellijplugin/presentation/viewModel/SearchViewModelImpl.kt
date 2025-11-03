package com.nemanjapluzarev.searchfileintellijplugin.presentation.viewModel

import com.nemanjapluzarev.searchfileintellijplugin.domain.manager.SearchFileManager
import com.nemanjapluzarev.searchfileintellijplugin.domain.model.Occurrence
import com.nemanjapluzarev.searchfileintellijplugin.domain.viewModel.SearchViewModel
import com.nemanjapluzarev.searchfileintellijplugin.presentation.uiState.SearchUiState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.file.Path

class SearchViewModelImpl(
    private val searchFileManager: SearchFileManager
) : SearchViewModel {

    private val mutableUiState: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState.Idle)
    override val uiState: StateFlow<SearchUiState> = mutableUiState.asStateFlow()

    override val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentJob: Job? = null

    override fun startSearch(directory: Path, query: String) {
        currentJob?.cancel()
        currentJob = scope.launch {
            val occurrences = mutableListOf<Occurrence>()
            runCatching {
                mutableUiState.value = SearchUiState.Loading
                searchFileManager.searchForTextOccurrences(query, directory)
            }.mapCatching { flow ->
                flow.collect { occurrence ->
                    occurrences.add(occurrence)
                    mutableUiState.value = SearchUiState.Success(results = occurrences.toList())
                }
            }.onFailure {
                mutableUiState.value = SearchUiState.Error(it.message ?: "Unknown error")
            }
        }
    }

    override fun cancelSearch() {
        currentJob?.cancel()
        mutableUiState.value = SearchUiState.Idle
    }
}