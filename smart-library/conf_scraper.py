# -*- coding: utf-8 -*-
"""
Created on Tue Apr 16 22:43:53 2019

@author: leahb
"""

import sqlite3
import random

import requests
from bs4 import BeautifulSoup

db = sqlite3.connect('cf2.db')

def to_unicode(nav_string):
    if nav_string is None:
        return None
    else:
        uni = ''
        for char in nav_string:
            uni += char
        return uni
    
def close_connections():
    db.close()
    
    

def find_my_book(title, verbose=True):
    q = "SELECT * FROM Books WHERE title=:t"
    c = db.cursor()
    c.execute(q, {'t':title})
    book = c.fetchone() #assumes that only one title is located in the database
    if book is None:
        if verbose:
            print("That title is not kept in Leah's database.")
        return None
    else:
        isbn = book[0]
        title = book[1]
        c.close()
        title_soup = get_final_soup(isbn, title, verbose)
        if title_soup is None:
            if verbose:
                print("Title not found in the ISFDB: %s" % title)
            return None
        else:
            parsed_soup = parse_title_soup(title_soup, verbose)
            parsed_soup['isbn'] = isbn
            return parsed_soup
            
    
def get_final_soup(isbn, title, verbose=True):
    pub_soup = get_pub_soup(isbn, verbose)
    if not pub_soup is None:
        #Just in case the title in the ISFDB is different than the one I use:
        title_tag = pub_soup.find("b", string='Publication:')
        isfdb_title = title_tag.next_sibling.strip()
        link_tags = pub_soup.find_all("a", string=isfdb_title)
        title_link = ''
        for tag in link_tags:
            ref = tag['href']
            if ('title' in ref):
                title_link = ref
        if title_link == '':
            #the function couldn't find the link to the Title page correctly, so we try a different way.
            title_links = [tag for tag in pub_soup.find_all("a") if 'title' in tag['href']]
            for link in title_links:
                if 'dir' in link.attrs.keys() and link.attrs['dir'] == 'ltr':
                    title_link = link['href']
        if verbose:
            print("Making title soup...")
        title_soup = BeautifulSoup(requests.get(title_link).text, "html.parser")
        return title_soup
    else:
        if verbose:
            print('publication soup not found for isbn %d' % isbn)
        return None
    
def get_pub_soup(isbn, verbose=True):
    url = "http://www.isfdb.org/cgi-bin/se.cgi?arg=%d&type=ISBN" % isbn
    response = requests.get(url)
    if verbose:
        print("Searching the ISFDB...")
    search_soup = BeautifulSoup(response.text, "html.parser")
    record_id = search_soup.find('span', 'recordID')
    pub_table = search_soup.find('table', class_='publications')
    if verbose:
        print("making publication soup...")
    if record_id is None and pub_table is None:
        return None
    else:
        if not record_id is None:
            rec_num = to_unicode(record_id.contents[1])
            url = "http://www.isfdb.org/cgi-bin/pl.cgi?%s" % rec_num
            if verbose:
                print("single record id found, using URL:\n%s" % url)
        else:
            first_result = pub_table.find_all('td')[0]
            a_links = first_result.contents[0]
            url = a_links['href']
            if verbose:
                print("multiple records found for isbn %d, using URL:\n%s" % (isbn, url))
        response = requests.get(url)
        pub_soup = BeautifulSoup(response.text, "html.parser")
        return pub_soup

