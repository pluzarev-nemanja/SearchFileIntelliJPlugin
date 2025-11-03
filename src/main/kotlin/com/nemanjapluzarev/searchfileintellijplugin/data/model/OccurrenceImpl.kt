package com.nemanjapluzarev.searchfileintellijplugin.data.model

import com.nemanjapluzarev.searchfileintellijplugin.domain.model.Occurrence
import java.nio.file.Path

data class OccurrenceImpl(
    override val file: Path,
    override val line: Int,
    override val offset: Int
) : Occurrence
