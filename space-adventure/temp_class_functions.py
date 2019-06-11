# -*- coding: utf-8 -*-
"""
Created on Fri Nov  9 17:59:04 2018

@author: leahb
"""
#for Room class
def set_description(self, room_description):
        self.description = room_description
    def get_loc_description(self):
        message = self.name + '\n'
        message += self.description + '\n'
        for item_key in self.inventory.keys():
            the_item = self.inventory.get(item_key)
            message += the_item.get_basic_desc()
        return message
#for Container class
def set_description(self, description, location):
        self.description = description
        self.basic_desc = self.name + ' ' + location
def get_basic_desc(self):
    message = self.basic_desc
    if self.is_open:
        message += ' It is open.'
        if not self.contents:
            message += ' There is nothing inside.\n'
        else:
            message += ' Inside, you see: \n'
            for item in self.contents:
                message += (item + '\n')
    else:
        message += ' It is closed.\n'
    return message
#for Item class
def set_consumable_qualities(self, item_details, location):
    self.description = item_details
    self.basic_desc = self.name + ' ' + location
def get_description(self):
    return self.description
def get_basic_desc(self):
    return self.basic_desc

