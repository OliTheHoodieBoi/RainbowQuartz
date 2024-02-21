package com.olillin.rainbowquartz.craft

import com.olillin.rainbowquartz.item.Item
import org.bukkit.Material
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.RecipeChoice.MaterialChoice

class ShapelessRecipe : Recipe() {
    private var group: String = ""
    private val ingredients: MutableList<RecipeChoice> = mutableListOf()
    private var amount: Int = 1
    override val suffix: String
        get() = id

    override fun asBukkitRecipe(item: Item): org.bukkit.inventory.ShapelessRecipe {
        val recipe = org.bukkit.inventory.ShapelessRecipe(
            key(item),
            item.getItem().also {
                it.amount = amount
            }
        )
        recipe.group = group

        for (ingredient in ingredients) {
            recipe.addIngredient(ingredient)
        }
        return recipe
    }

    fun addIngredient(ingredient: RecipeChoice, amount: Int): ShapelessRecipe {
        ingredients.addAll(List(amount) {ingredient})
        return this
    }

    fun addIngredient(ingredient: Material, amount: Int): ShapelessRecipe {
        return addIngredient(MaterialChoice(ingredient), amount)
    }

    fun addIngredient(ingredient: ItemStack, amount: Int): ShapelessRecipe {
        return addIngredient(ExactChoice(ingredient), amount)
    }

    fun addIngredient(ingredient: RecipeChoice): ShapelessRecipe {
        ingredients.add(ingredient)
        return this
    }

    fun addIngredient(ingredient: Material): ShapelessRecipe {
        return addIngredient(MaterialChoice(ingredient))
    }

    fun addIngredient(ingredient: ItemStack): ShapelessRecipe {
        return addIngredient(ExactChoice(ingredient))
    }

    fun getIngredients(): List<RecipeChoice> {
        return ingredients.toList()
    }

    fun removeIngredient(ingredient: RecipeChoice, amount: Int): ShapelessRecipe {
        repeat(amount) {
            removeIngredient(ingredient)
        }
        return this
    }

    fun removeIngredient(ingredient: Material, amount: Int): ShapelessRecipe {
        return removeIngredient(MaterialChoice(ingredient), amount)
    }

    fun removeIngredient(ingredient: ItemStack, amount: Int): ShapelessRecipe {
        return removeIngredient(ExactChoice(ingredient), amount)
    }

    fun removeIngredient(ingredient: RecipeChoice): ShapelessRecipe {
        ingredients.remove(ingredient)
        return this
    }

    fun removeIngredient(ingredient: Material): ShapelessRecipe {
        return removeIngredient(MaterialChoice(ingredient))
    }

    fun removeIngredient(ingredient: ItemStack): ShapelessRecipe {
        return removeIngredient(ExactChoice(ingredient))
    }

    fun setAmount(amount: Int): ShapelessRecipe {
        if (amount < 1) throw IllegalArgumentException("Amount must be at least 1")
        this.amount = amount
        return this
    }

    fun getAmount(): Int = amount

    fun setGroup(group: String): ShapelessRecipe {
        this.group = group
        return this
    }

    fun getGroup(): String = group

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "group" to group,
            "amount" to amount,
            "ingredients" to ingredients.map {
                it.itemStack
            }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShapelessRecipe

        if (group != other.group) return false
        if (ingredients != other.ingredients) return false

        return true
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + ingredients.hashCode()
        return result
    }

    companion object {
        const val id = "shapeless"
        val material = Material.CRAFTING_TABLE

        /**
         * Required method for configuration serialization
         *
         * @param args map to deserialize
         * @return deserialized item stack
         * @see ConfigurationSerializable
         */
        @JvmStatic
        fun deserialize(args: Map<String, Any>): ShapelessRecipe {

            val section = MemoryConfiguration()
            section.addDefaults(args)

            val recipe = ShapelessRecipe()

            val ingredients = section.getList("ingredients") ?: throw IllegalArgumentException("Missing or invalid property 'ingredients'")
            for (ingredient in ingredients) {
                if (ingredient !is ItemStack) throw IllegalArgumentException("Invalid ingredient, expected ItemStack")

                recipe.addIngredient(ingredient)
            }

            val group = section.getString("group")
                ?: throw IllegalArgumentException("Invalid value for property 'group'")
            recipe.setGroup(group)

            val amount = section.getInt("amount", 1)
            recipe.setAmount(amount)

            return recipe
        }
    }
}