def parse_title_soup(soup, verbose=True):
    box = soup.find('div', class_='ContentBox')
    attributes = box.find_all('b')
    if verbose:
        print("ISFDB attributes in title record:")
    record_attrs = {}
    for attr in attributes:
        #important values: title, author, series, series number, current tags.
        #create a dictionary which will be returned to the top function to compare with the DB record(s)
        cat = to_unicode(attr.string).strip()
        val = attr.next_sibling
        if cat == 'Title:':
            val = val.strip()
            record_attrs['title'] = val
        elif cat == 'Author:' or cat == 'Authors:':
            val = to_unicode(val.next_sibling.string)
            record_attrs['author'] = val
        elif cat == 'Series:':
            val = to_unicode(val.next_sibling.string)
            record_attrs['series'] = val
        elif cat == 'Series Number:':
            val = int(val.strip())
            record_attrs['position'] = val
        elif cat == 'Current Tags:':
            tags = []
            val = val.next_sibling
            while not (val is None) and val.name == 'a':
                if to_unicode(val.string) != 'Add Tags':
                    tags.append(to_unicode(val.string))
                val = val.next_sibling.next_sibling #have to skip over the string thing
            record_attrs['tags'] = tags
    if verbose:
        print("record attributes located: %s" % str(record_attrs))
    return record_attrs

#one-time use function to add tags using books already in my db
def add_tags_from_isfdb():
    q_find = "SELECT * FROM Books"
    q_insert = "INSERT INTO Tags VALUES (?,?,?,?)"
    cursor = db.cursor()
    cursor.execute(q_find)
    new_tags = []
    for book in cursor:
        title = book[1]
        isbn = book[0]
        author = book[2]
        isfdb_record = find_my_book(title, False)
        if not isfdb_record is None:
            isfdb_tags = isfdb_record['tags']
            for tag in isfdb_tags:
                new_tags.append((isbn, title, author, tag))
            print("tags prepared for title: %s" % title)
    for tag_tup in new_tags:
        cursor.execute(q_insert, tag_tup)
    print("%d tags added from isfdb." % len(new_tags))
    db.commit()
    cursor.close()
    
def check_title(book, record):
    my_title = book[1]
    my_author = book[2]
    try:
        isfdb_title = record['title']
        isfdb_author = record['author']
        return my_title == isfdb_title and my_author == isfdb_author
    except KeyError as e:
        print("Odd error detected: record is not None but has no key %s" % e)
        print("book title: %s, author: %s" % (my_title, my_author))
        return False

def check_series(series, record):
    name = series[0]
    pos = series[2]
    try:
        isfdb_series = record['series']
        isfdb_pos = record['position']
        return name == isfdb_series and pos == isfdb_pos
    except KeyError as e:
        print("Odd error detected: record is not None but has no key %s" % e)
        print("Series name: %s, position: %d" % (name, pos))
        return False

# Maintenance function meant to help me check and make sure that parsing ISFDB works correctly
#   Could be edited so that mismatching information is corrected to ISFDB version of the info
def check_records(verbose=False):
    c1 = db.cursor()
    q = "SELECT * FROM Books"
    c1.execute(q)
    q2 = "SELECT * FROM Series WHERE book_isbn=:i"
    for book in c1:
        try:
            record = find_my_book(book[1], verbose)
            if record is None:
                print('Title not found in ISFDB: %s' % book[1])
            elif check_title(book, record):
                print('Record in ISFDB matches my db for title %s.' % book[1])
                c2 = db.cursor()
                c2.execute(q2, {'i':book[0]})
                series = c2.fetchone()
                c2.close()
                if not series is None and check_series(series, record):
                    print("\tMy series info matches ISFDB.")
                elif not series is None:
                    print("\tMy series info does not match ISFDB.")
            else:
                print("ISFDB record does not match my records for title %s." % book[1])
        except Exception as e:
            print("error caught; book info-- title: %s, author: %s" % (book[1], book[2]))
            print(e)
    c1.close()
    
