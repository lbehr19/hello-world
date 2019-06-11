# -*- coding: utf-8 -*-
"""
Created on Fri Oct  5 22:09:26 2018

@author: leahb
"""
#Things I can fix for later:
#    -tidy up the first few lines of the REPL: the action/verb parsing looks weird.
#    -cheeses/item ids. Item objects don't need to keep track of their own ids if I refine the get_item or check function.
#    -item parsing: create a list of trivial objects, things that are in the room according to description that are not currently recognized. 
#    -globalize the major variables - things like player inventory and location, that way I don't have to pass them around and I can try saving/restarting games.
import classes

WELCOME = '''
Welcome to Small Space Adventure! 

You just woke up on your spaceship, the Northern Firelight. There is no one 
else here, and judging by the dim red lights, the ship is on emergency power 
only. You cannot remember exactly what happened, but you must get off this ship 
soon. 

Type 'h' or 'help' for assistance. 
'''
PROMPT = '=> '

HELP = """To move around the map, type the direction you'd like to travel in.
You can go in one of the cardinal directions: NORTH, SOUTH, EAST, and WEST, 
or in some rooms you can go UP or DOWN. 
To get a description of the room you are currently in, type LOOK (l).
For a list of exits out of your current location, type EXITS.

To interact with objects, try EXAMINE (x), TAKE, DROP, or USE. You
can also UNLOCK certain doors with the right key. To check what is in
your inventory, use INVENTORY (i).
If you are done playing, use QUIT (q)."""

##define rooms:
BLANK = classes.Room('Unfinished Room')
CREW_QUARTERS = classes.Room('Crew Quarters')
CAPT_QUARTERS = classes.Room("Captain's Quarters")
HALLWAY= classes.Room('Hallway')
BRIDGE = classes.Room('Bridge')
CARGO_BAY = classes.Room('Cargo Bay, North End')
CARGO_BAY2 = classes.Room('Cargo Bay, South End')
MESS_REC = classes.Room('Mess Hall and Rec Area')
LAB = classes.Room('Laboratory, South End')
SICK_BAY = classes.Room('Sick Bay')
ENGINE_ROOM = classes.Room('Engine Room')
SHUTTLE_BAY = classes.Room('Shuttle Bay')
SHUTTLE_INTERIOR = classes.Room('Shuttle Interior')
LAB2 = classes.Room('Laboratory, North End')
MAZE = []
##define objects:
key_card = classes.Item('key card', 1)
locker_key = classes.Item('metal key', 2)
capt_log = classes.Item('personal log', 3)
key_code_pad = classes.Item('key pad', 4, False)
machine_part = classes.Item('machine part', 5)
alien_plant = classes.Item('strange plant', 6, False)
sedative = classes.Item('syringe', 7)
ship_log = classes.Item("ship's log", 8)
rat_food = classes.Item('piece of cheese', 9)
CHEESE_ID = 9
CURRENT_CHEESE_ID = 9
#TODO add in a ship's log to add world description
#TODO add in description for the viewport
##Define containers:
CREW_LOCKER = classes.Container('locker')
MED_CABINET = classes.Container('medicine cabinet')

##The Rats:
RATS = [classes.Rat(8), classes.Rat(15), classes.Rat(17), classes.Rat(18)]

#TODO list:
#4. Take/drop don't count as moves, but they should. Implement global moves?
#implement buttons, levers, stuff like that as special items. (if needed)

def play():
    load_part_one() #function sets room descriptions, items, etc.
    print(WELCOME)
    game_on = True
    player_location = CREW_QUARTERS
    player_inventory = {}
    print(player_location.get_loc_description())
    while game_on:
        action_text = input(PROMPT).lower()
        action = normalize_action(action_text)
        if len(action.split()) > 1:
            verb = action.split()[0]
        else:
            verb = ''
        result_message = ''
        ### GAMEPLAY COMMANDS: ###
        if action == 'quit':
