#draft 2
import sqlite3

def initialize(db):
    c = db.cursor()
    q = """CREATE TABLE IF NOT EXISTS Users(
        id VARCHAR(30) PRIMARY KEY NOT NULL,
        password VARCHAR(50) NOT NULL,
        name VARCHAR(100) NOT NULL,
        location VARCHAR(50),
        picture VARCHAR(100),
        twitter VARCHAR(30),
        insta VARCHAR(30),
        fb VARCHAR(30)
        )"""
    c.execute(q)
    q = """CREATE TABLE IF NOT EXISTS Ratings(
        rating_id INT(10) PRIMARY KEY NOT NULL,
        user_id VARCHAR(30) NOT NULL,
        rating INT(11) NOT NULL,
        text VARCHAR(250),
        rater_id VARCHAR(30) NOT NULL,
        type VARCHAR(30) NOT NULL,
        time VARCHAR(30) NOT NULL,
        FOREIGN KEY (user_id) REFERENCES Users(id),
        FOREIGN KEY (rater_id) REFERENCES Users(id)
        )"""
    c.execute(q)
    q = """CREATE TABLE IF NOT EXISTS Emojis(
        rating_id INT(10) NOT NULL,
        emoji VARCHAR(1) NOT NULL,
        FOREIGN KEY (rating_id) REFERENCES Ratings(rating_id)
    )"""
    c.execute(q)
    c.close()

def add(cursor, relation, values_string, d):
    """Returns true if n did not previously exist and inserts into the table. 
    Otherwise returns false. Assumes cursor is a valid cursor
    into database.
    """
    q = "INSERT INTO {} VALUES {}".format(relation, values_string)
    try:
        cursor.execute(q, d)
    except sqlite3.IntegrityError as e:
        print(e)
        return False
    else:
        return True
    
def reset(db):
    cursor = db.cursor()
    q = "DELETE FROM Users"
    cursor.execute(q)
    q = "DELETE FROM Ratings"
    cursor.execute(q)
    q = "DELETE FROM Emojis"
    cursor.execute(q)
    db.commit()
    cursor.close()

#returns True if User is successfully added, False if there is a unique id constraint being violated    
def add_user(db, user_id, password, name, location=None, picture=None, twitter=None, insta=None, fb=None):
    c = db.cursor()
    q = "SELECT id FROM Users WHERE id=?"
    c.execute(q, (user_id,))
    x = c.fetchone()
    print(x)
    if x == None:
        add(c, "Users", "(?,?,?,?,?,?,?,?)", (user_id,password,name,location,picture,twitter,insta,fb))
        db.commit()
        return True
    else:
        return False
    c.close() 
    
def add_rating(db, rating_id, user_id, rating, text, rater_id, rating_type, time):
    c = db.cursor()
    add(c, "Ratings", "(?,?,?,?,?,?,?)", (rating_id, user_id, rating, text, rater_id, rating_type, time))
    db.commit()
    c.close()
    
    
def add_emojis(db, rating_id, emojis):
    c=db.cursor()
    for _ in emojis:
        add(c, "Emojis", "(?,?)", (rating_id, _))
    db.commit()
    c.close()

def edit_profile(db, user_id, password=None, name=None, location=None, picture=None, twitter=None, insta=None, fb=None):
    c = db.cursor()
    q = "UPDATE Users SET "
    t = []
    if name != None:
        q += "name=?, "
        t.append(name)
    if password != None:
        q += "password=?, "
        t.append(password)
    if location != None:
        q += "location=?, "
        t.append(location)
    if picture != None:
        q += "picture=?, "
        t.append(picture)
    if twitter != None:
        q += "twitter=?, "
        t.append(twitter)
    if insta != None:
        q += "insta=?, "
        t.append(insta)
    if fb != None:
        q += "fb=?, "
        t.append(fb)
    q = q.strip()
    q = q.strip(",")
    q += " WHERE id=?"
    t.append(user_id)
    tup = tuple(t)
    c.execute(q, tup)
    db.commit()
    c.close()
        
#common functions
def get_ratings(db, user_id):
    c = db.cursor()
    q = "SELECT * FROM Ratings WHERE user_id = ?"
    l = []
    c.execute(q, (user_id,))
    for _ in c:
        l.append(_)
    return l
    c.close()
    
def get_emojis_for_rating(db, rating_id):
    c = db.cursor()
    q = "SELECT emoji FROM Emojis WHERE rating_id=?"
    c.execute(q, (rating_id,))
    l = []
    for _ in c:
        l.append(_)
    c.close()
    return l
    
def get_emojis_for_user(db, user_id):
    c = db.cursor()
    q = "SELECT emoji FROM Emojis NATURAL JOIN Ratings WHERE user_id=?"
    c.execute(q, (user_id,))
    l = []
    for _ in c:
        l.append(_)
    c.close()
    return l

#############################################################################

