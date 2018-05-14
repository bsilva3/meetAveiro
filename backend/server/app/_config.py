import os

basedir = os.path.abspath(os.path.dirname(__file__))

POSTGRES = {
    'user'      :   'postgres',
    'password'  :   'postgres',
    'database'  :   'postgres',
    'host'      :   'localhost',
    'port'      :   '5432',
}

WTF_CSRF_ENABLED = True
SECRET_KEY = '1234567'
SQLALCHEMY_DATABASE_URI = 'postgresql://%(user)s:%(password)s@%(host)s:%(port)s/%(database)s' % POSTGRES
