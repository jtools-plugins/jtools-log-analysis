package com.lhstack;

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.lhstack.tools.plugins.Logger

class LogContext {

    companion object {
        private val LOG_CACHE = hashMapOf<String, Logger>()

        val LOG_CONSOLE_KEY = Key.create<ConsoleView>("JToolsLogAnalysis")

        fun put(key: String, logger: Logger) = LOG_CACHE.put(key, logger)

        fun get(key: String) = LOG_CACHE[key]

        fun remove(key: String) = LOG_CACHE.remove(key)

        fun clear() = LOG_CACHE.clear()

        fun put(project: Project, consoleView: ConsoleView) {
            project.putUserData(LOG_CONSOLE_KEY, consoleView)
        }

        fun get(project: Project) = project.getUserData(LOG_CONSOLE_KEY)
    }
}
