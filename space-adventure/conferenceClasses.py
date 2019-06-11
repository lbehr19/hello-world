# -*- coding: utf-8 -*-
"""
Created on Fri Oct 19 15:36:39 2018

@author: leahb
"""

#class Counter(object):
#    #to use constructor: c = Count
#    def __init__(self):
#        self.count = 0 # this is where to define member vars
#        self.inventory = [] #this would be where items and stuff go. 
#        self.exits = {
#                'north' : otherRoom, #stuff like that. 
#                'up' : upRoom
#                }
#        #make a graph to represent how objects and items interact with each other. 
#    def reset(self): #to use: c.reset()
#        self.count = 0
#    def increment(self):
#        self.count += 1
#    def set_value(self, val): #c.set_value(42)
#        self.count = val
#    #setters and getters aren't necessary. We can access the member variables, i.e. c.count. 
#    # this can be a problem; we can also create new instance variables i.e. c.coun
#    def get_value(self):
#        return self.count
    
    
class Item(object):
    def __init__(self, item_name):
        self.name = item_name
        self.description = 'Item description unfinished.'
        self.can_take = False
    def set_consumable_qualities(self, item_details, can_hold):
        self.description = item_details
        self.can_take = can_hold
    ##other methods to be included for making containers (or else use inheritence?)

### Location objects: must contain at least one direction; must also contain 'objects'
class Room(object):
    def __init__(self, room_title):
        self.name = room_title
        self.description = 'Unfinished area. Please go back the way you came.'
        self.inventory = {} #dictionary of string names to objects
        ##EXITS: a dictionary of String keys representing path names to 
        ##       a list, where 1st item is room at the end of path,
        ##       2nd item is boolean indicating locked/unlocked door,
        ##       3rd item is object required to unlock path.
        self.exits = {}
    def set_description(self, room_description):
        self.description = room_description
    def get_loc_description(self):
        message = self.name + '\n'
        message += self.description + '\n'
        for item_key in self.inventory.keys():
            item = self.inventory.get(item_key)
            if not item.can_take():
                message += ('A ' + item_key + ' is on the floor.')
            else:
                message += item.get_description()
            message += '\n'
        return message
    def unlock_path(self, path, key_item):
        """path is the direction that is being unlocked, i.e. north or up. (given as string)
        key_item is the actual object that is being used to unlock the path."""
        current_path_dest = self.exits.get(path, None)
        if current_path_dest is None:
            message = 'There is no exit that way.\n'
        elif current_path_dest[1] == True and current_path_dest[2] == key_item:
            message = 'The door to the ' + path + ' has been unlocked.\n'
            current_path_dest[1] = False
            current_path_dest[2] = None
        else:
            if not current_path_dest[1]:
                message = 'That exit is already unlocked!\n'
            else:
                message = 'You cannot unlock that door with ' + key_item.name + '.\n'
        return message
    def item_check(self, requested_item):
        return requested_item in self.inventory
    def set_exits(self, path_dictionary):
        self.exits = path_dictionary
    def get_exit_list(self):
        return self.exits.keys()
    def set_inventory(self, item_dictionary):
        self.inventory = item_dictionary
    def get_item(self, item_key):
        if item_key in self.inventory.keys():
            item_obj = self.inventory.pop(item_key)
            if item_obj.can_take:
                return item_obj
            else:
                self.inventory[item_key] = item_obj
                return None ##FIND SOMETHING ELSE TO RETURN so that the player knows why it can't be taken.
        else: 
            return None
    def put_item(self, item):
        item_name = item.name
        self.inventory[item_name] = item