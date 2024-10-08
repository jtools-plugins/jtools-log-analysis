package com.lhstack

import com.intellij.execution.filters.ConsoleDependentFilterProvider
import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import groovy.lang.GString
import groovy.lang.GroovyShell

class LogConsoleFilterProvider : ConsoleDependentFilterProvider() {
    override fun getDefaultFilters(
        consoleView: ConsoleView,
        project: Project,
        scope: GlobalSearchScope,
    ): Array<out Filter?> {
        val view = consoleView as ConsoleViewImpl
        return view.getClientProperty(LogToolWindowFactory.ID)?.let {
            arrayOf()
        } ?: arrayOf(object : Filter {

            val groovyShell = GroovyShell()

            override fun applyFilter(
                line: String,
                entireLength: Int,
            ): Filter.Result? {
                val logView = LogContext.get(project)
                if (logView != null) {
                    val impl = logView as ConsoleViewImpl
                    val filterText = impl.getClientProperty("filterText")

                    if (filterText != null) {
                        try {
                            groovyShell.setVariable("result", line)
                            val evaluate = groovyShell.evaluate(filterText as String?)
                            val result = groovyShell.getVariable("result")?.toString() ?: ""
                            if (evaluate != null) {
                                if (evaluate is Boolean && evaluate) {
                                    LogContext.get(project)?.print(result, ConsoleViewContentType.SYSTEM_OUTPUT)
                                } else if (evaluate is GString || evaluate is String) {
                                    if (line.contains(evaluate.toString())) {
                                        LogContext.get(project)?.print(result, ConsoleViewContentType.SYSTEM_OUTPUT)
                                    }
                                } else if (evaluate !is Boolean) {
                                    LogContext.get(project)
                                        ?.print(result, ConsoleViewContentType.SYSTEM_OUTPUT)
                                }

                            } else {
                                LogContext.get(project)?.print(result, ConsoleViewContentType.SYSTEM_OUTPUT)
                            }
                        } catch (e: Throwable) {
                            if (line.contains(filterText.toString())) {
                                LogContext.get(project)?.print(line, ConsoleViewContentType.SYSTEM_OUTPUT)
                            }
                        }
                    } else {
                        LogContext.get(project)?.print(line, ConsoleViewContentType.SYSTEM_OUTPUT)
                    }
                }

                return null
            }
        })
    }


    companion object {
        fun registry() {
            ApplicationManager.getApplication().extensionArea.getExtensionPoint<ConsoleFilterProvider>(
                ConsoleFilterProvider.FILTER_PROVIDERS
            )
                .registerExtension(LogConsoleFilterProvider()) {}
        }

        fun unRegistry() {
            ApplicationManager.getApplication().extensionArea.getExtensionPoint<ConsoleFilterProvider>(
                ConsoleFilterProvider.FILTER_PROVIDERS
            )
                .unregisterExtension(LogConsoleFilterProvider::class.java)
        }
    }

}