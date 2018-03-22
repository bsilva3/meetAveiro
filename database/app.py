from flask import *
from database.models import *

app = Flask(__name__)


POSTGRES = {
    'user'      :   'postgres',
    'password'  :   'postgres',
    'database'  :   'postgres',
    'host'      :   'localhost',
    'port'      :   '5432',
}

app.config['DEBUG'] = True
app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://%(user)s:%(password)s@%(host)s:%(port)s/%(database)s' % POSTGRES

db.init_app(app)

@app.route('/')
def main():
    ## Adição dos tipos de Utilizadores ##
    addTipo('Administrador')
    addTipo('Turista')
    addTipo('ExAdministrador')           # Quando um administrador elimina a conta
    addTipo('ExTurista')                 # Quando um turista elimina a conta

    ## Adição dos Utilizadores ##

    # Adição de Administradores
    addUtilizador('maria@ua.pt', 1)
    addUtilizador('carol@ua.pt', 1)
    addUtilizador('manel@ua.pt', 1)

    # Adição de Turistas
    addUtilizador('joana@ua.pt', 2)
    addUtilizador('joao@ua.pt', 2)
    addUtilizador('carlos@ua.pt', 2)

    # Adição de ExAdministrador
    addUtilizador('andre@ua.pt',3)

    # Adição de ExTurista
    addUtilizador('andreia',4)

    # Adição - tipo inválidp
    #addUtilizador('carlos@ua.pt', 3)            # sqlalchemy.exc.IntegrityError
    #addUtilizador('abc@ua.pt', 7)               # sqlalchemy.exc.IntegrityError


    return 'Done'

if __name__ == '__main__':
    app.run()