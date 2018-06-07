from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy import text
from sqlalchemy.exc import IntegrityError


db = SQLAlchemy()

class Tipo(db.Model):
    """ Represents the table: Tipo
    
    Attributes:
        id: integer, is the primary key of the table Tipo.
        nome: string, corresponds to the Tipo name, for example: 'Turista' or 'Administrador'.

    """
    __tablename__ = 'tipo'
    id = db.Column('id', db.Integer, primary_key=True)
    nome = db.Column('nome', db.String(80), unique=True, nullable=False)

    # o id é fk da tabela do utilizador
    user = db.relationship('Utilizador',back_populates="tipo")

    def __init__(self, nome):
	"""Inits Tipo with nome."""
        self.nome = nome

class Utilizador(db.Model):
    """ Represents the table: Utilizador
    
    Attributes:
        email: string, user email is the primary key of the table Utilizador.
        tipoid: integer, is foreign key of table Utilizador.
        conc: Object of the class Conceito, because the user's email is the foreign key of the class Conceito.
        inst: Object of the class InstanciaPercurso, because the user's email is the foreign key of the class InstanciaPercurso.
        foto: Object of the class Fotografia, because the user's email is the foreign key of the class Fotografia.

    """
    __tablename__ = 'utilizador'
    email = db.Column('email', db.String(80), primary_key=True)

    # o id é fk da tabela do utilizador
    tipoid = db.Column('tipo', db.Integer, db.ForeignKey('tipo.id'))
    tipo = relationship('Tipo')

    # O email é fk da tabela conceito
    conc = db.relationship('Conceito', back_populates="emailcriador")

    # O email é fk da tabela percurso
    conc = db.relationship('Percurso', back_populates="emailuser")

    # O email é fk da tabela InstanciaPercurso
    inst = db.relationship('InstanciaPercurso', back_populates="emailuser")

    foto = db.relationship('Fotografia', back_populates="emailcriador")

    def __init__(self, email, tipoid):
	"""Inits Utilizador with email and tipoid."""
        self.email = email
        self.tipoid = tipoid

class Conceito(db.Model):
    """ Represents the table: Conceito

    Attributes:
        nomeconceito: string, concept nomeconceito is the primary key of the table Utilizador, corresponding to the name of the folders.
        emailc: string, user's email is the foreign key of the class Conceito.
        latitude: double, the latitude field is the latitude where the concept is located.
        longitude: double, the longitude field is the longitude where the concept is located.
        raio: double, the raio field is the radius from the location of the concept of where it is visible.
        descricao: string, the descricao field contains information about the concept.
        nome: string, is the name of the concept that is presented to the user.
        classificacao: integer, this field is the classification of the concept from 1 to 5.
        conc: Object of the class Fotografia, because the concept's nomeconceito is the foreign key of the class Fotografia.

    """
    __tablename__ = 'conceito'
    nomeconceito = db.Column('nomeconceito', db.String(80), primary_key=True)

    # O email é fk da tabela conceito
    emailc = db.Column('emailcriador', db.String(80), ForeignKey('utilizador.email'))
    emailcriador = relationship('Utilizador')

    latitude = db.Column('latitude', db.Float)
    longitude = db.Column('longitude', db.Float)
    raio = db.Column('raio', db.Float)
    descricao = db.Column('descricao', db.String(80))
    nome = db.Column('nome', db.String(200))
    classificacao = db.Column('classificacao', db.Float)

    conc = db.relationship('Fotografia', back_populates="nomeconceito")

    def __init__(self, nomeconceito, emailc, latitude=None, longitude=None,
                 raio=None, descricao=None, nome=None, classificacao=None):
    """Inits Conceito with nomeconceito, emailc, latitude, longitude, raio, descricao, nome and classificacao."""
        self.nomeconceito = nomeconceito
        self.emailc = emailc
        self.latitude = latitude
        self.longitude = longitude
        self.raio = raio
        self.descricao = descricao
        self.nome = nome
        self.classificacao = classificacao