#            print('\nAre you sure you want to give up now? You cannot restore your progress.')
#            response = input(PROMPT).strip()
#            if response == 'yes' or response == 'y':
#                result_message = 'Thanks for playing!'
#                game_on = False
#            else:
#                result_message = player_location.get_loc_description()
            result_message = 'Thanks for playing!'
            game_on = False
        elif action == 'help':
            result_message = HELP
        elif action == 'look':
            result_message = player_location.get_loc_description()
        elif action == 'inventory':
            result_message = player_inventory_list(player_inventory)
        elif action == 'exits':
            result_message = 'You can go ' + ', '.join(player_location.get_exit_list())
        ### MOVEMENT COMMANDS: ###
        elif validate_direction(action):
            path = player_location.exits.get(action, None)
            if path is None:
                result_message = 'You cannot go that way!'
            elif path.is_locked:
                result_message = 'The passage to the {} is {}.'.format(action, path.lock_phrase)
            else:
                move_rat()
                player_location = path.destination
                if player_location.item_check('rat-like creature'):
                    rat_thief(player_inventory, player_location.get_item('rat-like creature'))
                result_message = player_location.get_loc_description()
        ### OBJECT COMMANDS: ###
        elif is_obj_action(verb):
            result_message = interact(action, player_inventory, player_location)
            if result_message == 'Congratulations! You have beat stage one.':
                print(END_TEXT)
                result_message += '\nThank you for playing! Type "quit" to exit.'
        elif verb == 'break':
            result_message = 'You are not strong enough to try breaking anything right now.'
        ### if user entered nothing: ###
        elif verb == '':
            result_message = 'You did not enter anything. Please try again.'
        ### if all else fails: ###
        else:
            result_message = 'Human, you do not have the time to "{}".'.format(action)
        result_message = '\n' + result_message
        print(result_message)
    quit



def is_obj_action(verb):
    return (verb == 'take' or verb == 'get' or verb == 'drop' or verb == 'examine' 
            or verb == 'open' or verb == 'close' or verb == 'unlock' or verb == 'put'
            or verb == 'use')

