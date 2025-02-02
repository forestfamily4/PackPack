package mandarin.card.commands

import mandarin.card.supporter.CardData
import mandarin.card.supporter.Inventory
import mandarin.packpack.commands.Command
import mandarin.packpack.supporter.lang.LangID
import mandarin.packpack.supporter.server.CommandLoader

class Hack : Command(LangID.EN, true) {
    override fun doSomething(loader: CommandLoader) {
        val ch = loader.channel
        val m = loader.member

        val inventory = Inventory.getInventory(m.id)

        CardData.permanents.forEachIndexed { index, bannerSet -> bannerSet.forEach { banner -> CardData.bannerData[index][banner].forEach { id ->
            val card = CardData.cards.find { c -> c.unitID == id }

            if (card != null)
                inventory.cards[card] = (inventory.cards[card] ?: 0) + 1
        } } }

        ch.sendMessage("Prr").queue()
    }
}