def test_hpseven():
    hp_soup = get_pub_soup(9780545139700)
    title_tag = hp_soup.find("b", string='Publication:')
    isfdb_title = title_tag.next_sibling.strip()
    print("The ISFDB refers to this record with the title %s." % isfdb_title)
    link_tags = hp_soup.find_all("a", string=isfdb_title)
    print("All tags found where the string is equal to the title above:")
    print(link_tags)
    title_tags = [tag for tag in hp_soup.find_all("a") if 'title' in tag['href']]
    print("All tags found where the word title is somewhere in the link:")
    print(title_tags)
    for tag in title_tags:
        if to_unicode(tag.string) == isfdb_title:
            print("Tag found where the string is equal to the title above:")
            print(tag)
        print('tag attributes: %s' % tag.attrs)
        if 'dir' in tag.attrs.keys():
            print("found tag with attribute 'dir'")
            print("is tag['dir'] == 'ltr'? ", tag['dir'] == 'ltr')
        #print("tag attribute dir: %s" % tag['dir'])
    
#######################################################################################
        # Below are functions for making recommendations
        
# grabs the initial tag soup from the original tag-based search of ISFDB
def first_tag_soup(tag, verbose):
    if verbose:
        print("Getting the initial tag soup...")
    url = "http://www.isfdb.org/cgi-bin/se.cgi?arg=%s&type=Tag" % tag
    response = requests.get(url)
    result_soup = BeautifulSoup(response.text, "html.parser")
    table = result_soup.find("table", class_="generic_table")
    tag_links = table.find_all("a", string=tag)
    if len(tag_links) > 1 and verbose:
        print("Found more than one link for tag %s, using first option" % tag)
    link_found = tag_links[0]['href']
    page_num = random.randint(0, 150) #the upper bound for page number of a tag is based on the fantasy tag, which has 144 pages. 
    soup_link = link_found + ('+' + str(page_num) + '00')
    tag_soup = BeautifulSoup(requests.get(soup_link).text, "html.parser")
    return link_found, page_num, tag_soup
    
def search_and_add_isfdb_tags(tag, desired_results, verbose, check_author):
    url, page_num, soup = first_tag_soup(tag, verbose)
    table = soup.find('table', class_="generic_table")
    while table.find('td') is None:
        page_num /= 2 #assumes that page 0 of a tag has at least some entries, meaning that we should never be dividing by zero.
        next_url = url + '+' + str(page_num) + '00'
        soup = BeautifulSoup(requests.get(next_url).text, 'html.parser')
        table = soup.find('table', class_="generic_table")
    final_results = 0
    while final_results < desired_results:
        res_num = desired_results - final_results
        if verbose:
            print("Attempting to get %d recs from soup..." % res_num)
        results = add_tags_from_table(table, res_num, tag, check_author)
        if verbose:
            print("Got %d tags from the soup." % results)
        final_results += results
        next_url = url + '+' + str(page_num) + '00'
        next_soup = BeautifulSoup(requests.get(next_url).text, 'html.parser')
        table = next_soup.find('table', class_="generic_table")
        while table.find('td') is None:
            page_num /= 2
            next_url = url + '+' + str(page_num) + '00'
            next_soup = BeautifulSoup(requests.get(next_url).text, 'html.parser')
            table = next_soup.find('table', class_="generic_table")
        if not table.find('td') is None:
            page_num += 1
    return final_results

def add_tags_from_table(table, res_length, tag, check_author):
    row = table.tr.next_sibling.next_sibling
    results = 0
    while results < res_length and not row is None:
        row_contents = row.contents
        pub_type = to_unicode(row_contents[3]).lower()
        lang = to_unicode(row_contents[5]).lower()
        title_col = row_contents[7]
        author_col = row_contents[9]
        all_tag_links = row_contents[-2].contents
        if pub_type == 'novel' and lang == 'english':
            title = to_unicode(title_col.a.string)
            author = to_unicode(author_col.a.string)
            if check_rec((title, author), check_author):
                tags = []
                next_tag = 1
                while next_tag < len(all_tag_links):
                    link_tag = all_tag_links[next_tag]
                    tags.append(to_unicode(link_tag.string))
                    next_tag += 2
                try:
                    for new_tag in tags:
                        add_recommendations([(title, author)], new_tag)
                        results += 1
                except sqlite3.OperationalError as e:
                    print("error:", e)
        row = row.next_sibling.next_sibling
    return results

