# -*- coding: utf-8 -*-
"""
Created on Sat May  4 18:07:59 2019

@author: leahb
"""
import sqlite3
import conf_scraper as cs


WELCOME = """
Welcome to Leah's Library! I can supply you with recommendations based on
books that Leah has read recently which are located in the ISFDB.
Your options are as follows: """
MENU = """
    1. Add/update a new/recently read book.
    2. Get a recommendation based on tags. 
    3. Get a recommendation based on series. 
    4. See Leah's reading statistics.
    5. Compare a book entry with the ISFDB.
    6. Add a recommendation from a friend/other source.
    7. Give me a book to read.
    To exit: Enter q. 
"""
SEPARATOR = '--------------------------------------------------------'
PROMPT = '#> '

MIN_RECS = 100

def main_repl():
    db = sqlite3.connect('cf2.db')
    print(WELCOME)
    print(MENU)
    response = input(PROMPT)
    while response != 'q':
        if response == '1':
            print("Would you like to *add* a new book, or *update* an existing one?")
            answer = input(PROMPT).lower()
            if answer == 'add':
                add_to_db(db)
            elif answer == 'update':
                update_db(db)
        elif response == '2':
            tag_recommend(db)
        elif response == '3':
            series_recommend(db)
        elif response == '4':
            show_stats(db)
        elif response == '5':
            check_with_isfdb(db)
        elif response == '6':
            add_friend_recommend(db)
        elif response == '7':
            generate_random_rec(db)
        else:
            print("Sorry, that's not an option I recognize. Try again.")
        print(SEPARATOR)
        print(MENU)
        response = input(PROMPT)
    print("Goodbye!")
    cs.close_connections()
    db.close()
    
def confirm(message):
    print(message)
    answer = input(PROMPT).lower()
    while True:
        if answer == 'y' or answer == 'yes':
            return True
        elif answer == 'n' or answer == 'no':
            return False
        else:
            print("Please enter 'yes' or 'no'.")
            answer = input(PROMPT).lower()
            
def format_recs(recs, tag):
    final = ''
    for rec in recs:
        title = rec[0]
        author = rec[1]
        final += '\n%s, by %s (recommended because of your interest in %s)' % (title, author, tag)
    return final + '\n'
    
def add_to_db(db):
    attempting_add = True
    while attempting_add:
        print("Please enter the following information in the order shown, separated by commas:")
        print("Title, author, isbn, year read, genre.")
        info = input(PROMPT).split(',')
        if len(info) > 4:
            title = info[0].strip()
            author = info[1].strip()
            isbn = int(info[2].strip())
            year_read = int(info[3].strip())
            genre = info[4].strip()
            message = "Please confirm that you would like to add the following to the database: %s by %s, read %d" % (title, author, year_read)
            if confirm(message):
                cursor = db.cursor()
                cursor.execute("INSERT INTO Books VALUES (?,?,?,?,?)", (isbn, title, author, year_read, genre))
                print("Insertion successful.")
                attempting_add = False
                cursor.execute("SELECT id FROM Recs WHERE title=? AND author=?", (title, author))
                result = cursor.fetchone()
                if not result is None:
                    cursor.execute("DELETE FROM Recs WHERE id=?", (result[0],))
                    cursor.execute("SELECT count(*) FROM Recs")
                    recs_left = cursor.fetchone()[0] #assumes that Recs includes at least one record, otherwise we'd have a None error
                    if recs_left < MIN_RECS:
                        cursor.close()
                        print("The database needs to add new recommendations.")
                        check_author = confirm("Would you like to restrict new recommendations to only include authors that you have already read?")
                        cs.refresh_tag_recommends(75, check_author)
                        cs.refresh_series_recommends(75)
                db.commit()
                cursor.close()
                add_tags(db, title, author, isbn)
                add_series(db, isbn)
        else:
            attempting_add = not confirm("Incorrect number of values entered. Did you mean to cancel?")

def add_series(db, isbn):
    start_loop = confirm("Would you like to add series information for your new book?")
    while start_loop:
        print("Please enter the series name and the position of the book, separated by a comma:")
        info = input(PROMPT).split(',')
        if len(info) > 1:
            name = info[0].strip()
            pos = int(info[1].strip())
            message = "Please confirm the information: Series %s, position %d." % (name, pos)
            if confirm(message):
                cursor = db.cursor()
                cursor.execute("INSERT INTO Series VALUES (?,?,?)", (name, isbn, pos))
                print("Insertion successful.")
                db.commit()
                cursor.close()

