# HavenBags
<a href="https://github.com/Valorless/HavenBags" rel="nofollow"><img src="https://img.shields.io/badge/Versions-%201.19%20--%201.20%2B-brightgreen?style=flat" alt="Versions" style="max-width: 100%;"/></a>
<a href="https://github.com/Valorless/ValorlessUtils" rel="nofollow"><img src="https://img.shields.io/badge/Requires-ValorlessUtils-red?style=flat" alt="Dependency" style="max-width: 100%;"/></a>
<br>

Create shulker-like bags of varying sizes bound to a player, or accessible by anyone.

## Commands
| Command | Permission | Description |
| --- | --- | --- |
| `/havenbags` |  | §7[§aHaven§bBags§7]§r HavenBags by Valorless. |
| `/havenbags reload`| `havenbags.reload` | Reloads config.yml & lang.yml |
| `/havenbags rename <value>`| `havenbags.rename` | Rename the bag in your hand.<br>*(Supports Hex. Leave value empty to reset.)* |
| `/havenbags create <size>` | `havenbags.create` | Create a new bag |
| `/havenbags create ownerless <size>` | `havenbags.create` | Create a new ownerless bag |
| `/havenbags give <player> <size>` | `havenbags.give` | Give player a bag |
| `/havenbags give <player> ownerless <size>` | `havenbags.give` | Give player an ownerless bag |
| `/havenbags restore <player>` | `havenbags.restore` | Shows a list of bags by that player. |
| `/havenbags restore <player> <bag-uuid>` | `havenbags.restore` | Gives a copy of the bag stored on the server. |
| `/havenbags preview <player>` | `havenbags.preview` | Shows a list of bags by that player. |
| `/havenbags preview <player> <bag-uuid>` | `havenbags.preview` | Preview a copy of the bag stored on the server. |

*All commands can be shortened to /bags & /bag*<br>

## Permissions
| Permission | Description |
| --- | --- |
| `havenbags.*` | Gives all HavenBags permissions. |
| `havenbags.reload` | Allows you to reload the plugin. |
| `havenbags.use` | Allows you to use bags. |
| `havenbags.rename` | Allows you to rename bags.<br>*Without havenbags.bypass, you can only rename your own bags.* |
| `havenbags.create` | Allows you to create bags. |
| `havenbags.give` | Allows you to give bags. |
| `havenbags.restore` | Allows you to restore bags. |
| `havenbags.bypass` | Allows you to bypass ownership locks. |
| `havenbags.preview` | Allows you to preview a copy of a bag, and take their content. |

## Configuration
[config.yml](/src/main/resources/config.yml)

## Lang
[lang.yml](/src/main/resources/lang.yml)
