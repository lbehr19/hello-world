# -*- coding: utf-8 -*-
"""
Created on Sat Mar  9 17:09:38 2019

@author: leahb
"""

import os.path
import sqlite3

#import conf_scraper as cs

#import http.client as httplib
host = "www.isfdb.org"


PROMPT = '+=> '

# This is an old REPL created to easily browse the database and execute SQL, before
# I had DB Browser for SQLite installed. 
def db_browser(current_db_name):
    db = sqlite3.connect(current_db_name)
    print("Welcome to the book database!")
    print("**only rudimentary functions implemented - no internet linking.\n")
    command = input(PROMPT)
    while command != 'q' and command != 'quit':
        if command == 'add':
            print("What do you want to add? 'tags' or 'book'?")
            option = input(PROMPT)
            if option == 'tags':
                print("Give the title and author of the book you want to add tags to, separated by commas.")
                (book_title, book_author) = input(PROMPT).split(',')
                print("Now give the list of tags you would like to add, separated by commas.")
                tags = input(PROMPT).split(',')
                title = book_title.strip()
                author = book_author.strip()
                new_tags(db, title, author, tags)
            else:
                print("Sorry, so far you can only insert tags.")
        elif command == 'show':
            print("Would you like to see Books, Series, or Tags?")
            option = input(PROMPT)
            if option == 'Books':
                display_books(db)
            elif option == 'Series':
                display_series(db)
            elif option == 'Tags':
                display_tags(db)
            else:
                print("Invalid option: " + option)
        elif command == 'check':
            print('Checking all books to see if they are contained in the ISFDB...')
            #check_book(db)
        elif command == 'execute':
            print('Please type in the query you would like to execute.')
            print(print_final(db, input(PROMPT)))
        elif command == 'reset':
            print("This will delete the current database and create a totally new one.")
            print("It will not save any of your tags or added books.")
            print("Are you sure you want to do this?")
            response = input(PROMPT)
            if response == 'yes' or response == 'y':
                empty_db(db)
                make_database(current_db_name, 'books')
                db = sqlite3.connect(current_db_name)
        else:
            print("Sorry, I can't do anything else right now.")
        command = input(PROMPT)
    db.close()
    
def print_final(db, query):
    cursor = db.cursor()
    cursor.execute(query)
    tup = cursor.fetchone()
    output = ''
    while not tup is None:
        i = 0
        while i < len(tup):
            data = tup[i]
            output += (str(data) + ', ')
            i += 1
        output += '\n'
        tup = cursor.fetchone()
    cursor.close()
    return output

#=============================================================================#

def new_tags(db, book_title, book_author, tags):
    c = db.cursor()
    book_isbn = find_isbn(db, book_title, book_author)
    values = {'i': book_isbn, 'b': book_title, 'a': book_author}
    q = "INSERT INTO Tags VALUES (:i, :b, :a, :t)"
    try:
        for tag in tags:
            values['t'] = tag.strip()
            c.execute(q, values)
        print("new tags added to %s, by %s: " % (book_title, book_author))
        print(tags)
        db.commit()
    except sqlite3.OperationalError as o:
        print("Insertion failed: ", o)
    c.close()
    
def display_books(db):
    print('book display')
    c = db.cursor()
    q = "SELECT * FROM Books"
    c.execute(q)
    for tup in c:
        display_one_book(tup)
    c.close()
    
def display_one_book(book_tup):
    title = book_tup[1]
    author = book_tup[2]
    genre = book_tup[4]
    year_read = book_tup[3]
    print("%s by %s, %s. Last read: appx. %4d." % (title, author, genre, year_read))
    
def find_by_isbn(db, isbn):
    c = db.cursor()
    q = "SELECT * FROM Books WHERE isbn=:i"
    c.execute(q, {'i':isbn})
    book = c.fetchone()
    title = book[1]
    author = book[2]
    c.close()
    return (title, author)

def display_series(db):
    print('series display')
    c = db.cursor()
    q = "SELECT name FROM Series GROUP BY name"
    c.execute(q)
#    for tup in c:
#        print(tup)
    tups = c.fetchall()
    for tup in tups:
        series = tup[0]
        print("%s series:" % series.strip())
        q = "SELECT * FROM Series WHERE name=:n"
        #TODO: can't execute this here, need to find a different way to figure this out. EDIT: maybe fixed?
        c.execute(q, {'n':series})
        for book in c:
            isbn = book[1]
            position = book[2]
            title = find_by_isbn(db, isbn)[0]
            print("\t#%d: %s" % (position, title))
    c.close()
    
def display_tags(db):
    print('tags display')                 
    
def find_isbn(db, title, author):
    c = db.cursor()
    q = "SELECT isbn FROM Books WHERE title=:t AND author=:a"
    c.execute(q, {'t':title, 'a':author})
    isbn = c.fetchone()[0]
    return isbn

# Outdated functions, created to use the ISFDB's provided API:

#def check_book(db):
#    c = db.cursor()
#    q = "SELECT * FROM Books WHERE author='Brandon Sanderson'"
#    # should find at least one book by Brandon Sanderson in isfdb
#    c.execute(q)
#    book1 = c.fetchone()
#    xml1 = getXML(book1[0])
#    parseXML_string(xml1, book1)
#    q = "SELECT * FROM Books"
#    # should not be able to find The Hobbit in isfdb
#    c.execute(q)
#    book2 = c.fetchone()
#    xml2 = getXML(book2[0])
#    parseXML_string(xml2, book2)
#    c.close()
    

