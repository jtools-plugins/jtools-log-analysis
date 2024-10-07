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
            override fun applyFilter(
                line: String,
                entireLength: Int,
            ): Filter.Result? {
                val logView = LogContext.get(project)
                if(logView != null) {
                    val impl = logView as ConsoleViewImpl
                    val filterText = impl.getClientProperty("filterText")
                    if(filterText != null) {
                        if(line.contains(filterText.toString())){
                            LogContext.get(project)?.print(line, ConsoleViewContentType.SYSTEM_OUTPUT)
                        }
                    }else {
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