def interact(action, player_inventory, player_location):
    """Returns the string that tells the player the result of their 
    attempt to interact with the object specified in action.
    Depending on the verb given, the item is moved from location inventory to
    player inventory or vice versa. 
    """
    global CURRENT_CHEESE_ID
    input_list = action.split()
    verb = input_list[0] #the action being done to/with the item
    item_given = ' '.join(input_list[1:]) #item/phrase following the verb
    item = normalize_item(item_given, player_inventory, player_location)
    if verb == 'examine':
        if player_location == CARGO_BAY2:
            if item == 'port':
                message = unlock_engine_room()
            elif item == 'key pad':
                message = key_code_pad.get_description()
                message += unlock_engine_room()
            else:
                message = 'There is nothing to examine in here other than the door and the key pad.'
        elif item == 'door' or item == 'port':
            message = 'The doors on this ship are just plain automatic doors.'
        elif player_location.item_check(item):
            actual_item = player_location.get_item(item)
            message = actual_item.get_description()
        elif inventory_check(item, player_inventory):
            actual_item = get_item_from_inventory(item, player_inventory)
            message = actual_item.get_description()
        else:
            message = 'You do not see that around here.'
    ## taking items: ##
    elif verb == 'take' or verb == 'get':
        if player_location.item_check(item):
            actual_item = player_location.get_item(item)
            if isinstance(actual_item, classes.Item) and actual_item.is_portable:
                if actual_item == rat_food:
                    item_id = CURRENT_CHEESE_ID
                    new_cheese = classes.Item('piece of cheese', item_id)
                    new_cheese.set_consumable_qualities('It was the only food left on board, but the small chunk of cheese is so stinky\nthat only a rat would eat it.',
                                      'sitting on the counter in the kitchenette.')
                    player_inventory[item_id] = classes.Item('piece of cheese', item_id)
                    CURRENT_CHEESE_ID += 1
                else:
                    player_inventory[actual_item.id] = actual_item
                    player_location.remove_item(item)
                message = 'Taken.'
                #TODO increase move counter
            elif isinstance(actual_item, classes.Rat):
                message = 'You reach your hand out to pick up the rat, and it bites you! Better not \ntry that again.'
                if not actual_item.held_item is None:
                    the_old_item = actual_item.held_item.name
                    actual_item.drop_item(player_location)
                    message += ' Thankfully, it dropped the {}.'.format(the_old_item)
            elif isinstance(actual_item, classes.Item):
                message = 'You cannot pick that up.'
            elif isinstance(actual_item, classes.Container):
                message = 'It is bolted to the floor.'
            else:
                message = 'There has been an error in Room.get_item'
        elif inventory_check(item, player_inventory):
            message = 'You already have that.'
        elif (player_location.item_check('rat-like creature') 
                and (player_location.get_item('rat-like creature').held_item.name == item)):
            message = '''You reach to take the {} from the rat and it snaps at you threateningly. 
Looks like you will have to be trickier than that.'''.format(item)
        else:
            message = 'You do not see that in here.'
    ## dropping items: ##
    elif verb == 'drop':
        if inventory_check(item, player_inventory):
            actual_item = get_item_from_inventory(item, player_inventory)
            player_inventory.pop(actual_item.id)
            actual_item.change_loc('on the floor.')
            player_location.put_item(actual_item)
            message = 'Dropped.'
        else:
            message = 'You do not have that!'
    ## putting items: ##
    elif verb == 'put':
        message = attempt_put_in(input_list[1:], player_inventory, player_location)
    elif verb == 'unlock':
        message = attempt_unlock(item, player_location, player_inventory)
    elif verb == 'open' or verb == 'close':
        if item == 'door':
            message = 'All doors on this ship open and close automatically, as long as they are \nunlocked.'
        if player_location.item_check(item):
            actual_container = player_location.get_item(item)
            if isinstance(actual_container, classes.Container):
                message = actual_container.toggle_open()
            else:
                message = 'That cannot be opened or closed.'
        else:
            message = 'You do not see that in this room.'
    elif verb == 'use':
        message = attempt_use(item, player_inventory, player_location)
    else:
        message = 'Sorry, I do not yet know how to {}.'.format(verb) #shouldn't ever happen, but just in case. 
    return message
    
def normalize_action(input_text):
    action = ''
    input_text = input_text.split(' ', 1) #split input into 1st word, and then all the rest
    text = input_text[0] #grab 1st word
    if text == 'n':
        action = 'north'
    elif text == 'e':
        action = 'east'
    elif text == 's':
        action = 'south'
    elif text == 'w':
        action = 'west'
    elif text == 'u':
        action = 'up'
    elif text == 'd':
        action = 'down'
    elif text == 'h':
        action = 'help'
    elif text == 'i':
        action = 'inventory'
    elif text == 'q':
        action = 'quit'
    elif text == 'x':
        action = 'examine ' + input_text[1] #add the rest back on
    elif text == 'l':
        action = 'look'
    else:
        action = ' '.join(input_text)
    return action

def validate_direction(direction):
    return (direction == 'north' or direction == 'south' or direction == 'east'
            or direction == 'west' or direction == 'up' or direction == 'down')
    
