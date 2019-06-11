# -*- coding: utf-8 -*-
"""
Created on Fri Oct 19 15:36:39 2018

@author: leahb
"""
import random

class Item(object):
    def __init__(self, item_name, item_id, can_take=True):
        self.name = item_name
        self.id = item_id
        self.description = 'Item description unfinished.'
        self.is_portable = can_take
    def set_consumable_qualities(self, item_details, location):
        self.description = item_details
        self.basic_desc = 'A ' + self.name + ' is ' + location
    def get_description(self):
        return self.description
    def get_basic_desc(self):
        return self.basic_desc
    def set_item_id(self, id_num):
        self.id = id_num
    def change_loc(self, location):
        self.basic_desc = 'A ' + self.name + ' is ' + location
    
class Container(object):
    def __init__(self, container_name, start_open=False):
        self.name = container_name
        self.description = 'Container description unfinished.'
        self.is_open = start_open
        self.is_locked = False
        self.key_needed = None
        self.contents = {}
    def set_contents(self, contents):
        self.contents = contents
    def set_description(self, description, location):
        self.description = description
        self.basic_desc = 'A ' + self.name + ' is ' + location
    def get_description(self):
        message = self.description + ' '
        if self.is_open:
            message += ('\n' + self.get_content_desc())
        return message
    def get_basic_desc(self):
        message = self.basic_desc
        if self.is_open:
            message += '\nIt is open. '
            message += self.get_content_desc()
        else:
            message += '\nIt is closed. '
        return message
    def get_content_desc(self):
        message = ''
        if not self.contents:
            message = 'There is nothing inside.'
        else:
            message = 'Inside, you see:\n'
            content_desc = []
            for key_name in self.contents.keys():
                content_desc.append(self.contents.get(key_name).get_basic_desc())
            message += '\n'.join(content_desc)
        return message
    def toggle_open(self):
        if not self.is_locked:
            if self.is_open:
                self.is_open = False
                message = 'You close the {}. '.format(self.name)
            else:
                self.is_open = True
                message = 'You open the {}. '.format(self.name)
                message += self.get_content_desc()
        else:
            message = 'The {} is locked.'.format(self.name)
        return message
    def check_contents(self, requested_item):
        if self.is_open:
            return requested_item in self.contents
        else:
            return False
    def lock_contents(self, key_item):
        self.is_locked = True
        self.key_needed = key_item
    def get_key_needed(self):
        if self.is_locked:
            return self.key_needed
        else:
            return None
    def unlock(self, key_item):
        if key_item == self.key_needed:
            self.is_locked = False
        return self.is_locked
    
class Path(object):
    def __init__(self, end_room):
        self.destination = end_room
        self.is_locked = False
        self.key_obj = None
    def set_locked(self, key, stopper):
        self.is_locked = True
        self.lock_phrase = stopper
        self.key_obj = key
    def get_key(self):
        return self.key_obj
    def unlock(self, key):
        if key == self.key_obj:
            self.is_locked = False
        return self.is_locked

### Location objects: must contain at least one direction; must also contain 'objects'
class Room(object):
    def __init__(self, room_title):
        self.name = room_title
        self.description = 'Unfinished area. Please go back the way you came.'
        self.inventory = {} #dictionary of string names to objects
        ##EXITS: a dictionary of String keys representing path names to Path objects
        self.exits = {}
    def set_description(self, room_description):
        self.description = room_description
    def get_loc_description(self):
        message = self.name + '\n'
        message += self.description
        if not self.inventory is None:
            for item_key in self.inventory.keys():
                message += '\n'
                the_item = self.inventory.get(item_key)
                message += the_item.get_basic_desc()
        return message
    def lock_exit_path(self, direction, key_needed, stop_phrase="locked"):
        if direction in self.exits:
            self.exits.get(direction).set_locked(key_needed, stop_phrase)
    def unlock_path(self, path, key_item):
        """path is the direction that is being unlocked, i.e. north or up. (given as string)
        key_item is the actual object that is being used to unlock the path."""
        current_path_dest = self.exits.get(path, None)
        if current_path_dest is None:
            message = 'There is no exit that way.'
        elif current_path_dest.is_locked:
            if current_path_dest.unlock(key_item):
                message = 'That is not the right key.'
            else:
                message = 'The door to the {} is unlocked.'.format(path)
        else:
            message = 'That door is already unlocked.'
        return message
    def item_check(self, requested_item):
        """Returns true if the requested item (given as a string) is in the location's inventory."""
        for item_name in self.inventory:
            actual_item = self.inventory.get(item_name)
            if isinstance(actual_item, Container) and actual_item.check_contents(requested_item):
                return True
            elif item_name == requested_item:
                return True
        return False
    def set_exits(self, path_dictionary):
        self.exits = path_dictionary
    def get_exit_list(self):
        return self.exits.keys()
    def set_inventory(self, item_dictionary):
        self.inventory = item_dictionary
    def get_item(self, item_key):
        for item_name in self.inventory:
            actual_item = self.inventory.get(item_name)
            if isinstance(actual_item, Container) and actual_item.check_contents(item_key):
                return actual_item.contents.get(item_key)
            elif item_name == item_key:
                return actual_item
        return None
    def remove_item(self, item_key):
        if item_key in self.inventory:
            self.inventory.pop(item_key)
        else:
            for item in self.inventory:
                the_item = self.inventory.get(item)
                if isinstance(the_item, Container) and the_item.check_contents(item_key):
                    the_item.contents.pop(item_key)
                    
    def put_item(self, item):
        item_name = item.name
        self.inventory[item_name] = item
    def put_item_in(self, item, container_name):
        item_name = item.name
        container = self.inventory.get(container_name)
        container.contents[item_name] = item
        
RAT_DESCRIPTION = 'The creature is small and covered in shiny green scales. It appears\nto be very hungry.'

class Rat(Item):
    def __init__(self, start_index):
        self.is_portable = False
        self.max_x = 6
        self.max_y = 4
        self.maze_pos = (start_index%self.max_x, start_index//self.max_x)
        self.name = 'rat-like creature'
        self.set_consumable_qualities(RAT_DESCRIPTION, 'snarling at you from the corner.')
        self.held_item = None
        self.item_possession = 'a'
    def get_current_index(self):
        current_x = self.maze_pos[0]
        current_y = self.maze_pos[1]
        return (current_y * self.max_x) + current_x
    def move(self):
        """randomly determines the direction that the rat should go"""
        move = random.randrange(0,4)
        dx = 0
        dy = 0
        if move == 0:
            dy = -1
        elif move == 1:
            dx = 1
        elif move == 2:
            dy = 1
        else:
            dx = -1
        next_row = self.maze_pos[1] + dy
        next_col = self.maze_pos[0] + dx
        valid_move = ((next_row >= 0 and next_row < self.max_y) and
                          (next_col >= 0 and next_col < self.max_x))
        if valid_move:
            self.maze_pos = (next_col, next_row)
            return ((next_row * self.max_x) + next_col)
        else:
            return self.get_current_index()
    def steal_item(self, item, whose_is_it='a'):
        if self.held_item is None and not item is None:
            self.held_item = item
            self.item_possession = whose_is_it
    def get_basic_desc(self):
        if self.held_item is None:
            return self.basic_desc
        else:\
            return self.basic_desc + '\nIt is holding {} {}.'.format(self.item_possession, self.held_item.name)
    def drop_item(self, room):
        if (not self.held_item is None) and self.held_item.name != 'piece of cheese':
            self.held_item.change_loc('on the floor.')
            room.put_item(self.held_item)
            self.held_item = None
        