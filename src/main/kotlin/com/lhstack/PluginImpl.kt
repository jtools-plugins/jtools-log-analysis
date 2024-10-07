package com.lhstack

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.lhstack.tools.plugins.Helper
import com.lhstack.tools.plugins.IPlugin
import com.lhstack.tools.plugins.Logger
import com.lhstack.tools.plugins.PluginType
import javax.swing.Icon

class PluginImpl : IPlugin {

    val disposer = hashMapOf<String, Disposable>()

    override fun unInstall() {
        LogConsoleFilterProvider.unRegistry()
    }

    override fun install() {
        LogConsoleFilterProvider.registry()

    }

    override fun openProject(project: Project, logger: Logger, openThisPage: Runnable) {
        val disposable = Disposer.newDisposable()
        LogContext.put(project.locationHash, logger)
        disposer.put(project.locationHash, disposable)
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindowManager.getToolWindow(LogToolWindowFactory.ID)?.remove()
        toolWindowManager.registerToolWindow(LogToolWindowFactory.ID) {
            this.contentFactory = LogToolWindowFactory(disposable)
            this.anchor = ToolWindowAnchor.RIGHT
            this.icon = Helper.findIcon("icons/logo.svg", PluginImpl::class.java)
            this.canCloseContent = false
            this.sideTool = true
        }
    }

    override fun closeProject(project: Project) {
        LogContext.remove(project.locationHash)
        disposer.remove(project.locationHash)?.let { Disposer.dispose(it) }
    }

    override fun pluginType(): PluginType? = PluginType.JAVA_NON_UI

    override fun pluginIcon(): Icon? = Helper.findIcon("icons/logo.svg", PluginImpl::class.java)

    override fun pluginTabIcon(): Icon? = Helper.findIcon("icons/tab.svg", PluginImpl::class.java)

    override fun pluginName(): String? = "日志分析"

    override fun pluginDesc(): String? = "分析日志数据"

    override fun pluginVersion(): String? = "0.0.1"

}