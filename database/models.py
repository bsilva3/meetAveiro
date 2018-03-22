from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship

db = SQLAlchemy()

class Tipo(db.Model):
    __tablename__ = 'tipo'
    id = db.Column('id', db.Integer, primary_key=True)
    nome = db.Column('nome', db.String(80), unique=True, nullable=False)
    user = db.relationship('Utilizador',back_populates="tipo")

    def __init__(self, nome):
        self.nome = nome

class Utilizador(db.Model):
    __tablename__ = 'utilizador'
    email = db.Column('email', db.String(80), primary_key=True)
    tipoid = db.Column('tipo', db.Integer, db.ForeignKey('tipo.id'))
    tipo = relationship('Tipo')

    def __init__(self, email, tipoid):
        self.email = email
        self.tipoid = tipoid

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
