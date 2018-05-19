# Sign in
#firebase.initializeApp({
#  apiKey: 'AIza…',
#  authDomain: '<PROJECT_ID>.firebasepp.com'
#});

#// As httpOnly cookies are to be used, do not persist any state client side.
#firebase.auth().setPersistence(firebase.auth.Auth.Persistence.NONE);

#// When the user signs in with email and password.
#firebase.auth().signInWithEmailAndPassword('user@example.com', 'password').then(user => {
#  // Get the user's ID token as it is needed to exchange for a session cookie.
#  return user.getIdToken().then(idToken = > {
#    // Session login endpoint is queried and the session cookie is set.
#    // CSRF protection should be taken into account.
#    // ...
#    const csrfToken = getCookie('csrfToken')
#    return postIdTokenToSessionLogin('/sessionLogin', idToken, csrfToken);
#  });
#}).then(() => {
#  // A page redirect would suffice as the persistence is set to NONE.
#  return firebase.auth().signOut();
#}).then(() => {
#  window.location.assign('/profile');
#});


from flask import Flask, session, render_template, request, redirect, g, url_for
import os

app = Flask(__name__)

@app.route('/', methods=['GET', 'POST'])
def index():
    return render_template('index.html')

@app.route('/sessionLogin', methods=['POST'])
def session_login():
    # Get the ID token sent by the client
    id_token = flask.request.json['idToken']
    # Set session expiration to 5 days.
    expires_in = datetime.timedelta(days=5)
    try:
        # Create the session cookie. This will also verify the ID token in the process.
        # The session cookie will have the same claims as the ID token.
        session_cookie = auth.create_session_cookie(id_token, expires_in=expires_in)
        response = flask.jsonify({'status': 'success'})
        # Set cookie policy for session cookie.
        expires = datetime.datetime.now() + expires_in
        response.set_cookie(
            'session', session_cookie, expires=expires, httponly=True, secure=True)
        return response
    except auth.AuthError:
        return flask.abort(401, 'Failed to create a session cookie')

@app.route('/profile', methods=['POST'])
def access_restricted_content():
    session_cookie = flask.request.cookies.get('session')
    # Verify the session cookie. In this case an additional check is added to detect
    # if the user's Firebase session was revoked, user deleted/disabled, etc.
    try:
        decoded_claims = auth.verify_session_cookie(session_cookie, check_revoked=True)
        return serve_content_for_user(decoded_claims)
    except ValueError:
        # Session cookie is unavailable or invalid. Force user to login.
        return flask.redirect('/login')
    except auth.AuthError:
        # Session revoked. Force user to login.
        return flask.redirect('/login')


@app.route('/sessionLogout', methods=['POST'])
def session_logout():
    response = flask.make_response(flask.redirect('/login'))
    response.set_cookie('session', expires=0)
    return response

