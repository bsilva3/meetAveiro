from flask import *
from models import *


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

def addUsers():
    addUtilizador('bjpsilva@hotmail.com', 1)
    addUtilizador('joao@outlook.com',1)
    addUtilizador('youngf3@live.com.pt',1)
    addUtilizador('joaoserpa1997@outlook.com',1)


def addTiposExemplo():
    ## Adição dos tipos de Utilizadores ##
    addTipo('Administrador')
    addTipo('Turista')

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
    addUtilizador('andre@ua.pt',2)

    # Adição - tipo inválidp
    #addUtilizador('carlos@ua.pt', 1)            # sqlalchemy.exc.IntegrityError
    #addUtilizador('abc@ua.pt', 7)               # sqlalchemy.exc.IntegrityError

def addConceitosExemplo():
    # Adição de conceitos
    addConceito('biblioteca', 'maria@ua.pt', 40.6310031, -8.659642599999984, 1, 'Bibs', 'Biblioteca', 4.5)
    addConceito('deti', 'maria@ua.pt', 40.633175, -8.659496, 0.3, 'Dep DETI', 'DETI',4.9)
    addConceito('dmat', 'carol@ua.pt', 40.630349, -8.658214, 0.6, 'Dep DMat', 'Departamento de Matemática', 4.3)
    addConceito('reitoria', 'manel@ua.pt', 40.63118, -8.657398, 0.5, 'Reitoria da UA', 'Reitoria', 4.3)
    '''addConceito('bugas', 'maria@ua.pt', 40.6310031, -8.659642599999984, 1, 'Loja das Bugas', 'Loja das Bugas', 4.5)
    addConceito('cantina', 'maria@ua.pt', 40.633175, -8.659496, 0.3, 'Cantina Santiago', 'Cantina Santiago',4.9)
    addConceito('dmat', 'carol@ua.pt', 40.630349, -8.658214, 0.6, 'Dep DMat', 'Departamento de Matemática', 4.3)
    addConceito('reitoria', 'manel@ua.pt', 40.63118, -8.657398, 0.5, 'Reitoria da UA', 'Reitoria', 4.3)
    addConceito('biblioteca', 'maria@ua.pt', 40.6310031, -8.659642599999984, 1, 'Bibs', 'Biblioteca', 4.5)
    addConceito('deti', 'maria@ua.pt', 40.633175, -8.659496, 0.3, 'Dep DETI', 'DETI',4.9)
    addConceito('dmat', 'carol@ua.pt', 40.630349, -8.658214, 0.6, 'Dep DMat', 'Departamento de Matemática', 4.3)
    addConceito('reitoria', 'manel@ua.pt', 40.63118, -8.657398, 0.5, 'Reitoria da UA', 'Reitoria', 4.3)
    addConceito('biblioteca', 'maria@ua.pt', 40.6310031, -8.659642599999984, 1, 'Bibs', 'Biblioteca', 4.5)
    addConceito('deti', 'maria@ua.pt', 40.633175, -8.659496, 0.3, 'Dep DETI', 'DETI',4.9)
    addConceito('dmat', 'carol@ua.pt', 40.630349, -8.658214, 0.6, 'Dep DMat', 'Departamento de Matemática', 4.3)
    addConceito('reitoria', 'manel@ua.pt', 40.63118, -8.657398, 0.5, 'Reitoria da UA', 'Reitoria', 4.3)
    addConceito('biblioteca', 'maria@ua.pt', 40.6310031, -8.659642599999984, 1, 'Bibs', 'Biblioteca', 4.5)
    addConceito('deti', 'maria@ua.pt', 40.633175, -8.659496, 0.3, 'Dep DETI', 'DETI',4.9)
    addConceito('dmat', 'carol@ua.pt', 40.630349, -8.658214, 0.6, 'Dep DMat', 'Departamento de Matemática', 4.3)
    addConceito('reitoria', 'manel@ua.pt', 40.63118, -8.657398, 0.5, 'Reitoria da UA', 'Reitoria', 4.3)
    addConceito('biblioteca', 'maria@ua.pt', 40.6310031, -8.659642599999984, 1, 'Bibs', 'Biblioteca', 4.5)
    addConceito('deti', 'maria@ua.pt', 40.633175, -8.659496, 0.3, 'Dep DETI', 'DETI',4.9)
    addConceito('dmat', 'carol@ua.pt', 40.630349, -8.658214, 0.6, 'Dep DMat', 'Departamento de Matemática', 4.3)
    addConceito('reitoria', 'manel@ua.pt', 40.63118, -8.657398, 0.5, 'Reitoria da UA', 'Reitoria', 4.3)
    addConceito('reitoria', 'manel@ua.pt', 40.63118, -8.657398, 0.5, 'Reitoria da UA', 'Reitoria', 4.3)'''