def update_db(db):
    print("Please enter the title and author of the work you would like to update, comma separated.")
    info = input(PROMPT).split(',')
    title = info[0].strip()
    author = info[1].strip()
    cursor = db.cursor()
    find_q = "SELECT * FROM Books WHERE title=? AND author=?"
    cursor.execute(find_q, (title, author))
    book = cursor.fetchone()
    if not book is None:
        isbn = book[0]
        print("Book found: %s by %s (%s); isbn: %d; last read: %d" % (book[1], book[2], book[4], isbn, book[3]))
        if confirm("Is this the book you would like to update?"):
            print("Please enter the updated year:")
            info = int(input(PROMPT))
            cursor.execute("UPDATE Books SET year_read=? WHERE isbn=?", (info, isbn))
            print("Update successful.")
            db.commit()
            cursor.close()
        else:
            print("Update action canceled.")
    else:
        print("Book not found. Update action canceled.")
        
def add_tags(db, title, author, isbn):
    isfdb_record = cs.find_my_book(title, False)
    if not isfdb_record is None:
        try:
            cursor = db.cursor()
            insert_q = "INSERT INTO Tags VALUES (?,?,?,?)"
            tags = isfdb_record['tags']
            for tag in tags:
                cursor.execute(insert_q, (isbn, title, author, tag))
            print("Tags added from ISFDB.")
            db.commit()
            cursor.close()
        except sqlite3.OperationalError as o: #catch accidental duplicates?
            print('Tags could not be added:', o)
    else:
        print("Title %s was not found in the ISFDB." % title)
    
def get_recs_by_tag(db, tag, num_recs=5, min_id=-1):
    cursor = db.cursor()
    cursor.execute("SELECT title, author, id FROM Recs WHERE tag=? ORDER BY id", (tag,))
    final_list = []
    i = 0
    while i < num_recs:
        rec = cursor.fetchone()
        if not rec is None and rec[2] > min_id:
            final_list.append((rec[0], rec[1], rec[2]))
            i += 1
        elif rec is None:
            i = num_recs
    cursor.close()
    return final_list
    
def get_any_recs(db, num_recs=5):
    cursor = db.cursor()
    cursor.execute("SELECT * FROM Recs")
    final_list = []
    for _ in range(num_recs):
        rec = cursor.fetchone()
        final_list.append((rec[0], rec[1], rec[2]))
    cursor.close()
    return final_list

def get_recs_by_id(db, start_id, num_recs):
    cursor = db.cursor()
    cursor.execute("SELECT * FROM Recs WHERE id>?", (start_id,))
    final_list = []
    i = 0
    rec = cursor.fetchone()
    while i < num_recs and not rec is None:
        final_list.append((rec[0], rec[1], rec[2]))
        i += 1
        rec = cursor.fetchone()
    cursor.close()
    return final_list

def tag_recommend(db):
    print("What kind of tag are you in the mood to read?")
    answer = input(PROMPT)
    results = get_recs_by_tag(db, answer)
    if not results:
        print("We don't have any recommendations with that tag in our database.")
        c = db.cursor()
        current_max_id = c.execute("SELECT max(id) FROM Recs").fetchone()[0]
        c.close()
        num_of_new_tags = cs.search_and_add_isfdb_tags(answer, 20, True, False)
        other_ideas = get_recs_by_tag(db, answer)
        if num_of_new_tags > 0:
            print("We've found some books based on that tag in the ISFDB:", 
                  format_recs(other_ideas, answer))
            if not confirm("Would you like to add these and other recommendations to our database?"):
                c = db.cursor()
                c.execute("DELETE FROM Recs WHERE id>?", (current_max_id,))
                c.close()
                print("Okay, returning to menu...")
            else:
                print("Okay, %d new recommendations have been added!" % num_of_new_tags)
        else:
            print("We couldn't find any equivalent tags in the ISFDB either. Please try something else.")
    else:
        print("Here are a few recommendations we've found:", format_recs(results, answer))
        keep_reccing = confirm("Please indicate if you would like to see more recommendations:")
        while keep_reccing:
            max_rec_id = results[-1][-1] #get last rec from list, then id as last value in rec
            results = get_recs_by_tag(db, answer, min_id=max_rec_id)
            if not results:
                print("Sorry, we don't have any more recommendations under that tag. Try again with a different tag.")
                keep_reccing = False
            else:
                print("Alright, here are a few more:", format_recs(results, answer))
                keep_reccing = confirm("Please indicate if you would like to see more recommendations:")
        print("Enjoy!")
        
