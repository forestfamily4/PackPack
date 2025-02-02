package mandarin.card.supporter.filter

import common.pack.UserProfile
import common.util.unit.Trait
import mandarin.card.supporter.Card

class TraitFilter(private val trait: Trait, amount: Int, name: String) : Filter(amount, name) {
    override fun filter(card: Card): Boolean {
        val uber = UserProfile.getBCData().units[card.unitID]

        return uber.forms.any { f ->
            if (f.fid == 2 && f.du.pCoin != null) {
                f.du.pCoin.full.traits.contains(trait)
            } else {
                f.du.traits.contains(trait)
            }
        }
    }
}