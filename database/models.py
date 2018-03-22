from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import ForeignKey
from sqlalchemy.orm import relationship

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

    def __init__(self, emailc, titulo, estado, descricao=None, classificacao=None):
        self.emailc=emailc
        self.titulo=titulo
        self.descricao=descricao
        self.classificacao=classificacao
        self.estado=estado

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