def normalize_item(phrase, p_inven, loc):
    if phrase == 'key':
        locker_around = (inventory_check(locker_key.name, p_inven)
                         or loc.item_check(locker_key.name)
                         or rat_holding_item(loc, locker_key.name))
        card_around = (inventory_check(key_card.name, p_inven) 
                        or loc.item_check(key_card.name)
                        or rat_holding_item(loc, key_card.name))
        if locker_around:
            if card_around == False:
                item = locker_key.name
            else:
                print('\nAre you referring to the key card or the metal key?')
                key_type = input(PROMPT)
                item = key_type
        elif card_around:
            item = key_card.name
        else:
            item = phrase
    elif phrase == 'plant' and loc == LAB:
        item = alien_plant.name
    elif phrase == 'cabinet' and loc == SICK_BAY:
        item = MED_CABINET.name
    elif phrase == 'log' and loc == CAPT_QUARTERS:
        item = 'personal log'
    elif phrase == 'cheese':
        item = 'piece of cheese'
    elif (phrase == 'creature' or phrase == 'rat') and loc in MAZE:
        item = 'rat-like creature'
    else:
        item = phrase
    return item
    
def attempt_unlock(unlock_phrase, loc, p_inventory):
    words = unlock_phrase.split()
    if 'door' in words:
        obj_pos = words.index('door')
        if obj_pos >= 1:
            direction = words[obj_pos - 1]
        else:
            print('\nWhich door would you like to unlock?')
            direction = normalize_action(input(PROMPT))
            if loc == CARGO_BAY2 and direction == 'south':
                message = unlock_engine_room()
            else:
                key_phrase = get_key_phrase(words, p_inventory, 'door')
                if inventory_check(key_phrase, p_inventory):
                    key_item = get_item_from_inventory(key_phrase, p_inventory)
                    message = loc.unlock_path(direction, key_item)
                else:
                    message = 'That is not in your inventory.'
    elif 'port' in words and loc == CARGO_BAY2:
        message = unlock_engine_room()
    else:
        obj_word = words[0]
        if obj_word in loc.inventory.keys():
            obj_item = loc.inventory.get(obj_word)
            key_needed = obj_item.get_key_needed().name
            if inventory_check(key_needed, p_inventory):
                key_phrase = key_needed
            else:
                key_phrase = get_key_phrase(words, p_inventory, obj_word)
            key_item = get_item_from_inventory(key_phrase, p_inventory)
            if (not key_item is None) and obj_item.unlock(key_item):
                message = 'Apparently that is not the right key.'
            elif (not key_item is None) and not obj_item.unlock(key_item):
                message = 'You unlock the {} using the {}. '.format(obj_word, key_needed)
                message += obj_item.toggle_open()
            else:
                message = 'You do not have that in your inventory.'
        else:
            message = 'You do not see that in here.'
    return message

def unlock_engine_room():
    print('\nThe port leads to the engine room, but you cannot enter without the key code.')
    print('Would you like to try and enter in the code?')
    response = input(PROMPT)
    if response == 'yes' or response == 'y' or response == 'yes please':
        print('Please enter the code:')
        code_input = input(PROMPT).strip()
        message = CARGO_BAY2.unlock_path('south', code_input)
    else:
        message = 'Okay, maybe later.'
    return message

def attempt_put_in(words, p_inventory, p_loc):
    if 'in' in words:
        in_pos = words.index('in')
        item = ' '.join(words[:in_pos])
        container = ' '.join(words[in_pos+1:])
    else:
        item = ' '.join(words)
        print('\nWhat would you like to place {} inside?'.format(item))
        container = input(PROMPT).strip()
    actual_container = p_loc.inventory.get(container, None)
    if not inventory_check(item, p_inventory):
        message = 'You do not have that!'
    elif actual_container is None:
        message = 'You do not see that in here.'
    elif not isinstance(actual_container, classes.Container):
        message = 'You cannot place any items inside the {}.'.format(container)
    else:
        actual_item = get_item_from_inventory(item, p_inventory)
        p_inventory.pop(actual_item.id)
        actual_item.change_loc('resting on the shelf.')
        p_loc.put_item_in(actual_item, container)
        message = 'You place the {} inside the {}.'.format(item, container)
    return message

