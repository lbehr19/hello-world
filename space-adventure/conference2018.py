# -*- coding: utf-8 -*-
"""
Created on Tue Oct 16 15:37:49 2018

@author: leahb
"""

def play():
    """ Plays the game """
    welcome()
    
    
def welcome():
    print('Welcome to the game!')
    if request_yes('Would you like to see instructions?'):
        print(HELP)
        
def request_yes(prompt):
    """Returns True if user, given string prompt, inputs "yes" or
    "y" (case insensitive). Returns False if user enters "no" (or
    "n", case insensitive). Repeats the request for user input
    until a valid yes or no is supplied.
    """
    keep_asking = None
    while keep_asking is None:
        user_in = input(prompt).lower()
        if user_in == 'yes' or user_in == 'y':
            keep_asking = True
        elif user_in == 'no' or user_in == 'n':
            keep_asking = False
        else:
            print('Enter "yes" or "no".')
    return keep_asking

HELP = '''
The basic moves are (n)orth, (s)outh, (e)ast and (w)est. 
Use these to move around. Other possible instructions
include TAKE, (L)OOK, E(X)AMINE, DROP, etc. '''