class Percurso(db.Model):
    """ Represents the table: Percurso

    Attributes:
		id: integer, is the primary key of the table Percurso.
		emailc: string, user's email is the foreign key of the class Percurso.
		titulo: string, is the title of the route assigned by the user.
		descricao: string, the descricao field contains information about the route.
		estado: string, a route can have one of three states:
			Validated: Path created by an administrator or even though it was not, it was a public path and an administrator validated it.
			Audience: A user created it and gave them permissions to be public, however, it is not yet visible to other users.
			Private: A user has created it and has not given permission for this to be public, so it is not visible to other users, can only be made public.
		conc: Object of the class Ponto, because the route's id is the foreign key of the class Ponto.
		concc: Object of the class InstanciaPercurso, because the route's id is the foreign key of the class InstanciaPercurso.

    """
    __tablename__ = 'percurso'
    id = db.Column('id', db.Integer, primary_key=True)

    # O email é fk da tabela percurso
    emailc = db.Column('emailuser', db.String(80), ForeignKey('utilizador.email'))
    emailuser = relationship('Utilizador')

    titulo = db.Column('titulo', db.String(80), unique=True, nullable=False)
    descricao = db.Column('descricao', db.String(80))

    estado = db.Column('estado', db.String(80), nullable=False)

    # O id do percurso é fk da tabela ponto
    conc = db.relationship('Ponto', back_populates="idpercurso")

    # O id do percurso é fk da tabela InstanciaPercurso
    concc = db.relationship('InstanciaPercurso', back_populates="idpercurso")


    def __init__(self, emailc, titulo, estado, descricao=None):
	"""Inits Percurso with emailc, titulo, estado and descricao."""
        self.emailc=emailc
        self.titulo=titulo
        self.descricao=descricao
        self.estado=estado

class Ponto(db.Model):
    """ Represents the table: Ponto

    Attributes:
		idponto: integer, is the primary key of the table Ponto.
		latitude: double, the latitude field is the latitude where the point is located.
        longitude: double, the longitude field is the longitude where the point is located.
		idperc: integer, is foreign key of table Ponto.

    """
    __tablename__ = 'ponto'
    idponto = db.Column('idponto', db.Integer, primary_key=True)
    latitude = db.Column('latitude', db.Float, nullable=False)
    longitude = db.Column('longitude', db.Float, nullable=False)

    # O id do percurso é fk da tabela ponto
    idperc = db.Column('idpercurso', db.Integer, ForeignKey('percurso.id'))
    idpercurso = relationship('Percurso')

    def __init__(self, latitude, longitude, idperc):
		"""Inits Ponto with latitude, longitude and idperc."""
        self.latitude=latitude
        self.longitude=longitude
        self.idperc=idperc

class InstanciaPercurso(db.Model):
    """ Represents the table: InstanciaPercurso

    Attributes:
		id: integer, is the primary key of the table InstanciaPercurso.
		emailc: string, user's email is the foreign key of the class Percurso.
		idperc: integer, is foreign key of table InstanciaPercurso.
		datainicio: date, is the start date of the route
		datafim:date, is the end date of the course.
		classificacao: integer, this field is the classification of the concept from 1 to 5.
		instid: Object of the class InstanciaPercurso, because the route's id is the foreign key of the class InstanciaPercurso.

    """
    __tablename__ = 'instanciapercurso'

    id = db.Column('id', db.Integer, primary_key=True)

    # O email é fk da tabela InstanciaPercurso
    emailc = db.Column('emailuser', db.String(80), ForeignKey('utilizador.email'))
    emailuser = relationship('Utilizador')

    # O id do percurso é fk da tabela InstanciaPercurso
    idperc = db.Column('idpercurso', db.Integer, ForeignKey('percurso.id'))
    idpercurso = relationship('Percurso')

    datainicio = db.Column('datainicio',db.Date)
    datafim = db.Column('datafim', db.Date)

    classificacao = db.Column('classificacao', db.Float)

    instid = db.relationship('Fotografia', back_populates="idinstpercurso")

    def __init__(self, emailc, idperc, datainicio, datafim, classificacao=None):
        """Inits InstanciaPercurso with emailc, idperc, datainicio, datafim and classificacao."""
        self.emailc=emailc
        self.idperc=idperc
        self.datafim=datafim
        self.classificacao=classificacao
        self.datainicio=datainicio

