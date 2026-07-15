# ArmorAura

A Fabric mod that adds passive effects and buffs based on the armor trims you're wearing.

## Trim Effects

### Snout Trim

Equip the snout trim on any piece of armor to unlock piglin-related effects.

---

**Piglin Aggro Immunity**

Piglins will not attack you, even without golden armor. The snout trim signals to them that you're one of their own.

---

**Piglin Bartering: Three-Tier Trade System**

When you throw a gold ingot at a piglin while wearing the snout trim, the trade is upgraded. The tier is rolled the moment the piglin picks up the gold:

| Tier | Chance | Message |
|------|--------|---------|
| Lucky Trade | 75% | "Lucky Trade!" |
| Extra Lucky Trade | 24% | "Extra Lucky Trade!" |
| Super Duper Lucky Trade | 1% | "Super Duper Lucky Trade!" |

Without the snout trim, bartering is 100% vanilla, no changes whatsoever.

---

**Lucky Trade (75%)**

- Uses a boosted loot table: all rare items have doubled weight vs vanilla
- Junk items removed: no crying obsidian, leather, soul sand, nether brick, or blackstone
- Admiration time: vanilla (~6 seconds)

Possible drops (no junk): Soul Speed book, Soul Speed iron boots, fire resistance potions, water potion, iron nuggets, ender pearls, dried ghast, string, quartz, obsidian, fire charge, spectral arrows, gravel

---

**Extra Lucky Trade (24%)**

- Uses a further boosted loot table: rare items at 4x vanilla weight
- Same junk items removed as Lucky Trade
- Admiration time: ~9 seconds (50% longer than vanilla)
- Guaranteed bonus rare item on top of the random roll (randomly one of: ender pearls, fire resistance potion, dried ghast, diamond, or iron nuggets)

---

**Super Duper Lucky Trade (1%)**

- Piglin shakes during admiration
- Admiration time: ~13 seconds (50% longer than Extra Lucky)
- Gives **5 full Extra Lucky Trade results**: each roll includes the random table drop plus a guaranteed rare bonus item
- Piglin dies after trading

---

## Setup

Requirements:

- Java 25
- Minecraft 26.1.2
- Fabric Loader 0.19.2 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)

Build the mod jar (written to `build/libs/`):

```
./gradlew build
```

Or launch a development client with the mod already loaded:

```
./gradlew runClient
```

## License

This mod is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