def series_recommend(db):
    c = db.cursor()
    q = "SELECT name, year_read, title, author FROM Books NATURAL JOIN (SELECT name, book_isbn as isbn, position FROM Series) GROUP BY year_read ORDER BY year_read DESC"
    c.execute(q)
    success = False
    try_again = True
    while try_again:
        print("Here are the 3 most recent series that you've read from: ")
        results = []
        while len(results) < 3:
            next_series = c.fetchone()
            name = next_series[0]
            if not name in results:
                results.append(next_series[0])
                year = next_series[1]
                title = next_series[2]
                author = next_series[3]
                print("%s, read from in %d (%s by %s)" % (name, year, title, author))
        print("\nWe'll search our recommendations to find if there are any books in the same series.")
        c2 = db.cursor()
        final_recs = []
        display = ''
        for series in results:
            c2.execute("SELECT title, author FROM Recs WHERE tag=?", (series,))
            result = c2.fetchone()
            while not result is None and len(final_recs) < 5:
                final_recs.append(result)
                display += '\n%s by %s (from %s)' % (result[0], result[1], series)
                result = c2.fetchone()
        print("Here's what we've found:", display+'\n')
        if display == '':
            print("Sorry, those series don't have any more books listed in our database.")
        if len(final_recs) < 5:
            try_again = confirm("Would you like to try the next 3 most recent series for more?")
        else:
            try_again = False
            success = True
        c2.close()
    if success:
        print("Enjoy!")
    else:
        print("Sorry. Maybe next time.")
    c.close()
    
    
def check_with_isfdb(db):
    print("Please input the title of the book you would like to compare with the ISFDB:")
    answer = input(PROMPT).strip()
    isfdb_data = cs.find_my_book(answer)
    if isfdb_data is None:
        return
    else:
        print("Here is the information kept in Leah's database:")
        c = db.cursor()
        c.execute("SELECT * FROM Books WHERE title=?", (answer,))
        my_data = c.fetchone()
        isbn = my_data[0]
        my_author = my_data[2]
        last_read = my_data[3]
        my_genre = my_data[4]
        c.execute("SELECT name, position FROM Series WHERE book_isbn=?", (isbn,))
        ser_data = c.fetchone()
        ser_name = ser_data[0]
        ser_pos = ser_data[1]
        c.execute("SELECT tag FROM Tags WHERE book_isbn=?", (isbn,))
        tags = []
        for tag in c.fetchall():
            tags.append(tag[0])
        print("Title: %s \tAuthor: %s" % (answer, my_author))
        print("Last read in %d. Genre: %s" % (last_read, my_genre))
        print("Belongs to series %s (volume %d)\n" % (ser_name, ser_pos))
        c.close()
        if confirm("If there are discrepencies in this data, please confirm that you would like to resolve them:"):
            update_db_data_from_isfdb(db, isfdb_data, my_data, tags, ser_data)
            
