package com.nemanjapluzarev.searchfileintellijplugin.toolwindow

import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.nemanjapluzarev.searchfileintellijplugin.domain.viewModel.SearchViewModel
import com.nemanjapluzarev.searchfileintellijplugin.presentation.uiState.SearchUiState
import kotlinx.coroutines.*
import java.awt.Dimension
import java.nio.file.Path
import javax.swing.*
import kotlin.io.path.isDirectory

class SearchToolWindow(
    private val searchViewModel: SearchViewModel
) : SimpleToolWindowPanel(true, true) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val rootPanel: JComponent get() = this
    val pathField = JTextField()
    val queryField = JTextField()
    val startButton = JButton("Start search")
    val outputArea = JTextArea()

    init {
        initializeUI()
        initializeButtonListener()
        observeUiStates()
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
                            outputArea.text = state.results.joinToString("\n") {
                                "${it.file}: ${it.line}:${it.offset}"
                            }
                        }

                        is SearchUiState.Error -> {
                            startButton.text = "Start search"
                            outputArea.text = "Error: ${state.message}"
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

                    if (!path.isDirectory() || query.isEmpty()) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Please enter both directory path and search query.",
                            "Input Error",
                            JOptionPane.WARNING_MESSAGE
                        )
                        return@addActionListener
                    }

                    outputArea.text = ""
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

        outputArea.isEditable = false
        val scrollPane = JScrollPane(outputArea)
        scrollPane.preferredSize = Dimension(400, 300)

        mainPanel.add(pathPanel)
        mainPanel.add(Box.createRigidArea(Dimension(0, 5)))
        mainPanel.add(queryPanel)
        mainPanel.add(Box.createRigidArea(Dimension(0, 10)))
        mainPanel.add(scrollPane)

        setContent(mainPanel)
    }
}