class Fotografia(db.Model):	# ACABAR
    """ Represents the table: Fotografia

    Attributes:
		id: integer, is the primary key of the table Fotografia.
		nomeconc: string, is foreign key of table Conceito.
		emailinst: string, user's email is the foreign key of the class Fotografia.
		latitude: double, the latitude field is the latitude where the photograph was taken.
        longitude: double, the longitude field is the longitude where the photograph was taken.
		path: string,is where the photo is stored on the server.
		idinstperc: integer, 
		datafoto: date
		feedback: integer
		estado:
		classificacaotensorflow:
		tempotensorflow:

		emailc: string, user's email is the foreign key of the class Percurso.
		idperc: integer, is foreign key of table InstanciaPercurso.
		datainicio: date, is the start date of the route
		datafim:date, is the end date of the course.
		classificacao: integer, this field is the classification of the concept from 1 to 5.

    """

    __tablename__ = 'fotografia'
    id = db.Column('idfoto', db.Integer, primary_key=True, autoincrement=True)

    nomeconc = db.Column('nomeconceito', db.String(80), ForeignKey('conceito.nomeconceito'))
    nomeconceito = relationship('Conceito')

    emailinst = db.Column('emailcriador', db.String(80), ForeignKey('utilizador.email'))
    emailcriador = relationship('Utilizador')

    latitude = db.Column('latitude', db.Float, nullable=False)
    longitude = db.Column('longitude', db.Float, nullable=False)

    path = db.Column('path', db.String(80), nullable=False)

    idinstperc = db.Column('idinstpercurso', db.Integer, ForeignKey('instanciapercurso.id'))
    idinstpercurso = relationship('InstanciaPercurso')

    datafoto = db.Column('datafoto', db.Date, nullable=False)

    feedback = db.Column('feedback', db.Float)

    estado = db.Column('estado', db.String(80), nullable=False)

    classificacaotensorflow = db.Column('classificacaotensorflow', db.Float)

    tempotensorflow = db.Column('tempotensorflow', db.Float)

    def __init__(self, id, nomeconc, emailinst, latitude, longitude, path, idinstperc, datafoto, feedback, estado, classificacaotensorflow, tempotensorflow):
        self.id=id
        self.nomeconc=nomeconc
        self.emailinst=emailinst
        self.latitude=latitude
        self.longitude=longitude
        self.path=path
        self.idinstperc=idinstperc
        self.datafoto=datafoto
        self.feedback=feedback
        self.estado=estado
        self.classificacaotensorflow=classificacaotensorflow
        self.tempotensorflow=tempotensorflow

# Adição dos tipos de Utilizadores
def addTipo(nome):
    tipo = Tipo(nome)
    db.session.add(tipo)
    db.session.commit()
    return tipo

# Adição dos Utilizadores
def addUtilizador(email, tipoid):
    user = Utilizador(email, tipoid)
    db.session.add(user)
    db.session.commit()
    return user

# Adição de Conceitos
def addConceito(nomeconceito, emailcriador, latitude=None, longitude=None,
                raio=None, descricao=None, nome=None, classificacao=None):
    conceito = Conceito(nomeconceito, emailcriador, latitude, longitude, raio, descricao, nome, classificacao)
    db.session.add(conceito)
    db.session.commit()
    return conceito

def deleteConceito(id):
    conc = db.session.query(Conceito).get(id)
    db.session.delete(conc)
    db.session.commit()

def addPercurso(emailc, titulo, estado, descricao=None):
    percurso = Percurso(emailc, titulo, estado, descricao)
    db.session.add(percurso)
    db.session.commit()
    return percurso

def addPonto(latitude, longitude, idperc=None):
    ponto = Ponto(latitude, longitude, idperc)
    db.session.add(ponto)
    db.session.commit()
    return ponto

def addInstanciaPercurso(emailc, idperc, datainicio, datafim, classificacao=None):
    inst = InstanciaPercurso(emailc, idperc, datainicio, datafim, classificacao)
    db.session.add(inst)
    db.session.commit()
    return inst

def addFotografia(id, nomeconc, emailinst, latitude, longitude, path, idperc, datafoto, feedback, estado, classificacaotensorflow, tempotensorflow):
    #try:
    foto = Fotografia(id, nomeconc, emailinst, latitude, longitude, path, idperc, datafoto, feedback, estado, classificacaotensorflow, tempotensorflow)
    db.session.add(foto)
    db.session.commit()
    return foto
    #except IntegrityError as e:
    #    db.session.rollback()
    #    return None
    

def nTotalUsers():
    return Utilizador.query.count()

def TiposDeUtilizadores():
    sql = text('select id, nome from tipo')
    result = db.engine.execute(sql)
    tipos = []
    for row in result:
        tipos.append((row[0],row[1]))
    return tipos

def TipoDeUtilizador(em):
    sql = text('select tipo.nome from utilizador join tipo on tipo.id=utilizador.tipo where email=\'' + em + '\'')
    result = db.engine.execute(sql)
    for row in result:
        return row[0]

def getFotosUser(em):
    sql = text('select * from fotografia where emailcriador=\'' + em + '\'')
    result = db.engine.execute(sql)
    fotos = []
    for row in result:
        fotos.append((row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9], row[10], row[11]))
    return fotos

