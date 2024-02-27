package com.olillin.rainbowquartz

import com.olillin.rainbowquartz.craft.*
import com.olillin.rainbowquartz.event.ItemEventDispatcher
import com.olillin.rainbowquartz.item.ItemManager
import com.olillin.rainbowquartz.plugin.command.GetItemCommand
import com.olillin.rainbowquartz.plugin.command.MenuCommand
import com.olillin.rainbowquartz.plugin.command.ViewItemCommand
import com.olillin.rainbowquartz.plugin.gui.GuiEventDispatcher
import me.lucko.commodore.Commodore
import me.lucko.commodore.CommodoreProvider
import me.lucko.commodore.file.CommodoreFileReader
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabExecutor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.IOException
import java.util.*

open class RainbowQuartz : JavaPlugin(), Listener {
    companion object {
        lateinit var itemManager: ItemManager
        lateinit var itemEventDispatcher: ItemEventDispatcher
        lateinit var guiEventDispatcher: GuiEventDispatcher
        internal lateinit var instance: RainbowQuartz
    }

    override fun onEnable() {
        // Initialize event dispatchers
        itemEventDispatcher = ItemEventDispatcher(this)
        guiEventDispatcher = GuiEventDispatcher(this)
        guiEventDispatcher.start()

        itemManager = ItemManager(this)
        itemManager.reload()

        // Initialize commands
        registerCommands()

        instance = this
    }

    override fun onLoad() {
        val serializable: List<Class<out ConfigurationSerializable>> = listOf(
            Recipe::class.java,
            Ingredient::class.java,
            BlastingRecipe::class.java,
            CampfireRecipe::class.java,
            FurnaceRecipe::class.java,
            ShapedRecipe::class.java,
            ShapelessRecipe::class.java,
            SmithingTransformRecipe::class.java,
            SmokingRecipe::class.java,
            StonecuttingRecipe::class.java,
        )
        for (clazz in serializable) {
            ConfigurationSerialization.registerClass(clazz)
        }
    }

    @Suppress("SpellCheckingInspection")
    @Throws(IOException::class)
    private fun registerCommands() {

        val getItemCommand = server.getPluginCommand("getitem")
        var executor: TabExecutor = GetItemCommand()
        getItemCommand!!.setExecutor(executor)
        getItemCommand.tabCompleter = executor

        val viewItemCommand = server.getPluginCommand("viewitem")
        executor = ViewItemCommand()
        viewItemCommand!!.setExecutor(executor)
        viewItemCommand.tabCompleter = executor

        val menuCommand = server.getPluginCommand("rainbowquartz")
        executor = MenuCommand(this)
        menuCommand!!.setExecutor(executor)
        menuCommand.tabCompleter = executor

        // check if brigadier is supported by server
        if (CommodoreProvider.isSupported()) {

            // get a commodore instance
            val commodore = CommodoreProvider.getCommodore(this)

            // register completions for each command
            registerCompletionsFromFile(commodore, getItemCommand)
            registerCompletionsFromFile(commodore, viewItemCommand)
        }
    }

    private fun registerCompletionsFromFile(commodore: Commodore, command: PluginCommand) {
        val uri = "commodore/${command.name}.commodore"
        val file = getResource(uri)
        if (file == null) {
            logger.warning("Unable to register completions for command ${command.name}: Could not find file $uri")
        }
        commodore.register(command, CommodoreFileReader.INSTANCE.parse<Any>(file))
    }

    override fun onDisable() {
        itemManager.clear()
    }
}