def attempt_use(phrase, inventory, loc):
    words = phrase.split()
    if 'on' in words:
        on_pos = words.index('on')
        item = ' '.join(words[:on_pos])
        subject = normalize_item(' '.join(words[on_pos+1:]), inventory, loc)
    else:
        item = ' '.join(words)
        print('\nWhat would you like to use the {} on?'.format(item))
        subject = normalize_item(input(PROMPT).strip(), inventory, loc)
    if not inventory_check(item, inventory):
        message = 'You do not have that in your inventory.'
    elif item == 'key card' or item == 'metal key':
        message = "If you are trying to use a key, please use the verb 'unlock' instead."
    elif item == 'key pad' and loc == CARGO_BAY2:
        message = unlock_engine_room()
    elif item == 'syringe':
        if subject == 'me':
            message = 'You really should not be trying to put yourself to sleep again.'
        elif subject == alien_plant.name and loc == LAB:
            loc.unlock_path('north', sedative)
            alien_plant.set_consumable_qualities('The plant is now sleeping soundly, allowing you to pass without fear.', 
                                                 'snoring softly in its pot.')
            message = alien_plant.get_description()
        elif not inventory_check(subject, inventory) and not subject in loc.inventory:
            message = 'You do not see that here.'
        else:
            message = 'You cannot figure out how to inject something into the {}.'.format(subject)
    elif item == 'machine part':
        if subject == 'engine' and loc == ENGINE_ROOM:
            message = 'Congratulations! You have beat stage one.'
        elif subject == 'me':
            message = 'That will not work because you are not a machine.'
        else:
            message = 'You cannot figure out how to connect the part to the {}.'.format(subject)
    else:
        message = 'You cannot figure out how to use that.'
    return message

def get_key_phrase(unlock_words, inventory, obj):
    found_with = False
    key_phrase = ''
    for word in unlock_words:
        if found_with:
            key_phrase += (word + ' ')
        if word == 'with':
            found_with = True
    if not found_with:
        print('What would you like to use to unlock the {}?\n'.format(obj))
        key_phrase = input(PROMPT)
    return key_phrase.strip()

def player_inventory_list(current_inventory):
    if not current_inventory:
        return 'You have nothing in your inventory.'
    else:
        message = 'You have:'
        cheese_count = 0
        for item_id in current_inventory:
            item = current_inventory.get(item_id)
            if item_id >= CHEESE_ID: 
                cheese_count += 1
            else:
                message += '\nA {}.'.format(item.name)
        if cheese_count > 0:
            message += '\n{} pieces of cheese.'.format(cheese_count)
        return message
    
def cheese_in_inventory(current_inventory):
    if not current_inventory:
        return False
    else:
        for item_id in current_inventory:
            if item_id >= CHEESE_ID:
                return True
        return False

def move_rat():
    for rat in RATS:
        current_place = rat.get_current_index()
        rat_room = MAZE[current_place]
        rat_room.remove_item('rat-like creature')
        next_place = rat.move()
        next_room = MAZE[next_place]
        next_room.put_item(rat)
        rat.drop_item(next_room)
        
def rat_thief(inv, rat):
    if cheese_in_inventory(inv):
        cheese = get_item_from_inventory(rat_food.name, inv)
        inv.pop(cheese.id)
        rat.steal_item(cheese)
    else:
        if inventory_check(machine_part.name, inv):
            item_name = machine_part.id
        elif inventory_check(sedative.name, inv):
            item_name = sedative.id
        elif inventory_check(capt_log.name, inv):
            item_name = capt_log.id
        elif inventory_check(key_card.name, inv):
            item_name = key_card.id
        elif inventory_check(locker_key.name, inv):
            item_name = locker_key.id
        else:
            item_name = ship_log.id
        if item_name in inv:
            the_item = inv.pop(item_name)
        else:
            the_item = None
        rat.steal_item(the_item, 'your')
    
def rat_holding_item(location, item_name):
    if location.item_check(RATS[0].name):
        the_rat = location.get_item(RATS[0].name)
        if the_rat.held_item.name == item_name:
            return True
    return False
    
