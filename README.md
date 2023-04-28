# BottomlessBackpacks
A lightweight and simple backpacks plugin for your Spigot server!

### Features
 - Backpacks have 45 slots per page and support up to 1000 pages
 - Admins can view and edit backpacks of online and offline players
 - A simple command to edit the size of a player's backpack
 - Ability to save the page a backpack closed on and open to a specific page
- Incredibly efficient and player-friendly UI

### Requirements
- This plugin has one dependency: [ServerUtils](https://www.spigotmc.org/resources/serverutils.106515/)
- Currently only supports MySQL and MariaDB

### Commands
- The only player command is `/backpack [page]` (alias `bp`) which opens a backpack to the specified page (if provided)
    - Permission: `bottomlessbackpacks.backpack`

### Admin Commands
- The base command is `backpackadmin` (alias `bpa`) with the permission `bottomlessbackpacks.backpackadmin`
- All commands require permission to use which follows the format `bottomlessbackpacks.backpackadmin.command` where command is the name of the command
- Note: arguments with <> are required and [] are optional
- `/bpa clear <player>` Clear all items from a backpack
- `/bpa confirm` Confirm an action
- `/bpa help` Opens the help menu
- `/bpa open <player> [page]` Open a player's backpack
- `/bpa pages <player> <action> <amount>` Edit the number of pages of a backpack
- `/bpa reload [arg]` Reload the whole plugin or a specified part
- `/bpa reset <player>` Resets a backpack
- `/bpa resetAllData` Resets all backpacks

### Backpack Inventory
 - Each page contains a menu bar with some buttons and information
   - A button to increase or decrease the page
   - A button to toggle if the player's page should save for the next time they open the menu (not saved between sessions)
   - An item displaying the page they are on e.g. `Page: 2/10`

![img.png](images/player_backpack.png)

### Additional Information
- The number of pages is limited to 1000
- Decreasing the size of a backpack *will* delete all data on pages that no longer exist
  - If the player is online, then the items will be deleted immediately
  - If the player is offline, then the items will delete the next time the backpack saves item data. For example, lowering the number of pages then setting it back while the player is offline will **not** cause the data to be deleted.