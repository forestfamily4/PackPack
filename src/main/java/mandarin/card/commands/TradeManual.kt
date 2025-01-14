package mandarin.card.commands

import mandarin.card.CardBot
import mandarin.card.supporter.CardData
import mandarin.card.supporter.TradingSession
import mandarin.card.supporter.log.TransactionLogger
import mandarin.packpack.commands.Command
import mandarin.packpack.supporter.StaticStore
import mandarin.packpack.supporter.lang.LangID
import mandarin.packpack.supporter.server.CommandLoader
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.utils.concurrent.Task
import net.dv8tion.jda.api.utils.messages.MessageCreateData

class TradeManual : Command(LangID.EN, true) {
    override fun doSomething(loader: CommandLoader) {
        val ch = loader.channel
        val g = loader.guild
        val m = loader.member

        if (!CardData.hasAllPermission(m) && m.id != StaticStore.MANDARIN_SMELL) {
            return
        }

        val contents = loader.content.split(" ")

        if (contents.size < 3) {
            replyToMessageSafely(ch, "Not enough data! When you call this command, you have to provide two users" +
                    " who will participate in trading\n\nUser must be provided via either mention of their ID\n\nFor " +
                    "example, if user called `A` and user called `B`, then you have to call command like `${CardBot.globalPrefix}" +
                    "trade @A @B`", loader.message) { a -> a }

            return
        }

        val task = getTwoUsers(contents, g)

        task.onSuccess { members ->
            if (members.size < 2) {
                replyToMessageSafely(ch, "Bot failed to find two members from the command. Please check if you have offered proper" +
                        " user data to the bot", loader.message
                ) { a -> a }

                return@onSuccess
            }

            if (members[0].id == members[1].id) {
                replyToMessageSafely(ch, "You provided same two members!", loader.message) { a -> a }

                return@onSuccess
            }

            if (members.any { member -> member.user.isBot }) {
                replyToMessageSafely(ch, "You can't trade with bots!", loader.message) { a -> a }

                return@onSuccess
            }

            val forum = g.getForumChannelById(if (CardBot.test) CardData.testTradingPlace else CardData.tradingPlace) ?: return@onSuccess

            val postData = MessageCreateData.fromContent("## Welcome to trading session #${CardData.sessionNumber}\n" +
                    "\n" +
                    "${members[0].asMention} ${members[1].asMention}\n" +
                    "\n" +
                    "This post has been created to focus on trading between both of you. You can suggest or discuss about cards or cf that will be traded\n" +
                    "\n" +
                    "### Guide for trading\n" +
                    "\n" +
                    "If you are trading cards for first time, this place may be quite new and confusing for you. Please read guide below thoroughly to properly trade cards with others!\n" +
                    "\n" +
                    "1. First, you and the other traders have to suggest which cards will be traded, and how much cf will be traded\n" +
                    "\n" +
                    "- This can be done by calling `${CardBot.globalPrefix}suggest` command. Once command is called, it will open up your inventory. You can select cards that will be traded, up to 10 cards in total. Both must suggest something at least to make trading done. If you want to edit what you've suggested, call the command again, and re-suggest\n" +
                    "\n" +
                    "- Cf can be handled by bot, so **we do not recommend users to perform manual cf transferring.** As this warning exists, we won't take any responsibilities from problems that can happen regarding manual transferring\n" +
                    "\n" +
                    "2. Second, both users must agree on what they are trading\n" +
                    "\n" +
                    "- **Please check what the other traders has suggested thoroughly, and check if it meets requirements that you wanted. Once trading is done, it cannot be undone, and mistakes won't be handled by anyone.** Both users must call `${CardBot.globalPrefix}confirm` to confirm their suggestion. If any of users edits their suggestion, this confirmation will be canceled, so suggestion must not be edited to make confirmation done\n" +
                    "\n" +
                    "If you want to cancel trading, please call `${CardBot.globalPrefix}cancel`. Any of user calling this command will directly cancel the trading, and this session will be closed\n" +
                    "\n" +
                    "**__DO NOT scam others.__**  Keep in mind that all transactions will be logged, and this post won't be deleted"
            )

            forum.createForumPost("Trading Session #${CardData.sessionNumber}", postData).queue { post ->
                CardData.sessionNumber++

                val session = TradingSession(post.threadChannel.idLong, members.map { me -> me.idLong }.toTypedArray())

                CardData.sessions.add(session)

                members.forEach { member -> CardData.tradeCooldown[member.id] = 0 }

                CardBot.saveCardData()

                TransactionLogger.logTradeStart(session, m)
            }
        }
    }

    private fun getTwoUsers(contents: List<String>, g: Guild) : Task<List<Member>> {
        val result = ArrayList<UserSnowflake>()

        for (segment in contents) {
            if (StaticStore.isNumeric(segment)) {
                result.add(UserSnowflake.fromId(segment))
            } else if (segment.startsWith("<@")) {
                result.add(UserSnowflake.fromId(segment.replace("<@", "").replace(">", "")))
            }
        }

        return g.retrieveMembers(result)
    }
}