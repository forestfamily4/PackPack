package mandarin.card.commands

import mandarin.card.CardBot
import mandarin.card.supporter.CardData
import mandarin.card.supporter.holder.PackSelectHolder
import mandarin.packpack.commands.Command
import mandarin.packpack.supporter.StaticStore
import mandarin.packpack.supporter.lang.LangID
import mandarin.packpack.supporter.server.CommandLoader
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class Roll : Command(LangID.EN, true) {
    override fun doSomething(loader: CommandLoader) {
        val ch = loader.channel
        val m = loader.member

        if (CardBot.rollLocked && !CardData.hasAllPermission(m) && m.id != StaticStore.MANDARIN_SMELL) {
            return
        }

        replyToMessageSafely(ch, "Please select the pack that you want to roll", loader.message, { a ->
            a.setComponents(registerComponents(m))
        }, { msg ->
            val content = loader.content.split(" ")

            val noImage = arrayOf("-s", "-simple", "-n", "-noimage").any { p -> p in content }

            StaticStore.putHolder(m.id, PackSelectHolder(loader.message, ch.id, msg, noImage))
        })
    }

    private fun registerComponents(member: Member) : List<LayoutComponent> {
        val result = ArrayList<LayoutComponent>()

        val packOptions = ArrayList<SelectOption>()

        val largeDesc = if ((CardData.cooldown[member.id]?.get(CardData.LARGE) ?: 0) - CardData.getUnixEpochTime() > 0) {
            "Cooldown Left : ${CardData.convertMillisecondsToText((CardData.cooldown[member.id]?.get(CardData.LARGE) ?: 0) - CardData.getUnixEpochTime())}"
        } else {
            "8 Common + 1 Common/Uncommon + 1 Uncommon/Ultra Rare"
        }

        packOptions.add(SelectOption.of("Large Card Pack [10k cf]", "large").withDescription(largeDesc))

        val smallDesc = if ((CardData.cooldown[member.id]?.get(CardData.SMALL) ?: 0) - CardData.getUnixEpochTime() > 0) {
            "Cooldown Left : ${CardData.convertMillisecondsToText((CardData.cooldown[member.id]?.get(CardData.SMALL) ?: 0) - CardData.getUnixEpochTime())}"
        } else {
            "4 Common + 1 Common/Uncommon"
        }

        packOptions.add(SelectOption.of("Small Card Pack [5k cf]", "small").withDescription(smallDesc))

        val premiumDesc = if ((CardData.cooldown[member.id]?.get(CardData.PREMIUM) ?: 0) - CardData.getUnixEpochTime() > 0) {
            "Cooldown Left : ${CardData.convertMillisecondsToText((CardData.cooldown[member.id]?.get(CardData.PREMIUM) ?: 0) - CardData.getUnixEpochTime())}"
        } else {
            "5 Common/Ultra Rare/Legend Rare"
        }

        packOptions.add(SelectOption.of("Premium Card Pack [5 Tier 2 Cards]", "premium").withDescription(premiumDesc))

        val packs = StringSelectMenu.create("pack")
            .addOptions(packOptions)
            .setPlaceholder("Select pack here")
            .build()

        result.add(ActionRow.of(packs))

        return result
    }
}