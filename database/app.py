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

def addTiposExemplo():
    ## Adição dos tipos de Utilizadores ##
    addTipo('Administrador')
    addTipo('Turista')
    addTipo('ExAdministrador')  # Quando um administrador elimina a conta
    addTipo('ExTurista')  # Quando um turista elimina a conta

def addUtilizadoresExemplo():
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

def addConceitosExemplo():
    # Adição de conceitos
    addConceito('Biblioteca, Universidade de Aveiro', 'maria@ua.pt', 40.6310031, -8.659642599999984, 1, 'Bibs', 4.5)
    addConceito('DETI, Universidade de Aveiro', 'maria@ua.pt', 40.633175, -8.659496, 0.3, 'Dep DETI', 4.9)
    addConceito('DMat, Universidade de Aveiro', 'carol@ua.pt', 40.630349, -8.658214, 0.6, 'Dep DMat', 4.3)
    addConceito('Reitoria, Universidade de Aveiro', 'manel@ua.pt', 40.63118, -8.657398, 0.5, 'Reitoria da UA', 4.3)

def addPercursosExemplo():
    addPercurso('manel@ua.pt', 'Conhece a UA', 'Validado','Conhece a Universidade de Aveiro', 4.6)
    addPercurso('joana@ua.pt', 'Aveiro 1', 'Publico', 'Conhece a cidade de Aveiro', 4.4)
    addPercurso('joana@ua.pt', 'Aveiro 2', 'Privado', 'Conhece a cidade de Aveiro', 4.4)

def addPontosExemplo():
    addPonto(40.5, -8.6)
    addPonto(40.6310031, -8.659642599999984, 1)
    addPonto(40.633175, -8.659496, 1)
    addPonto(40.630349, -8.658214, 1)

def addInstanciaPercursoExemplo():
    addInstanciaPercurso('joana@ua.pt', 1, '2018-03-22 13:00:00', '2018-03-22 14:00:00')

def addFotografiaExemplo():
    addFotografia('Biblioteca, Universidade de Aveiro', 'joana@ua.pt',  40.6310031, -8.659642599999984, 'path1.png', 1, '2018-03-22 13:30:00', 4.5)
    addFotografia('Biblioteca, Universidade de Aveiro', 'joana@ua.pt',  40.6310031, -8.659642599999984, 'path2.png', 1, '2018-03-22 13:30:30', 4.5)

def addInfoExemplo():
    addTiposExemplo()
    addUtilizadoresExemplo()
    addConceitosExemplo()
    addPercursosExemplo()
    addPontosExemplo()
    addInstanciaPercursoExemplo()
    addFotografiaExemplo()

@app.route('/')
def main():
    addFotografiaExemplo()
    return 'Done'

if __name__ == '__main__':
    app.run()