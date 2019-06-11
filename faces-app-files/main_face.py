from flask import Flask
from flask import (
    flash, g, redirect, render_template, request, session, url_for
)
from werkzeug.security import check_password_hash, generate_password_hash
#from werkzeug.exceptions import abort

import sqlite3
import datetime
import functools

import facesdb_7 as facedb


app = Flask(__name__)
app.secret_key = 'help'

db = sqlite3.connect('fakefaces.db', check_same_thread=False)
facedb.initialize(db)

def get_new_rating_id():
    c = db.cursor()
    c.execute("SELECT MAX(rating_id) FROM Ratings")
    result = c.fetchone()
    if result[0] is None:
        current = 0
    else:
        current = result[0]
    c.close()
    return current + 1


###################################################################
#---------------------------AUTH----------------------------------#
###################################################################
@app.route('/auth/register', methods=('GET', 'POST'))
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        name = request.form['name']
        location = request.form['hometown']
        twit = request.form['twitter']
        fb = request.form['facebook']
        insta = request.form['instagram']
        error = None

        if not username:
            error = 'Username is required.'
        elif not password:
            error = 'Password is required.'
        elif not name:
            error = 'Name is required.'
        elif not facedb.add_user(db, username.lower(), password, name, location, twitter=twit, insta=insta, fb=fb):
            error = 'Username is taken.'

        if error is None:
            #add_user successfully works?
            return redirect(url_for('login'))

        flash(error)

    return render_template('auth/register.html')

@app.route('/auth/login', methods=('GET', 'POST'))
def login():
    if request.method == 'POST':
        username = request.form['username'].lower()
        password = request.form['password']
        error = None

        user = get_user(username)

        if user is None:
            error = 'Incorrect username.'
        elif not user[1] == password:
            error = 'Incorrect password.'

        if error is None:
            session.clear()
            session['user_id'] = user[0]
            return redirect(url_for('index'))

        flash(error)

    return render_template('auth/login.html')

@app.before_request
def load_logged_in_user():
    user_id = session.get('user_id')

    if user_id is None:
        g.user = None
    else:
        g.user = get_user(user_id)

@app.route('/auth/logout')
def logout():
    session.clear()
    return redirect(url_for('index'))

def login_required(view):
    @functools.wraps(view)
    def wrapped_view(**kwargs):
        if g.user is None:
            return redirect(url_for('login'))

        return view(**kwargs)

    return wrapped_view

###################################################################
#--------------------------RATINGS--------------------------------#
###################################################################

possible_emojies = ["😎","🤠","💖","😊","😘","💣","🔥","😆","🤑","🌞","🍆","🍑","🦄","😲",'🤫','🙃','💩','🤢','😑','😕','🖕','💔','💀','🤬','😭','🙄','😂']

@app.route('/')
def index():
    high = facedb.get_highest_rated(db)
    most = facedb.get_most_rated_person(db)
    controversial = facedb.get_most_controversial(db)
    emos = []
    for e in possible_emojies:
        numbers = facedb.get_most_emoji(db, e)
        if numbers:
            if len(numbers) > 10:
                numbers = numbers[:10]
            emos.append((e, numbers))
    # posts = db.execute(
    #     'SELECT p.id, title, body, created, author_id, username'
    #     ' FROM post p JOIN user u ON p.author_id = u.id'
    #     ' ORDER BY created DESC'
    # ).fetchall()
    return render_template('ratings/index.html', highest=high, most=most, cont=controversial, emojies=emos)

@app.route('/rate', methods=('GET', 'POST'))
@login_required
def create():
    if request.method == 'POST':
        ratee = request.form['username'].lower()
        text = request.form['text']
        rating = request.form['stars']
        rating_type = request.form['context']
        emojies = request.form.getlist('emojies')
        user_id = g.user[0]
        
        error = None

        if not ratee:
            error = 'You must enter the name or username of the person you are rating.'
            #put the check for valid username here?
        elif get_user(ratee) is None:
            error = 'You can only rate a user who is registered with Faces. Tell your friend to join Faces.'
        elif ratee == user_id:
            error = 'You cannot submit ratings for yourself.'
            
        if error is not None:
            flash(error)
        else:
            rating_id = get_new_rating_id()
            time = str(datetime.datetime.utcnow())
            # db.execute(
            #     'INSERT INTO post (title, body, author_id)'
            #     ' VALUES (?, ?, ?)',
            #     (ratee, comments, g.user['id'])
            # )
            # db.commit()
            facedb.add_rating(db, rating_id, ratee, rating, text, user_id, rating_type, time)
            facedb.add_emojis(db, rating_id, emojies)
            return redirect(url_for('index'))

    return render_template('ratings/rate.html')

###################################################################
#--------------------------PROFILE--------------------------------#
###################################################################

@app.route('/user/<username>', methods=('GET', 'POST'))
@login_required
def view_user(username):
    user = get_user(username)
    if user[0] == g.user[0]:
        ratings = facedb.get_ratings_by_person(db, username)
    else:
        ratings = None
    avg = facedb.get_avg_of_ratings(db, username)[0]
    breakdown = facedb.get_rating_percentage(db, username)
    bff = facedb.best_friend(db, username)
    comments = get_user_comments(username)
    emo = get_user_emoji(username)
    return render_template('profile/other.html', viewer=user, rates=ratings, avg=avg, bff=bff, breakdown=breakdown, comments=comments, emoji=emo)

@app.route('/user/<username>/edit', methods=('GET', 'POST'))
@login_required
def edit_profile(username):
    if request.method == 'POST':
        name = request.form['name']
        loc = request.form['hometown']
        insta = request.form['insta']
        fb = request.form['fb']
        twit = request.form['twitter']
        error = None

        if not name:
            error = 'Name is required.'

        if error is not None:
            flash(error)
        else:
            facedb.edit_profile(db, username, name=name, location=loc, insta=insta, fb=fb, twitter=twit)
            return redirect(url_for('view_user', username=username))

    return render_template('profile/edit.html', viewer=get_user(username))


def get_user(username):
    user = db.execute(
            'SELECT * FROM Users WHERE id = ?',
            (username,)
        ).fetchone()
    return user

def get_user_comments(username):
    c = db.cursor()
    c.execute(
        'SELECT text, time FROM Ratings WHERE user_id=? ORDER BY time DESC', 
        (username, )
        )
    comments = c.fetchmany(100)
    c.close()
    return comments

def get_user_emoji(username):
    c = db.cursor()
    c.execute(
        'SELECT emoji, count(emoji) FROM Emojis NATURAL JOIN Ratings WHERE user_id=? GROUP BY emoji ORDER BY count(emoji) DESC',
        (username,)
    )
    emojie = c.fetchone()
    c.close()
    if emojie is None:
        return "☐"
    else:
        return emojie