def addPercursosExemplo():
    addPercurso('manel@ua.pt', 'Conhece a UA', 'Validado','Conhece a Universidade de Aveiro')
    addPercurso('joana@ua.pt', 'Aveiro 1', 'Publico', 'Conhece a cidade de Aveiro')
    addPercurso('joana@ua.pt', 'Aveiro 2', 'Privado', 'Conhece a cidade de Aveiro')

def addPontosExemplo():
    addPonto(40.5, -8.6)
    addPonto(40.6310031, -8.659642599999984, 1)
    addPonto(40.633175, -8.659496, 1)
    addPonto(40.630349, -8.658214, 1)

def addInstanciaPercursoExemplo():
    addInstanciaPercurso('joana@ua.pt', 1, '2018-03-22 13:00:00', '2018-03-22 14:00:00', 4.4)

def addFotografiaExemplo():
    addFotografia(1, 'biblioteca', 'joana@ua.pt',  40.6310031, -8.659642599999984, '../../../../treino/biblioteca/1.jpg', 1, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(2, 'biblioteca', 'joana@ua.pt',  40.6310031, -8.659642599999984, '../../../../treino/biblioteca/1.jpg', 1, '2018-03-22 13:30:30', 4.5, 'EmEspera', 0.79, 0.156)
    addFotografia(3, 'biblioteca', 'joana@ua.pt', 40.6310030, -8.65964259999998, '../../../../treino/biblioteca/1.jpg',
                  1, '2018-03-22 13:30:30', 4.5, 'EmEspera', 0.79, 0.156)

def addInfoExemplo():
    addTiposExemplo()
    addUtilizadoresExemplo()
    addConceitosExemplo()
    addPercursosExemplo()
    addPontosExemplo()
    addInstanciaPercursoExemplo()
    addFotografiaExemplo()

def queriesExemplo():
    print("Número total de utlizadores:")
    print(nTotalUsers())

    print("\nTipos de Utilizadores que existem:")
    print(TiposDeUtilizadores())

    print("\nTipos de Utilizador que o andre@ua.pt é:")
    print(TipoDeUtilizador('andre@ua.pt'))

    print("\nFotografias que a joana@ua.pt tem:")
    print(getFotosUser('joana@ua.pt'))

    print("\nNúmero total de conceitos:")
    print(nTotalConcepts())

    print("\nNúmero total de percursos:")
    print(nTotalPath())

    print("\nNúmero total de fotografias:")
    print(nTotalFotos())

    print("\nNúmero total de turistas:")
    print(nTotalTipoUser('Turista'))

    print("\nNúmero total de admin:")
    print(nTotalTipoUser('Administrador'))

    print("\nNúmero total de ex - turistas:")
    print(nTotalTipoUser('ExTurista'))

    print("\nNúmero total de ex -admin:")
    print(nTotalTipoUser('ExAdministrador'))

    print("\nInformação sobre os conceitos:")
    print(infoConceitos())

    print("\nInformação sobre os percursos:")
    print(infoPercursos())

def queriesChico():
    print("\nUpdate fotografia - id, conceito, path:")
    print(updateFotografia(3, 1, 'DETI, Universidade de Aveiro', 'Biblioteca, Universidade de Aveiro', 'novopath'))

    print("\nObter todas as instancias de percurso feitas por um utilizador:")
    print(getTodasInstPercursoUser('joana@ua.pt'))

    print("\nObter todos os pontos de um percurso:")
    print(reconstruirPontosPercurso(1))

    print("\nUpdate estado de um percurso:")
    print(updateEstadoPercurso(1, 'Validado'))

    print("\nObter todas as fotografias associadas a uma instancia de um percurso")
    print(getFotografiasDeUmaInstPercurso(1))

    print("\nObter todos os pontos de uma instancia de um percurso:")
    print(reconstruirPontosInstPercurso(1))

    print("\nObter todos os percursos públicos:")
    print(todosPercursosDoTipo('Publico'))

    print("\nObter todos os percursos que contêm a substring a no título:")
    print(searchTodosPercursoContemSubString('a'))



@app.route('/')
def index():
    addInfoExemplo()
    addUsers()
   
    # queriesExemplo()
    #queriesChico()
    print(getInfoConceito('Biblioteca, Universidade de Aveiro'))
    print(getConceptRoutes('Biblioteca, Universidade de Aveiro'))
    return render_template('index.html',
                           totalusers = nTotalUsers(),
                           #totalAdmin = nTotalTipoUser('Administrador'),
                           #totalTuristas = nTotalTipoUser('Turista'),
                           totalAdmin=10,
                           totalTuristas = 80,
                           totalconcepts = nTotalConcepts(),
                           totalPaths = nTotalPath(),
                           conceitos = infoConceitos(),
                           percursos = infoPercursos(),
                           totalfotos = nTotalFotos())

if __name__ == '__main__':
    app.run(port=8000, host='0.0.0.0')