def inventory_check(item_name, current_inventory):
    for item_id in current_inventory:
        the_item = current_inventory.get(item_id)
        if the_item.name == item_name:
            return True
    return False

def get_item_from_inventory(item_name, current_inventory):
    for item_id in current_inventory:
        the_item = current_inventory.get(item_id)
        if the_item.name == item_name:
            return the_item
    return None

################################################################
   ##------------------------------------------------------##   
################################################################

MAZE_LIST = [
        {'east':1}, 
        {'north':-1, 'west':0, 'east':2},
        {'west':1, 'east':3, 'south':8}, 
        {'west':2, 'south':9},
        {'east':5}, 
        {'west':4, 'south':11}, 
        {'east':7, 'south':12},
        {'west':6, 'east':8},
        {'north':2, 'west':7}, 
        {'north':3, 'east':10}, 
        {'west':9, 'east':11, 'south':16}, 
        {'north':5, 'west':10}, 
        {'north':6, 'south':18},
        {'east':14, 'south':19},
        {'west':13, 'south':20},
        {'east':16, 'south':21}, 
        {'north':10, 'west':15}, 
        {'south':23}, 
        {'north':12, 'east':19},
        {'north':13, 'west':18},
        {'north':14},
        {'north':15, 'east':22},
        {'west':21, 'east':23},
        {'north':17, 'west':22, 'south':-2}]

def maze_maker():
    rooms = []
    room_count = 0
    while room_count < len(MAZE_LIST):
        room = classes.Room('Maze of Boxes {}'.format(room_count))
        rooms.append(room)
        room_count += 1
    room_count = 0
    base_description = 'You are in a maze of storage crates, with boxes piled up to the ceiling.'
    while room_count < len(MAZE_LIST):
        room = rooms[room_count]
        exits = MAZE_LIST[room_count]
        for path in exits.keys():
            exit_index = exits.get(path)
            if exit_index >= 0:
                room.exits[path] = classes.Path(rooms[exit_index])
            elif exit_index == -1:
                room.exits['north'] = classes.Path(CARGO_BAY)
            else:
                room.exits['south'] = classes.Path(CARGO_BAY2)
        if len(exits.keys()) == 1:
            if 'north' in exits.keys():
                path = 'north'
            elif 'south' in exits.keys():
                path = 'south'
            else:
                path = 'east'
            exit_message = '\nYou have reached a dead end. The exit is to the {}.'.format(path)
        else:
            exit_message = '\nThere are exits to the ' + ', '.join(room.get_exit_list()) + '.'
        room.set_description(base_description + exit_message)
        room_count += 1
    return rooms

MAZE = maze_maker()
    
