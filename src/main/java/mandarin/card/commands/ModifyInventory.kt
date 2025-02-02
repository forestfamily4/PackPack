package mandarin.card.commands

import mandarin.card.supporter.CardData
import mandarin.card.supporter.Inventory
import mandarin.card.supporter.holder.ModifyCategoryHolder
import mandarin.packpack.commands.Command
import mandarin.packpack.supporter.StaticStore
import mandarin.packpack.supporter.lang.LangID
import mandarin.packpack.supporter.server.CommandLoader
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class ModifyInventory : Command(LangID.EN, true) {
    override fun doSomething(loader: CommandLoader) {
        val ch = loader.channel
        val m = loader.member
        val g = loader.guild

        if (m.id != StaticStore.MANDARIN_SMELL && !CardData.hasAllPermission(m))
            return

        val contents = loader.content.split(" ")

        if (contents.size < 2) {
            replyToMessageSafely(ch, "You have to provide member whose inventory will be managed!", loader.message) { a -> a }

            return
        }

        val userID = getUserID(contents)

        if (userID.isBlank() || !StaticStore.isNumeric(userID)) {
            replyToMessageSafely(ch, "Bot failed to find user ID from command. User must be provided via either mention of user ID", loader.message) { a -> a }

            return
        }

        try {
            g.retrieveMember(UserSnowflake.fromId(userID)).queue() { targetMember ->
                if (targetMember.user.isBot) {
                    replyToMessageSafely(ch, "You can't modify inventory of the bot!", loader.message) { a -> a }

                    return@queue
                }

                val inventory = Inventory.getInventory(targetMember.id)

                replyToMessageSafely(ch, "Please select which thing you want to modify for inventory of ${targetMember.asMention}", loader.message, { a ->
                    a.setComponents(registerComponents())
                }, { msg ->
                    StaticStore.putHolder(m.id, ModifyCategoryHolder(loader.message, ch.id, msg, inventory, targetMember))
                })
            }
        } catch (_: Exception) {
            replyToMessageSafely(ch, "Bot failed to find provided user in this server", loader.message) { a -> a }
        }
    }

    private fun getUserID(contents: List<String>) : String {
        for(segment in contents) {
            if (StaticStore.isNumeric(segment)) {
                return segment
            } else if (segment.startsWith("<@")) {
                return segment.replace("<@", "").replace(">", "")
            }
        }

        return ""
    }

    private fun registerComponents() : List<LayoutComponent> {
        val rows = ArrayList<ActionRow>()

        val modeOptions = ArrayList<SelectOption>()

        modeOptions.add(SelectOption.of("Cards", "card"))
        modeOptions.add(SelectOption.of("Vanity Roles", "role"))

        val modes = StringSelectMenu.create("category")
            .addOptions(modeOptions)
            .setPlaceholder("Select category that you want to modify")
            .build()

        rows.add(ActionRow.of(modes))

        rows.add(ActionRow.of(Button.danger("close", "Close")))

        return rows
    }
}