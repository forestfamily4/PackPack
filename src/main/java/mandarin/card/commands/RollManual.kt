package mandarin.card.commands

import mandarin.card.CardBot
import mandarin.card.supporter.Card
import mandarin.card.supporter.CardData
import mandarin.card.supporter.Inventory
import mandarin.card.supporter.transaction.TransactionLogger
import mandarin.packpack.commands.Command
import mandarin.packpack.supporter.EmojiStore
import mandarin.packpack.supporter.StaticStore
import mandarin.packpack.supporter.lang.LangID
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.utils.FileUpload
import kotlin.random.Random

class RollManual : Command(LangID.EN, true) {
    @Throws(Exception::class)
    override fun doSomething(event: GenericMessageEvent) {
        val ch = getChannel(event) ?: return
        val m = getMember(event) ?: return
        val g = getGuild(event) ?: return

        if (m.id != StaticStore.MANDARIN_SMELL && !CardData.isManager(m))
            return

        val contents = getContent(event)?.split(" ") ?: return

        if (contents.size < 3) {
            replyToMessageSafely(ch, "Not enough data! When you call this command, you have to provide user and " +
                    "which pack to be rolled. Format will be `${CardBot.globalPrefix}roll [User] [Pack]`\n\nUser can be provided via either " +
                    "ID or mention. For pack, call `-l` for large pack, `-s` for small pack, and `-p` for premium pack\n\nFor example, if you want" +
                    "to roll large pack for user A, then you have to call `p!roll @A -l`", getMessage(event)) { a -> a }

            return
        }

        val userID = getUserID(contents)

        if (userID.isBlank() || !StaticStore.isNumeric(userID)) {
            replyToMessageSafely(ch, "Bot failed to find user ID from command. User must be provided via either mention of user ID", getMessage(event)) { a -> a }

            return
        }

        val pack = findPack(contents)

        if (pack == CardData.Pack.NONE) {
            replyToMessageSafely(ch, "Please specify which pack will be rolled. Pass `-l` for large pack, an pass `-s` for small pack", getMessage(event)) { a -> a }

            return
        }

        try {
            val targetMember = g.retrieveMember(UserSnowflake.fromId(userID)).complete()

            replyToMessageSafely(ch, "\uD83C\uDFB2 Rolling...!", getMessage(event)) { a -> a }

            val result = rollCards(pack)

            val inventory = Inventory.getInventory(targetMember.id)

            try {
                val builder = StringBuilder("### ${pack.getPackName()} Result [${result.size} cards in total]\n\n")

                for (card in result) {
                    builder.append("- ")

                    if (card.tier == CardData.Tier.ULTRA) {
                        builder.append(Emoji.fromUnicode("✨").formatted).append(" ")
                    } else if (card.tier == CardData.Tier.LEGEND) {
                        builder.append(EmojiStore.ABILITY["LEGEND"]?.formatted).append(" ")
                    }

                    builder.append(card.cardInfo())

                    if (!inventory.cards.containsKey(card)) {
                        builder.append(" {**NEW**}")
                    }

                    if (card.tier == CardData.Tier.ULTRA) {
                        builder.append(" ").append(Emoji.fromUnicode("✨").formatted)
                    } else if (card.tier == CardData.Tier.LEGEND) {
                        builder.append(" ").append(EmojiStore.ABILITY["LEGEND"]?.formatted)
                    }

                    builder.append("\n")
                }

                ch.sendMessage(builder.toString())
                    .setMessageReference(getMessage(event))
                    .mentionRepliedUser(false)
                    .addFiles(result.filter { c -> !inventory.cards.containsKey(c) }.map { c -> FileUpload.fromData(c.cardImage, "${c.name}.png") })
                    .queue()
            } catch (_: Exception) {

            }

            inventory.addCards(result)

            TransactionLogger.logRoll(result, pack, targetMember, true)
        } catch (_: Exception) {
            replyToMessageSafely(ch, "Bot failed to find provided user in this server", getMessage(event)) { a -> a }
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

    private fun findPack(contents: List<String>) : CardData.Pack {
        for(segment in contents) {
            when(segment) {
                "-s" -> return CardData.Pack.SMALL
                "-l" -> return CardData.Pack.LARGE
                "-p" -> return CardData.Pack.PREMIUM
            }
        }

        return CardData.Pack.NONE
    }

    private fun rollCards(pack: CardData.Pack) : List<Card> {
        val result = ArrayList<Card>()

        when(pack) {
            CardData.Pack.SMALL -> {
                repeat(4) {
                    result.add(CardData.common.random())
                }

                val chance = Random.nextDouble()

                if (chance <= 0.7) {
                    result.add(CardData.appendUncommon(CardData.common).random())
                } else {
                    result.add(CardData.uncommon.random())
                }
            }
            CardData.Pack.LARGE -> {
                repeat(8) {
                    result.add(CardData.common.random())
                }

                var chance = Random.nextDouble()

                if (chance <= 0.5) {
                    result.add(CardData.common.random())
                } else {
                    result.add(CardData.appendUncommon(CardData.uncommon).random())
                }

                chance = Random.nextDouble()

                if (chance <= 0.99) {
                    result.add(CardData.appendUncommon(CardData.uncommon).random())
                } else {
                    result.add(CardData.ultraRare.random())
                }
            }
            CardData.Pack.PREMIUM -> {
                repeat(5) {
                    val chance = Random.nextDouble()

                    if (chance <= 0.93) {
                        result.add(CardData.common.random())
                    } else if (chance <= 0.99) {
                        result.add(CardData.ultraRare.random())
                    } else {
                        result.add(CardData.appendLR(CardData.legendRare).random())
                    }
                }
            }
            else -> {
                throw IllegalStateException("Invalid pack type $pack found")
            }
        }

        return result
    }
}
