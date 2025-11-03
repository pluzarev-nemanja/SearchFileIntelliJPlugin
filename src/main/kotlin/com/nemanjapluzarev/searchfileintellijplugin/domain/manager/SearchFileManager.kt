package com.nemanjapluzarev.searchfileintellijplugin.domain.manager

import com.nemanjapluzarev.searchfileintellijplugin.domain.model.Occurrence
import kotlinx.coroutines.flow.Flow
import java.nio.file.Path

interface SearchFileManager {
    fun searchForTextOccurrences(
        stringToSearch: String,
        directory: Path
    ): Flow<Occurrence>
}