def load_part_one():
    ## ROOM DESCRIPTIONS:
    HALLWAY.set_description('''
You are in a long, dimly lit hallway, barren except for some helpful arrows
pointing down the hallway to the north and south, and one leading down a hatch 
directly in front of you. The door to the crew's quarters is off to the east.''')
    CAPT_QUARTERS.set_description('''
The Captain's quarters are relatively spartan in decor; there are a few
personal momentos on display, and there is a bed in the middle of the room. A
desk sits off to one side. The door to the Crew's quarters is to the south. 
There is another door leading west.''')
    CREW_QUARTERS.set_description('''
You are standing in the crew's quarters, next to your own bunk. There is an
open door to the west and a closed door to the north.''')
    BRIDGE.set_description('''
This is the ship's bridge. The control console is off, due to the lack of 
power. The viewport at the front of the room shows the ship's current 
surroundings: empty space. To the south, a door leads out to the hallway.
Another door lies to the east.''')
    CARGO_BAY.set_description('''
The large room is occupied by numerous piles of storage crates. You could
check to see what they contain, but you already know that they are empty.
The piles of crates have made it very difficult to navigate passage to the 
south side of the room, where the Engine Room should be. Off to the side, 
a ladder leads up to the main hallway.''')
    CARGO_BAY2.set_description('''
Having made it out of the maze of boxes, you can see a key pad on the south
wall, next to a port labeled ENGINE ROOM. To the east is a passage leading 
to the SHUTTLE BAY. ''')
    ENGINE_ROOM.set_description('''
The room is not small, and yet the dim light makes it feel even more cramped
and cluttered than it usually is. There are various tools laying about on the 
counters, but the center of the room is dominated by a large, dormant machine 
that you might guess to be the engine. The only exit is to the north.''')
    SHUTTLE_BAY.set_description('''
The bay is just large enough to fit two small emergency shuttles. You would 
know that one of those shuttles has already departed with the rest of your 
crew. The other shuttle is on the southern side of the room, but you cannot 
enter it. The exit to the west is labeled 'CARGO BAY'.''')
    SICK_BAY.set_description('''
The room is mostly bare, all the essentials having been taken earlier. The
exit lies to the west.''')
    MESS_REC.set_description('''
There is a dining table and six chairs set up near the western wall. Empty 
counters and cabinets line the walls in the kitchenette area to the southwest. 
A pile of bean bags and board games stands in the opposite corner. The hallway 
leads away to the north, and an unlabeled door is set into the eastern wall. 
Next to the door, a ladder leads up to the hatch in the ceiling.''')
    LAB.set_description('''
A long, narrow room stretches out to the north; the metal tables that line each 
side of the room constrict the path through. The tables are covered in 
scientific equipment of all kinds, most of which you cannot recognize. The 
exit hatch lies on the floor behind you.''')
    LAB2.set_description('''
The tables continue on this end of the room, although the equipment resembles
a mechanic's tools rather than those of a scientist. The exit lies to the south, 
past the sleeping alien plant. ''')
    ## EXIT PATHS:
    HALLWAY.set_exits({
            'east' : classes.Path(CREW_QUARTERS),
            'north' : classes.Path(BRIDGE),
            'south' : classes.Path(MESS_REC),
            'down' : classes.Path(CARGO_BAY)})
    CAPT_QUARTERS.set_exits({'south' : classes.Path(CREW_QUARTERS),
                             'west' : classes.Path(BRIDGE)})
    CREW_QUARTERS.set_exits({'west' : classes.Path(HALLWAY),
                             'north' : classes.Path(CAPT_QUARTERS)})
    BRIDGE.set_exits({'south' : classes.Path(HALLWAY),
                      'east' : classes.Path(CAPT_QUARTERS)})
    CARGO_BAY.set_exits({'south' : classes.Path(MAZE[1]),
                         'up' : classes.Path(HALLWAY)})
    CARGO_BAY2.set_exits({'east' : classes.Path(SHUTTLE_BAY),
                          'north' : classes.Path(MAZE[23]),
                          'south' : classes.Path(ENGINE_ROOM)})
    SHUTTLE_BAY.set_exits({'west' : classes.Path(CARGO_BAY),
                           'south' : classes.Path(SHUTTLE_INTERIOR)})
    ENGINE_ROOM.set_exits({'north' : classes.Path(CARGO_BAY)})
    MESS_REC.set_exits({'north' : classes.Path(HALLWAY),
                        'up' : classes.Path(LAB),
                        'east' : classes.Path(SICK_BAY)})
    SICK_BAY.set_exits({'west' : classes.Path(MESS_REC)})
    LAB.set_exits({'down' : classes.Path(MESS_REC),
                   'north' : classes.Path(LAB2)})
    LAB2.set_exits({'south' : classes.Path(LAB)})
    ## locking paths:
    CREW_QUARTERS.lock_exit_path('north', key_card) #locks the capt quarters
    BRIDGE.lock_exit_path('east', key_card) #locks capt quarters
    CARGO_BAY2.lock_exit_path('south', '42286') #locks the engine room
    LAB.lock_exit_path('north', sedative, 'blocked') #TODO another special lock?
    SHUTTLE_BAY.lock_exit_path('south', ship_log) #TODO this is also a special lock (shouldn't be unlocked yet)
    ## ITEM DESCRIPTIONS:
    key_card.set_consumable_qualities('It is just an ordinary plastic key card. On the back is written "CAPTAIN".', 
                                      'sitting on the shelf.')
    locker_key.set_consumable_qualities("It is a small, metal key, labeled '011'.", 
                                        'resting next to one of the place settings on the dining table.')
    capt_log.set_consumable_qualities("It is an old-fashioned portable tablet. The data in the tablet reads as follows:" + CAPT_LOG_WORDS,
                                      "laying on the Captain's desk.")
    key_code_pad.set_consumable_qualities('It is a key pad, locking the door to the engine room. It seems to require a 5-digit key code.',
                                          'on the wall, next to the south port.')
    machine_part.set_consumable_qualities('It is a gear of some sort, though the size and shape seem to indicate that it\nis part of the engine.',
                                          'on the table to the right.')
    alien_plant.set_consumable_qualities('''You were not aware that plants could have teeth, but this one is gnashing a 
nasty-looking set of teeth, drooling all over the floor. You clearly cannot pass 
without dealing with this creature.''',
                                         'in the middle of the floor, blocking the path to the north.')
    sedative.set_consumable_qualities('The label on the syringe indicates that it is a sedative strong enough to knock\nout a small horse.',
                                      'on the shelf, among a few other scattered bottles.')
    rat_food.set_consumable_qualities('It was the only food left on board, but the small chunk of cheese is so stinky\nthat only a rat would eat it.',
                                      'sitting on the counter in the kitchenette.')
    #TODO write the ships log, and other descriptive objects, i.e. beds, tools, etc.
    ## CONTAINER DESCRIPTIONS:
    CREW_LOCKER.set_description("It is an ordinary crew member's locker, labeled '011'.", 
                                'standing next to the bunk across from yours.')
    MED_CABINET.set_description('It is a regular cabinet with glass paneling on the doors; it used to hold medicines,\nuntil the rest of the crew left with the medical supplies.',
                                'standing against the east wall.')
    ## locking containers:
    CREW_LOCKER.lock_contents(locker_key)
    ## item placement inside containers:
    CREW_LOCKER.set_contents({key_card.name : key_card})
    MED_CABINET.set_contents({sedative.name : sedative})
    ## items inside rooms:
    CREW_QUARTERS.set_inventory({CREW_LOCKER.name : CREW_LOCKER})
    MESS_REC.set_inventory({locker_key.name : locker_key,
                            rat_food.name : rat_food})
    CAPT_QUARTERS.set_inventory({capt_log.name : capt_log})
    CARGO_BAY2.set_inventory({key_code_pad.name : key_code_pad})
    LAB2.set_inventory({machine_part.name : machine_part})
    LAB.set_inventory({alien_plant.name : alien_plant})
    SICK_BAY.set_inventory({MED_CABINET.name : MED_CABINET})
    for rat in RATS:
        room = MAZE[rat.get_current_index()]
        room.put_item(rat)
    
    
CAPT_LOG_WORDS = '''
Captain's log, 17.62.4201: 
    River and Aurora had another fight about sharing the lab today, something 
    about Aurora's newest 'pet' taking up too much space. So I told River she 
    could have more counter space in the engine room, and I let her program a 
    code-lock on the engine room door, although I'm sure I'll regret it later.
    She did tell me that the code was 42286.'''
#Easter egg note:
# 42286 is the Stardate mentioned at the beginning of 
#Star Trek: The Next Generation, episode "Elementary, Dear Data"
    
END_TEXT = '''
Luckily, you can easily spot where in the engine the missing part goes. As the 
engine whirs back to life, the lights around you flash on, brightening your
surroundings. With the ship's power restored, you can return to the bridge and 
assume control of the ship.'''
    
#########################################################################

if __name__ == '__main__':
    play()