package com.lhstack

import com.intellij.build.BuildTextConsoleView
import com.intellij.execution.filters.Filter
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTextField
import com.lhstack.tools.plugins.Helper
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class LogToolWindowFactory(val disposable: Disposable) : ToolWindowFactory {

    companion object {
        const val ID = "Log Analysis"
    }

    override fun init(toolWindow: ToolWindow) {
        toolWindow.title = "JTools Log Analysis"
        toolWindow.setIcon(Helper.findIcon("icons/tab.svg", LogToolWindowFactory::class.java))
    }

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val factory = toolWindow.contentManager.factory
        val console = BuildTextConsoleView(project, true, listOf<Filter>())
        console.putClientProperty(ID, true)
        val content = factory.createContent(JPanel(BorderLayout()).apply {
            this.add(console.component, BorderLayout.CENTER)
            this.add(JBTextField().apply {
                this.document.addDocumentListener(object : DocumentListener {
                    override fun insertUpdate(e: DocumentEvent) {
                        console.putClientProperty("filterText", text)
                    }

                    override fun removeUpdate(e: DocumentEvent) {
                        console.putClientProperty("filterText", text)
                    }

                    override fun changedUpdate(e: DocumentEvent) {
                        console.putClientProperty("filterText", text)
                    }
                })
            }, BorderLayout.NORTH)
        }, "JTools Log Analysis", true)
        LogContext.put(project, console)
        val editorEx = console.editor as EditorEx
        editorEx.settings.isUseSoftWraps = true
        Disposer.register(disposable) {
            toolWindow.remove()
            Disposer.dispose(console)
            Disposer.dispose(toolWindow.contentManager)
        }
        content.isCloseable = false
        content.icon = Helper.findIcon("icons/tab.svg", LogToolWindowFactory::class.java)
        toolWindow.contentManager.addContent(content)
    }
}