#returns tuple of the name, id, and average rating of the highest rated user
def get_highest_rated(db):
    c = db.cursor()
    q = "SELECT AVG(rating), user_id FROM Ratings GROUP BY user_id"
    c.execute(q)
    best = None
    score = 0
    for _ in c:
        if(_[0] > score):
            best = _[1]
            score = _[0]
    q = "SELECT name FROM Users where id=?"
    c.execute(q, (best,))
    name = c.fetchone()
    c.close()
    return (name[0], best, score)


#returns tuple of the name, id, and number of ratings of most rated person
def get_most_rated_person(db):
    c = db.cursor()
    q = "SELECT COUNT(user_id), user_id FROM Ratings GROUP BY user_id"
    c.execute(q)
    best = None
    count = 0
    for _ in c:
        if(_[0] > count):
            count = _[0]
            best = _[1]
    q = "SELECT name FROM Users where id=?"
    c.execute(q, (best,))
    name = c.fetchone()[0]
    c.close()

    return (name, best, count)

#returns dictionary of type percentage
def get_rating_percentage(db, id):
    c = db.cursor()
    id = (str(id), )
    q = "SELECT COUNT(type), type FROM Ratings WHERE user_id=? GROUP BY type"
    c.execute(q, id)
    d = {}
    total = 0
    for _ in c:
        d[_[1]] = _[0]
        total = total + _[0]
    c.close()
    for f in d:
        d[f] = d[f] / total

    return d

#returns decending list of users with most emoji count (count, user_id)
def get_most_emoji(db, emoji):
    c = db.cursor()
    q = "SELECT COUNT(emoji), rater_id FROM Emojis NATURAL JOIN Ratings WHERE emoji = ? GROUP BY rater_id ORDER BY COUNT(emoji) DESC"
    c.execute(q, (emoji,))
    l = []
    for _ in c:
        l.append(_)
    c.close()
    return l

# returns a dictionary with username as key and the level of controversy
# 
def get_controversial(db):
    c = db.cursor()
    q = "SELECT rating, user_id FROM Ratings ORDER BY user_id"
    d = {}
    c.execute(q)
    
    for _ in c:
        if(_[1] in d.keys()):
            d[_[1]].append(_[0])
            
        else:
            d[_[1]] = [_[0]]
  

    for x in d:
        d[x].sort()
        d[x] = cont_func(d[x])
    
    return d

#returns tuple with (name, id, controversy score) of most controversial person
def get_most_controversial(db):
    controversial = get_controversial(db)
    most = ("", 0)
    for key, value in controversial.items():
        if value > most[1]:
            most = (key, value)
    c = db.cursor()
    q = "SELECT name FROM users WHERE id=?"
    c.execute(q, (most[0],))
    x = c.fetchone()
    most = (x[0], most[0], most[1])
    return most

# assumes l is ordered
# sum of distance between (0, n) (1, n-1), (2, n-2)
# higher scores are more controversial
def cont_func(l):
    score = 0
    for i in range(len(l)//2):
        score = score + (l[i] - l[len(l)-i-1])**2

    return score


########################################################################

#returns avg of all ratings for a user
    # do we want a weighting algorithm???????
def get_avg_of_ratings(db, user, rating_type=None):
    c = db.cursor()
    q = "SELECT AVG(rating) FROM Ratings WHERE user_id=?"
    if rating_type != None:
        q += " AND type=?"
        c.execute(q, (user, rating_type))
    else:
        c.execute(q, (user,))
    x = c.fetchone()
    c.close()
    return x

#returns list of all ratings by a person
def get_ratings_by_person(db, user):
    c = db.cursor()
    q = "SELECT * FROM Ratings WHERE rater_id=?"
    l = []
    c.execute(q, (user,))
    for _ in c:
        l.append(_)
    c.close()
    return l
        
#parses a ratings list for specific attributes
def parse_rating_list(l, rating_type):
    x = []
    for _ in l:
        if _[5] == rating_type:
            x.append(_)
    return x

#cursor to list
def cursor_to_list(cursor):
    x = []
    for _ in cursor:
        x.append(_)
    return x

#returns a tuple of user's best friends based on how many intimate interactions of 4 or 
#more stars they have exchanged. the tuple format is (# of interactions, best_friend_id, tied_best_friend_id, tied_best_friend_id...)
def best_friend(db, user):
    ratings = get_ratings(db, user)
    ratings_by = get_ratings_by_person(db, user)
    bag = {}
    for r in ratings:
        if int(r[2]) >= 4 and r[5].lower() == 'intimate':
            if r[4] in bag:
                bag[r[4]] += 1
            else:
                bag[r[4]] = 1
    for r in ratings_by:
        if int(r[2]) >= 4 and r[5].lower() == 'intimate':
            if r[4] in bag:
                bag[r[4]] += 1
            else:
                bag[r[4]] = 1
    best = (0, "none")
    nbest = False
    for friend, interactions in bag.items():
        if interactions > best[0]:
            best = (interactions, friend)
        elif interactions == best[0]:
            nbest = best + (friend,)
    if nbest: 
        return nbest
    else: 
        return best

if __name__ == '__main__':
    db = sqlite3.connect('fakefaces.db')
    initialize(db)
    db.close()
fake = sqlite3.connect('fakefaces.db')