#def getXML(isbn):
#    webservice = httplib.HTTPConnection(host)
#    command = '/cgi-bin/rest/getpub.cgi?%s' % isbn
#    webservice.putrequest("GET", command)
#    webservice.putheader("Host", host)
#    webservice.putheader("User-Agent", "Wget/1.9+cvs-stable (Red Hat modified)")
#    webservice.endheaders()
#    resp = webservice.getresponse()
#    errcode = resp.status
#    errmsg = resp.reason
#    webservice.close()
#    if errcode != 200:
#        #resp = webservice.getfile()
#        print("Error:", errmsg)
#        print("Resp:", resp.read())
#        resp.close()
#        return ''
#    else:
#        #resp = webservice.getfile()
#        raw = resp.read()
#        resp.close()
#        text = bytes_to_string(raw)
#        #index = raw.find('<?xml')
#        #return raw[index:]
#        return text
    
def bytes_to_string(b):
    return "".join(chr(byte) for byte in b)

def parseXML_string(text, book):
    assert len(text) > 0, "Error occured in fetching through API."
    title = book[1]
    author = book[2]
    #start out by finding how many records matched the search by isbn
    start = text.find('<Records>') + 9
    stop = text.find('</Records>')
    recs = int(text[start:stop])
    if recs == 0:
        print("No match for %s by %s in the ISFDB." % (title, author))
    else:
        rec_title = parse_element(text, 'Title')
        rec_author = parse_element(text, 'Author')
        print("Matching record found for %s by %s:" % (title, author))
        print("\t\t%s by %s" % (rec_title, rec_author))
        
def parse_element(text, e_name):
    start_key = '<' + e_name + '>'
    stop_key = '</' + e_name + '>'
    start_index = text.find(start_key) + len(start_key)
    stop_index = text.find(stop_key)
    return text[start_index:stop_index]

#=============================================================================#
#TODO move to a different file?

def read_table(table_name):
    filename = table_name + '.csv'
    if os.path.isfile(filename):
        f = open(filename)
        data = f.read()
        f.close()
        data = data.strip()
        lines = data.split('\n')
        if len(lines) > 0:
            headers = lines[0]
            field_names = headers.split(', ')
            n_fields = len(field_names)
            records = []
            for line in lines[1:]:
                line_data = line.split(', ')
                if len(line_data) == n_fields:
                    records.append(line_data)
                else:
                    raise TableReadError('inconsitent field count')
            return (field_names, records)
        else:
            raise TableReadError('empty file')
    else:
        raise TableReadError('cannot open file for reading')

class TableReadError(Exception):
    def __init__(self, message):
        super().__init__(message)
        

def make_book_table(db, csv_name):
    c = db.cursor()
    table_start = read_table(csv_name)
    #build book table
    #TODO fix this before running the query!
    q = """CREATE TABLE IF NOT EXISTS Books (
    isbn INTEGER NOT NULL,
    title CHARACTER VARYING(50),
    author CHARACTER VARYING(50),
    year_read INTEGER NOT NULL,
    genre CHARACTER VARYING(50),
    PRIMARY KEY(isbn)
    )"""
    c.execute(q)
    #build series table
    q = """CREATE TABLE IF NOT EXISTS Series (
    name CHARACTER VARYING(50) NOT NULL,
    book_isbn INTEGER NOT NULL,
    position INTEGER,
    PRIMARY KEY (name, book_isbn),
    FOREIGN KEY (book_isbn) REFERENCES Books(isbn)
    )"""
    c.execute(q)
    #pull records from csv and insert
    isbn = table_start[0].index('isbn')
    title = table_start[0].index('book_title')
    author = table_start[0].index('author')
    year = table_start[0].index('year_read')
    genre = table_start[0].index('book_genre')
    series_name = table_start[0].index('series_name')
    series_pos = table_start[0].index('series_pos')
    q = "INSERT INTO Books VALUES (:i, :t, :a, :y, :g)"
    q2 = "INSERT INTO Series VALUES (:n, :i, :p)"
    for record in table_start[1]:
        values = {'i': record[isbn], 't':record[title], 'a':record[author], 'y':record[year], 'g':record[genre]}
        c.execute(q, values)
        if not (record[series_name] == ''):
            val2 = {'n':record[series_name], 'i':record[isbn], 'p':record[series_pos]}
            c.execute(q2, val2)
        else:
            print('book found with no series: %s' % record[title])
    print("records added to database: %d" % len(table_start[1]))
    db.commit()
    c.close()

def create_tag_table(db):
    c = db.cursor()
    q = """
    CREATE TABLE IF NOT EXISTS Tags (
    book_isbn INTEGER NOT NULL,
    book_title CHARACTER VARYING(50) NOT NULL,
    book_author CHARACTER VARYING(50) NOT NULL,
    tag CHARACTER VARYING(50) NOT NULL,
    PRIMARY KEY (book_isbn, tag),
    FOREIGN KEY (book_isbn, book_title, book_author) REFERENCES Books(isbn, title, author)
    )"""
    c.execute(q)
    print("Tag table initialized and ready for input")
    db.commit()
    c.close()
    
def make_database(db_name, book_csv):
    db = sqlite3.connect(db_name)
    make_book_table(db, book_csv)
    create_tag_table(db)
    db.close()

def empty_db(db):
    c = db.cursor()
    c.execute("DROP TABLE Tags")
    c.execute("DROP TABLE Series")
    c.execute("DROP TABLE Books")
    c.close()