def check_rec(rec, check_author=False):
    c = db.cursor()
    q = "SELECT * FROM Books WHERE title=? AND author=?"
    c.execute(q, rec)
    result = c.fetchone()
    if result is None and not check_author:
        c.close()
        return True
    elif check_author:
        new_q = "SELECT * FROM Books WHERE author=?"
        c.execute(new_q, (rec[1],))
        result = c.fetchone()
        c.close()
        return not (result is None) #returns true only if the author is in the database
    else:
        c.close()
        return False

def add_recommendations(recs, tag):
    c = db.cursor()
    q = "SELECT max(id) FROM Recs"
    rec_id = c.execute(q).fetchone()[0]
    if rec_id is None:
        rec_id = 0
    else:
        rec_id += 1
    q = "INSERT INTO Recs VALUES (?,?,?,?)"
    for r in recs:
        r = (r[0], r[1], tag, rec_id)
        c.execute(q, r)
    db.commit()
    c.close()

def add_next_books(series, max_recs, verbose):
    name, max_position = series
    url = "http://www.isfdb.org/cgi-bin/se.cgi?arg=%s&type=Series" % name
    search_soup = BeautifulSoup(requests.get(url).text, "html.parser")
    table = search_soup.find("table", class_="generic_table")
    if table is None:
        if verbose:
            print("Could not find results for series %s in ISFDB." % name)
        return 0
    else:
        links = table.find_all("a", string=name)
        if len(links) < 1:
            if verbose:
                print("Found 0 exact matches for series %s in ISFDB." % name)
                print("Regex matching not yet implemented, either.")
            return 0
        else:
            link_found = (links[0]['href'] + "+None")
            if verbose:
                print("link found: ", link_found)
            series_soup = BeautifulSoup(requests.get(link_found).text, "html.parser")
            series_box = series_soup.find_all('div', class_="ContentBox")[1]
            parent_series_ul = series_box.contents[3]
            inner_series_ul = parent_series_ul.contents[1].contents[3]
            entry_tree = inner_series_ul.contents[1]
            count = 0
            entry = entry_tree.contents
            while count < max_recs and not entry is None:
                try:
                    number = int(to_unicode(entry[0]))
                    if number > max_position:
                        title = to_unicode(entry[1].string)
                        author = to_unicode(entry[7].string)
                        if check_rec((title, author)):
                            try:
                                add_recommendations([(title, author)], name)
                                count += 1
                            except sqlite3.OperationalError as o:
                                print("error!", o)
                    next_entry_pos = len(entry) - 1
                    if next_entry_pos >= 0:
                        entry = entry[next_entry_pos].contents
                    else:
                        entry = None
                except ValueError:
                    #print("something went wrong with parsing the series soup for %s" % name)
                    entry = None
            return count
            
def refresh_tag_recommends(total_recs, check_author, verbose=False):
    cursor = db.cursor()
    cursor.execute("SELECT count(book_isbn), tag FROM Tags GROUP BY tag ORDER BY count(book_isbn) DESC")
    total_added = 0
    while total_added < total_recs:
        result = cursor.fetchone()
        tag = result[1]
        count = result[0]
        recs_per_tag = min((count * 5), total_recs)
        if verbose:
            print("adding %d recs for tag %s" % (recs_per_tag, tag))
        successful_adds = search_and_add_isfdb_tags(tag, recs_per_tag, verbose, check_author)
        total_added += successful_adds
    cursor.close()

def refresh_series_recommends(num_recs, verbose=False):
    cursor = db.cursor()
    cursor.execute("SELECT name, max(position) FROM Series GROUP BY name ORDER BY max(position) DESC")
    #The above query is likely the cause of the bug/feature discussed in the readme.
    count = 0
    next_series = cursor.fetchone()
    while count < num_recs and not next_series is None:
        recs_needed = num_recs - count
        new_recs = add_next_books(next_series, recs_needed, verbose)
        count += new_recs
        next_series = cursor.fetchone()
    cursor.close()