def update_db_data_from_isfdb(db, isfdb, my_db, my_tags, my_series):
    c = db.cursor()
    update_book_start = "UPDATE Books SET "
    update_book_sections = []
    update_book_end = " WHERE isbn=:i"
    isbn = my_db[0]
    values_dic = {'i':isbn}
    my_title = my_db[1]
    isfdb_title = isfdb['title']
    if my_title != isfdb_title:
        final_title = isfdb_title
        update_book_sections.append("title=:t")
        values_dic['t'] = isfdb_title
        c.execute("UPDATE Tags SET book_title=:t WHERE book_isbn=:i", values_dic)
    else:
        final_title = my_title
    my_author = my_db[2]
    isfdb_author = isfdb['author']
    if my_author != isfdb_author:
        final_author = isfdb_author
        update_book_sections.append("author=:a")
        values_dic['a'] = isfdb_author
        c.execute("UPDATE Tags SET book_author=:a WHERE book_isbn=:i", values_dic)
    else:
        final_author = my_author
    if update_book_sections:
        update_book_q = update_book_start + ', '.join(update_book_sections) + update_book_end
        c.execute(update_book_q, values_dic)
        
    update_series_start = "UPDATE Series SET "
    update_series_sections = []
    update_series_end = " WHERE book_isbn=:i"
    my_ser_name = my_series[0]
    isfdb_series = isfdb['series']
    if my_ser_name != isfdb_series:
        update_series_sections.append("name=:n")
        values_dic['n'] = isfdb_series
    my_ser_pos = my_series[1]
    isfdb_pos = isfdb['position']
    if my_ser_pos != isfdb_pos:
        update_series_sections.append("position=:p")
        values_dic['p'] = isfdb_pos
    if update_series_sections:
        update_series_q = update_series_start + ', '.join(update_series_sections) + update_series_end
        c.execute(update_series_q, values_dic)
    
    new_tags = []
    for tag in isfdb['tags']:
        if not tag in my_tags:
            new_tags.append(tag)
    update_tag_q = "INSERT INTO Tags VALUES(?,?,?,?)"
    for tag in new_tags:
        c.execute(update_tag_q, (isbn, final_title, final_author, tag))

def show_stats(db, num_stats=5):
    cursor = db.cursor()
    cursor.execute("SELECT * FROM Books ORDER BY year_read DESC")
    recents = cursor.fetchmany(num_stats)
    recent_books = ''
    for book in recents:
        recent_books += '\n%s by %s: last read in %d' % (book[1], book[2], book[3])
    print(SEPARATOR)
    print("Your most recently read books:", recent_books)
    
    cursor.execute("SELECT count(isbn), author FROM Books GROUP BY author ORDER BY count(isbn) DESC")
    faves = cursor.fetchmany(num_stats)
    fave_authors = ''
    for author in faves:
        fave_authors += '\n%s: read %d times.' % (author[1], author[0])
    print(SEPARATOR)
    print("Your most read authors:", fave_authors)
    
    cursor.execute("SELECT tag, count(book_isbn) FROM Tags GROUP BY tag ORDER BY count(book_isbn) DESC")
    tags = cursor.fetchmany(num_stats)
    pop_tags = ''
    for tag in tags:
        pop_tags += '\n%s: read %d titles with this tag.' % (tag[0], tag[1])
    print(SEPARATOR)
    print("Your most popular tags:", pop_tags)
    
    cursor.execute("SELECT name, count(book_isbn), max(position) FROM Series GROUP BY name ORDER BY count(book_isbn) DESC")
    serieses = cursor.fetchmany(num_stats)
    longest_series = ''
    for series in serieses:
        longest_series += '\n%s: read %d books in the series. (most recent installment read: %d)' % (series[0], series[1], series[2])
    print(SEPARATOR)
    print("The series read with the most books:", longest_series)
    cursor.close()

def add_friend_recommend(db):
    print("Please enter the title and author, comma separated:")
    response = input(PROMPT).split(',')
    title = response[0].strip()
    author = response[1].strip()
    cs.add_recommendations([(title, author)], 'other')
    print("All set!")
    
def generate_random_rec(db):
    #start with the friend recs
    c = db.cursor()
    c.execute("SELECT tag, count(*) FROM Recs GROUP BY tag ORDER BY count(*) DESC")
    popular_tags = []
    for tup in c:
        if tup[0] != 'other':
            popular_tags.append(tup[0])
    tag_query = "SELECT * FROM Recs WHERE tag=?"
    current_tag = 'other'
    c.execute(tag_query, (current_tag,))
    tag_recs = c.fetchall()
    keep_going = True
    while keep_going and (popular_tags or tag_recs):
        if tag_recs:
            next_rec = tag_recs.pop(0)
        else:
            current_tag = popular_tags.pop(0)
            tag_recs = c.execute(tag_query, (current_tag,)).fetchall()
            next_rec = tag_recs.pop(0)
        print("Try this one:", format_recs([next_rec], current_tag))
        keep_going = confirm("Do you want another one?")
    if keep_going:
        print("Sorry, we don't have any more recommendations in the database.")
        print("We could generate more, but we won't because we've just listed over 100 recommendations and you haven't picked a single one.")
        print("Try again another time.")
    else:
        print("Enjoy!")
    c.close()

if __name__ == "__main__":
    main_repl()