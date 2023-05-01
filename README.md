# HavenBags
<a href="https://github.com/Valorless/HavenBags" rel="nofollow"><img src="https://img.shields.io/badge/Tested On-%201.19-brightgreen?style=flat" alt="Versions" style="max-width: 100%;"/>
<a href="https://github.com/Valorless/ValorlessUtils" rel="nofollow"><img src="https://img.shields.io/badge/Requires-ValorlessUtils-red?style=flat" alt="Dependency" style="max-width: 100%;"/>
<br>

## Commands
| Command | Parameters | Permission | Description |
| --- | --- | --- | --- |
| `/havenbags` |  |  |
| `/havenbags reload` | | `havenbags.reload` | Reloads config.yml & lang.yml |
| `/havenbags create` | `<size>` | `havenbags.create` | Create new bags |
| `/havenbags give` | `<player> <size>` | `havenbags.create` | Give player a bag |
| `/havenbags restore` | `<player> (bag-uuid)` | `havenbags.restore` | Show all bags by \<player\>,<br> or restore specific bag with (bag-uuid) |

Required: \<\><br>
Optional: ( )<br>
<br>
*All commands can be shortened to /bags*<br>
<br>
*Example of a bound bag: `/bags create 27`*<br>
*Example of an ownerless bag: `/bags create ownerless 27`*<br>
*(The same can be applied to the 'give' command.)*

  
## Permissions
| Permission | Description |
| --- | --- |
| `havenbags.*` | Gives all HavenBags permissions. |
| `havenbags.reload` | Allows you to reload the plugin. |
| `havenbags.use` | Allows you to use bags. |
| `havenbags.create` | Allows you to create bags. |
| `havenbags.restore` | Allows you to restore bags. |
| `havenbags.bypass` | Allows you to bypass ownership locks. |


## Configuration
[config.yml](/src/main/resources/config.yml)


## Lang
[lang.yml](/src/main/resources/lang.yml)
