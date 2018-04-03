from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy import text

db = SQLAlchemy()

class Tipo(db.Model):
    __tablename__ = 'tipo'
    id = db.Column('id', db.Integer, primary_key=True)
    nome = db.Column('nome', db.String(80), unique=True, nullable=False)

    # o id é fk da tabela do utilizador
    user = db.relationship('Utilizador',back_populates="tipo")

    def __init__(self, nome):
        self.nome = nome

class Utilizador(db.Model):
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
        self.email = email
        self.tipoid = tipoid

class Conceito(db.Model):
    __tablename__ = 'conceito'
    nomeconceito = db.Column('nomeconceito', db.String(80), primary_key=True)

    # O email é fk da tabela conceito
    emailc = db.Column('emailcriador', db.String(80), ForeignKey('utilizador.email'))
    emailcriador = relationship('Utilizador')

    latitude = db.Column('latitude', db.Float)
    longitude = db.Column('longitude', db.Float)
    raio = db.Column('raio', db.Float)
    descricao = db.Column('descricao', db.String(80))
    classificacao = db.Column('classificacao', db.Float)

    conc = db.relationship('Fotografia', back_populates="nomeconceito")

    def __init__(self, nomeconceito, emailc, latitude=None, longitude=None,
                 raio=None, descricao=None, classificacao=None):
        self.nomeconceito = nomeconceito
        self.emailc = emailc
        self.latitude = latitude
        self.longitude = longitude
        self.raio = raio
        self.descricao = descricao
        self.classificacao = classificacao

class Percurso(db.Model):
    __tablename__ = 'percurso'
    id = db.Column('id', db.Integer, primary_key=True)

    # O email é fk da tabela percurso
    emailc = db.Column('emailuser', db.String(80), ForeignKey('utilizador.email'))
    emailuser = relationship('Utilizador')

    titulo = db.Column('titulo', db.String(80), unique=True, nullable=False)
    descricao = db.Column('descricao', db.String(80))
    classificacao = db.Column('classificacao', db.Float)
    estado = db.Column('estado', db.String(80), nullable=False)

    # O id do percurso é fk da tabela ponto
    conc = db.relationship('Ponto', back_populates="idpercurso")

    # O id do percurso é fk da tabela InstanciaPercurso
    concc = db.relationship('InstanciaPercurso', back_populates="idpercurso")


    def __init__(self, emailc, titulo, estado, descricao=None, classificacao=None):
        self.emailc=emailc
        self.titulo=titulo
        self.descricao=descricao
        self.classificacao=classificacao
        self.estado=estado

class Ponto(db.Model):
    __tablename__ = 'ponto'
    idponto = db.Column('idponto', db.Integer, primary_key=True)
    latitude = db.Column('latitude', db.Float, nullable=False)
    longitude = db.Column('longitude', db.Float, nullable=False)

    # O id do percurso é fk da tabela ponto
    idperc = db.Column('idpercurso', db.Integer, ForeignKey('percurso.id'))
    idpercurso = relationship('Percurso')

    def __init__(self, latitude, longitude, idperc):
        self.latitude=latitude
        self.longitude=longitude
        self.idperc=idperc

class InstanciaPercurso(db.Model):
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

    instid = db.relationship('Fotografia', back_populates="idinstpercurso")

    def __init__(self, emailc, idperc, datainicio, datafim):
        self.emailc=emailc
        self.idperc=idperc
        self.datafim=datafim
        self.datainicio=datainicio

class Fotografia(db.Model):
    __tablename__ = 'fotografia'
    id = db.Column('idfoto', db.Integer, primary_key=True)

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

    def __init__(self, nomeconc, emailinst, latitude, longitude, path, idinstperc, datafoto, feedback):
        self.nomeconc=nomeconc
        self.emailinst=emailinst
        self.latitude=latitude
        self.longitude=longitude
        self.path=path
        self.idinstperc=idinstperc
        self.datafoto=datafoto
        self.feedback=feedback

# Adição dos tipos de Utilizadores
def addTipo(nome):
    tipo = Tipo(nome)
    db.session.add(tipo)
    db.session.commit()

# Adição dos Utilizadores
def addUtilizador(email, tipoid):
    user = Utilizador(email, tipoid)
    db.session.add(user)
    db.session.commit()

# Adição de Conceitos
def addConceito(nomeconceito, emailcriador, latitude=None, longitude=None,
                raio=None, descricao=None, classificacao=None):
    conceito = Conceito(nomeconceito, emailcriador, latitude, longitude, raio, descricao, classificacao)
    db.session.add(conceito)
    db.session.commit()

def addPercurso(emailc, titulo, estado, descricao=None, classificacao=None):
    percurso = Percurso(emailc, titulo, estado, descricao, classificacao)
    db.session.add(percurso)
    db.session.commit()

def addPonto(latitude, longitude, idperc=None):
    ponto = Ponto(latitude, longitude, idperc)
    db.session.add(ponto)
    db.session.commit()

def addInstanciaPercurso(emailc, idperc, datainicio, datafim):
    inst = InstanciaPercurso(emailc, idperc, datainicio, datafim)
    db.session.add(inst)
    db.session.commit()

def addFotografia(nomeconc, emailinst, latitude, longitude, path, idperc, datafoto, feedback):
    foto = Fotografia(nomeconc, emailinst, latitude, longitude, path, idperc, datafoto, feedback)
    db.session.add(foto)
    db.session.commit()

def countUtilizadores():
    return Utilizador.query.count()

def UtilizadoresQueSaoAdministradores():
    sql = text('select id, nome from tipo')
    result = db.engine.execute(sql)
    tipos = []
    for row in result:
        tipos.append((row[0],row[1]))
    return tipos

def TipoDeUtilizador(em):
    sql = text('select tipo from utilizador where email=\'' + em + '\'')
    result = db.engine.execute(sql)
    for row in result:
        return row[0]

def getFotosUser(em):
    sql = text('select * from fotografia where emailcriador=\'' + em + '\'')
    result = db.engine.execute(sql)
    fotos = []
    for row in result:
        fotos.append((row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8]))
    return fotos