def nTotalConcepts():
    return Conceito.query.count()

def nTotalPath():
    return Percurso.query.count()

def nTotalFotos():
    return Fotografia.query.count()

def nTotalTipoUser(tipo):
    sql = text('select count(utilizador.email) from utilizador join tipo on tipo.id=utilizador.tipo where tipo.nome=\'' + tipo + '\'')
    result = db.engine.execute(sql)
    for row in result:
        return row[0]

def infoConceitos():
    sql = text('SELECT conceito.nomeconceito, count(fotografia.nomeconceito), avg(fotografia.classificacaotensorflow),'
                   ' avg(fotografia.feedback), avg(fotografia.tempotensorflow) '
                   ' FROM conceito'
                   ' JOIN fotografia on fotografia.nomeconceito=conceito.nomeconceito'
                   ' GROUP BY conceito.nomeconceito'
                   ' ORDER BY count(fotografia.nomeconceito);')
    result = db.engine.execute(sql)

    totalfotos = nTotalFotos()
    concs = []
    indice = 1

    for row in result:
        concs.append((indice, row[0], row[1]/totalfotos*100, row[1], row[2], row[3], row[4]))
        indice+=1
    return concs

def infoPercursos():
    sql = text('SELECT percurso.titulo, count(instanciapercurso.idpercurso), avg(instanciapercurso.classificacao)'
               ' FROM percurso'
               ' JOIN instanciapercurso on instanciapercurso.idpercurso=percurso.id'
               ' GROUP BY percurso.titulo'
               ' ORDER BY count(instanciapercurso.idpercurso)')

    result = db.engine.execute(sql)
    concs = []
    indice = 1
    for row in result:
        concs.append((indice, row[0], row[1], row[1], 0, row[2], 0))
        indice+=1
    return concs

def updateFotografia(id, conceito, path):
    #sql = text('update fotografia set idfoto = \'' + str(id) + '\', nomeconceito = \'' + conceito + '\', path = \'' + newpath + '\' where idfoto = \'' + str(newid) + '\' and nomeconceito = \'' + newconceito + '\'')
    #db.engine.execute(sql)
    #return
    foto = db.session.query(Fotografia).get(id)
    if conceito is not None:
        foto.nomeconc = conceito
    foto.path = path
    db.session.commit()
    return

def updateFotoByPath(path, newpath):
    foto = db.session.query(Fotografia).filter(Fotografia.path == path).first()
    foto.path = newpath
    db.session.commit()
    return foto

def getFoto(id):
    #sql = text('select from fotografia where idfoto=\'' + str(id) + '\'')
    #result = db.engine.execute(sql)
    foto = db.session.query(Fotografia).get(id)
    #foto = None
    #for row in result:
    #    foto = Fotografia(row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9], row[10], row[11])
    return foto

def deleteFoto(path):
    foto = db.session.query(Fotografia).filter(Fotografia.path == path).delete()
    db.session.commit()

def getTodasInstPercursoUser(em):
    sql = text('select percurso.titulo, percurso.id, instanciapercurso.datainicio, instanciapercurso.datafim,  \
        instanciapercurso.classificacao, percurso.estado \
        from instanciapercurso \
        join percurso on instanciapercurso.idpercurso = percurso.id \
        where instanciapercurso.emailuser=\'' + em + '\'')
    result = db.engine.execute(sql)
    inst = []
    for row in result:
        inst.append((row[0], row[1], row[2], row[3], row[4], row[5]))
    return inst

def reconstruirPontosPercurso(id):
    sql = text('select ponto.idponto, ponto.latitude, ponto.longitude from ponto \
        join percurso on ponto.idpercurso = percurso.id \
        where percurso.id =\'' + str(id) + '\'')
    result = db.engine.execute(sql)
    pnt = []
    for row in result:
        pnt.append((row[0], row[1], row[2]))
    return pnt

def updateEstadoPercurso(id, novoestado):
    sql = text('update percurso set estado=\'' + novoestado + '\' where id=\'' + str(id) + '\'')
    db.engine.execute(sql)
    return

def getFotografiasDeUmaInstPercurso(id):
    sql = text('select * from fotografia where idinstpercurso=\'' + str(id) + '\'')
    result = db.engine.execute(sql)
    temp = []
    for row in result:
        temp.append(( row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9], row[10], row[11]))
    return temp

