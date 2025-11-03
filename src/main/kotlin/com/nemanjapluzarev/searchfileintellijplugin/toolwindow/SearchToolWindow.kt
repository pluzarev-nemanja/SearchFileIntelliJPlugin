package com.nemanjapluzarev.searchfileintellijplugin.toolwindow

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.components.JBList
import com.nemanjapluzarev.searchfileintellijplugin.domain.model.Occurrence
import com.nemanjapluzarev.searchfileintellijplugin.domain.viewModel.SearchViewModel
import com.nemanjapluzarev.searchfileintellijplugin.presentation.uiState.SearchUiState
import kotlinx.coroutines.*
import java.awt.Dimension
import java.nio.file.Path
import javax.swing.*

class SearchToolWindow(
    private val project: Project,
    private val searchViewModel: SearchViewModel
) : SimpleToolWindowPanel(true, true) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val rootPanel: JComponent get() = this
    val pathField = JTextField()
    val queryField = JTextField()
    val startButton = JButton("Start search")
    private val resultListModel = DefaultListModel<Occurrence>()
    private val resultList = JBList(resultListModel)

    init {
        initializeUI()
        initializeButtonListener()
        observeUiStates()
        setupResultClick()
    }

    private fun observeUiStates() {
        scope.launch {
            searchViewModel.uiState.collect { state ->
                withContext(Dispatchers.Main) {
                    when (state) {
                        SearchUiState.Idle -> {
                            startButton.text = "Start search"
                        }

                        SearchUiState.Loading -> {
                            startButton.text = "Cancel"
                        }

                        is SearchUiState.Success -> {
                            startButton.text = "Start search"
                            resultListModel.clear()
                            state.results.forEach { occurrence ->
                                resultListModel.addElement(occurrence)
                            }
                        }

                        is SearchUiState.Error -> {
                            startButton.text = "Start search"
                            resultListModel.clear()
                            JOptionPane.showMessageDialog(
                                this@SearchToolWindow,
                                "Error: ${state.message}",
                                "Search Error",
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initializeButtonListener() {
        startButton.addActionListener {
            when (searchViewModel.uiState.value) {
                SearchUiState.Idle,
                is SearchUiState.Success,
                is SearchUiState.Error -> {
                    val path = Path.of(pathField.text.trim())
                    val query = queryField.text.trim()

                    if (pathField.text.trim().isEmpty() || query.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Please enter both directory path and search query.",
                            "Input Error",
                            JOptionPane.WARNING_MESSAGE
                        )
                        return@addActionListener
                    }

                    resultListModel.clear()
                    startButton.text = "Cancel"
                    searchViewModel.startSearch(path, query)
                }

                SearchUiState.Loading -> {
                    searchViewModel.cancelSearch()
                }
            }
        }
    }

    private fun initializeUI() {
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val fieldHeight = 25
        pathField.maximumSize = Dimension(Int.MAX_VALUE, fieldHeight)
        queryField.maximumSize = Dimension(Int.MAX_VALUE, fieldHeight)

        val pathPanel = JPanel()
        pathPanel.layout = BoxLayout(pathPanel, BoxLayout.X_AXIS)
        pathPanel.add(JLabel("Directory path: "))
        pathPanel.add(pathField)

        val queryPanel = JPanel()
        queryPanel.layout = BoxLayout(queryPanel, BoxLayout.X_AXIS)
        queryPanel.add(JLabel("Search query: "))
        queryPanel.add(queryField)
        queryPanel.add(Box.createRigidArea(Dimension(5, 0)))
        queryPanel.add(startButton)

        resultList.fixedCellHeight = 20
        resultList.visibleRowCount = 15
        val scrollPane = JScrollPane(resultList)
        scrollPane.preferredSize = Dimension(400, 300)

        mainPanel.add(pathPanel)
        mainPanel.add(Box.createRigidArea(Dimension(0, 5)))
        mainPanel.add(queryPanel)
        mainPanel.add(Box.createRigidArea(Dimension(0, 10)))
        mainPanel.add(scrollPane)

        setContent(mainPanel)
    }

    private fun setupResultClick() {
        resultList.cellRenderer = JLabelRenderer()
        resultList.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                val occurrence = resultList.selectedValue ?: return@addListSelectionListener
                val virtualFile = LocalFileSystem.getInstance().findFileByPath(occurrence.file.toString())
                    ?: return@addListSelectionListener
                OpenFileDescriptor(project, virtualFile, occurrence.line - 1, occurrence.offset).navigate(true)
            }
        }
    }

    private class JLabelRenderer : ListCellRenderer<Occurrence> {
        override fun getListCellRendererComponent(
            list: JList<out Occurrence>,
            value: Occurrence,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): java.awt.Component {
            val label = JLabel("${value.file}: ${value.line}:${value.offset}")
            label.isOpaque = true
            label.background = if (isSelected) list.selectionBackground else list.background
            label.foreground = if (isSelected) list.selectionForeground else list.foreground
            return label
        }
    }
}