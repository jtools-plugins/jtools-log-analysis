package com.lhstack

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.lang.Language
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBDimension
import com.lhstack.tools.plugins.Helper
import org.apache.commons.lang3.StringUtils
import java.awt.BorderLayout
import java.io.File
import java.nio.file.Files
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

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
        val consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
        consoleBuilder.setViewer(true)
        val console = consoleBuilder.console as ConsoleViewImpl
        console.putClientProperty(ID, true)
        val content = factory.createContent(JPanel(BorderLayout()).apply {
            this.add(console.component, BorderLayout.CENTER)
            this.add(JPanel().apply {
                this.layout = BoxLayout(this, BoxLayout.X_AXIS)
                val cacheScript = loadCache(project)
                val textField =
                    LanguageTextField(Language.findLanguageByID("Groovy"), project, cacheScript, true).apply {
                        this.document.addDocumentListener(object : com.intellij.openapi.editor.event.DocumentListener {
                            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                                console.putClientProperty("filterText", event.document.text)
                                storeCache(project, event.document.text)
                            }
                        })
                    }
                if (StringUtils.isNotBlank(cacheScript)) {
                    console.putClientProperty("filterText", cacheScript)
                }
                Disposer.register(disposable) {
                    textField.editor?.let {
                        EditorFactory.getInstance().releaseEditor(it)
                    }
                }
                this.add(textField)
                this.add(ActionButton(object :
                    AnAction({ "打开脚本面板" }, Helper.findIcon("icons/groovy.svg", PluginImpl::class.java)) {
                    override fun actionPerformed(e: AnActionEvent) {
                        val dialogWrapper = object : DialogWrapper(project, true) {
                            val languageTextField = object :
                                LanguageTextField(Language.findLanguageByID("Groovy"), project, textField.text, false) {
                                override fun createEditor(): EditorEx {
                                    val editorEx = EditorFactory.getInstance()
                                        .createEditor(document, project, fileType, false) as EditorEx
                                    editorEx.highlighter = HighlighterFactory.createHighlighter(project, fileType)
                                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(
                                        editorEx.document
                                    )
                                    if (psiFile != null) {
                                        DaemonCodeAnalyzer.getInstance(project).setHighlightingEnabled(psiFile, true)
                                    }
                                    editorEx.setBorder(null)
                                    val settings = editorEx.settings
                                    settings.additionalLinesCount = 0
                                    settings.additionalColumnsCount = 1
                                    settings.isLineNumbersShown = true
                                    settings.lineCursorWidth = 1
                                    settings.isLineMarkerAreaShown = false
                                    settings.setRightMargin(-1)
                                    return editorEx
                                }
                            }

                            init {
                                languageTextField.document.addDocumentListener(object :
                                    com.intellij.openapi.editor.event.DocumentListener {
                                    override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                                        textField.setText(event.document.text)
                                        storeCache(project, event.document.text)
                                    }
                                })
                                Disposer.register(this.disposable) {
                                    languageTextField.editor?.let {
                                        LogContext.get(project.locationHash)?.info("回收textField")
                                        EditorFactory.getInstance().releaseEditor(it)
                                    }
                                }
                                this.setSize(800, 600)
                                this.title = "脚本过滤"
                                super.init()
                            }

                            override fun createCenterPanel(): JComponent? {
                                return languageTextField
                            }

                        }
                        dialogWrapper.showAndGet()
                    }

                }, Presentation().apply {
                    this.text = "打开脚本面板"
                }, ActionPlaces.UNKNOWN, JBDimension(24, 24)))
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

    fun storeCache(project: Project, script: String) {
        project.basePath?.let {
            File("${it}/.idea/JTools Log Analysis.groovy").apply {
                val parent = this.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }.let {
                Files.writeString(it.toPath(), script)
            }
        }
    }

    fun loadCache(project: Project): String = project.basePath?.let {
        File("${it}/.idea/JTools Log Analysis.groovy").apply {
            val parent = this.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }
        }.let {
            if (!it.exists()) {
                ""
            } else {
                Files.readString(it.toPath())!!
            }
        }
    } ?: ""
}