from flask import jsonify, request, Flask, render_template, url_for, send_from_directory, redirect, session
import pyrebase
from flask_bootstrap import Bootstrap
import sys
sys.path.append('../database')
from models import *

app = Flask(__name__)
app.config.from_object('_config')
db.init_app(app)

Bootstrap(app)

config = {
    'apiKey': "AIzaSyDCYwU48HMDzFbz_98UUl_NzNXgzy16LOY",
    'authDomain': "meetaveiro-1520289975584.firebaseapp.com",
    'databaseURL': "https://meetaveiro-1520289975584.firebaseio.com",
    'projectId': "meetaveiro-1520289975584",
    'storageBucket': "meetaveiro-1520289975584.appspot.com",
    'messagingSenderId': "938454414503"
};


firebase = pyrebase.initialize_app(config)
auth = firebase.auth()

@app.route('/')
def welcome():
    return render_template('signIn.html')

@app.route('/signIn', methods=['POST'])
def signIn():
    email = request.form['user']
    passwd = request.form['password']
    user = None
    try:
        user = auth.sign_in_with_email_and_password(email, passwd)
        session_id = user['idToken']
        session['uid'] = str(session_id)
        session['email'] = email
        utilizador = db.session.query(Utilizador).get(email)
        session['type'] = utilizador.tipoid
    except:
        message = 'Invalid credentials'
        return render_template('signIn.html', message=message)
    
    return render_template('welcome.html')
    

if __name__ == '__main__':
    app.run(debug=True, port=8080, host='0.0.0.0')