def reconstruirPontosInstPercurso(id):
    sql = text('select ponto.idponto, ponto.latitude, ponto.longitude from ponto \
        join percurso on ponto.idpercurso = percurso.id \
        join instanciapercurso on instanciapercurso.idpercurso = percurso.id \
        where instanciapercurso.id = \'' + str(id) + '\'')
    result = db.engine.execute(sql)
    pnt = []
    for row in result:
        pnt.append((row[0], row[1], row[2]))
    return pnt

def todosPercursosDoTipo(estado):
    sql = text('select * from percurso where estado = \'' + estado + '\'')
    result = db.engine.execute(sql)
    pnt = []
    for row in result:
        pnt.append((row[0], row[1], row[2], row[3], row[4]))
    return pnt

def searchTodosPercursoContemSubString(substring):
    sql = text('SELECT * FROM percurso WHERE titulo LIKE \'%' + substring + '%\'')
    result = db.engine.execute(sql)
    pnt = []
    for row in result:
        pnt.append((row[0], row[1], row[2], row[3], row[4]))
    return pnt

def getInfoConceito(nomeConceito):
    sql = text('SELECT conceito.descricao, fotografia.path'
               ' FROM conceito'
               ' JOIN fotografia on fotografia.nomeconceito=conceito.nomeconceito'
               ' WHERE conceito.nomeconceito= \'' + nomeConceito + '\''
               ' LIMIT 3;')

    result = db.engine.execute(sql)

    totalfotos = nTotalFotos()
    concs = []
    indice = 1

    for row in result:
        if(indice==1):
            concs.append(row[0])
            concs.append(row[1])
        else:
            concs.append(row[1])
        indice+=1
    return concs

def getConceptRoutes(nomeConceito):
    sql = text('SELECT distinct percurso.id, percurso.titulo, percurso.descricao, percurso.estado'
               ' FROM percurso'
               ' JOIN instanciapercurso on instanciapercurso.idpercurso = percurso.id'
               ' JOIN fotografia on fotografia.idinstpercurso = instanciapercurso.id'
               ' WHERE fotografia.nomeconceito= \'' + nomeConceito + '\'' + ';')

    result = db.engine.execute(sql)

    concs = []

    for row in result:
        concs.append((row[0], row[1], row[2], row[3]))
    return concs

def fotosPorConceito():
    sql = text('SELECT count(fotografia.nomeconceito)'
                   ' FROM conceito'
                   ' JOIN fotografia on fotografia.nomeconceito=conceito.nomeconceito'
                   ' GROUP BY conceito.nomeconceito'
                   ' ORDER BY count(fotografia.nomeconceito);')
    result = db.engine.execute(sql)

    ntot = int(nTotalFotos())

    values = []

    for row in result:
        values += [(int(row[0])/ntot)*100]
    return values

def conc():
    sql = text('SELECT conceito.nomeconceito, count(fotografia.nomeconceito)'
                   ' FROM conceito'
                   ' JOIN fotografia on fotografia.nomeconceito=conceito.nomeconceito'
                   ' GROUP BY conceito.nomeconceito'
                   ' ORDER BY count(fotografia.nomeconceito);')
    result = db.engine.execute(sql)


    values = []

    for row in result:
        values += [row[0]]
    return values

def nDesconhConhe():
    sql = text('SELECT count(fotografia.nomeconceito), conceito.nomeconceito'
                   ' FROM conceito'
                   ' JOIN fotografia on fotografia.nomeconceito=conceito.nomeconceito'
                   ' where conceito.nomeconceito=\'desconhecido\''
                   ' GROUP BY conceito.nomeconceito'
                   ' ORDER BY count(fotografia.nomeconceito);')
    result = db.engine.execute(sql)

    ntot = int(nTotalFotos())

    values = []

    for row in result:
        values += [(int(row[0])/ntot)*100]                  # desc
        values += [((ntot-int(row[0]))/ntot)*100]       # conh
    return values

def percFeedback():
    sql1 = text('SELECT count(fotografia.feedback), avg(fotografia.feedback) FROM fotografia where fotografia.nomeconceito <> \'desconhecido\';')
    sql2 = text('SELECT count(fotografia.nomeconceito) FROM fotografia where fotografia.nomeconceito <> \'desconhecido\';')

    result1 = db.engine.execute(sql1) # nº de fotos com feedback e a média do feedback
    result2 = db.engine.execute(sql2) # nº de fotos

    for row in result1:
        fcf = row[0]
        med = row[1]

    for row in result2:
        totf = row[0]

    return ((fcf/totf*100), med)
