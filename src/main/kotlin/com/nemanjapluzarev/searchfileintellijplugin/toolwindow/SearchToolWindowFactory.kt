package com.nemanjapluzarev.searchfileintellijplugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.nemanjapluzarev.searchfileintellijplugin.data.manager.SearchFileManagerImpl
import com.nemanjapluzarev.searchfileintellijplugin.domain.viewModel.SearchViewModel
import com.nemanjapluzarev.searchfileintellijplugin.presentation.viewModel.SearchViewModelImpl

class SearchToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val searchViewModel: SearchViewModel = SearchViewModelImpl(
            searchFileManager = SearchFileManagerImpl()
        )
        toolWindow.contentManager.addContent(
            ContentFactory.getInstance().createContent(
                SearchToolWindow(project, searchViewModel).rootPanel,
                null,
                false
            )
        )
    }
}