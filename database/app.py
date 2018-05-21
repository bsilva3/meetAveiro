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
    #addUtilizador('bjpsilva@hotmail.com', 1)
    #addUtilizador('joao@outlook.com',1)
    #addUtilizador('youngf3@live.com.pt',1)
    #addUtilizador('joaoserpa1997@outlook.com',1)
    #addUtilizador('admin@ua.pt',1)
    #addUtilizador('joana@ua.pt', 2)
    #addUtilizador('almeidaanafilipa999@gmail.com', 1)
    #addUtilizador('franciscomanuelmatos@gmail.com', 1)
    addUtilizador('franciscomatos@ua.pt', 1)


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
    addConceito('biblioteca', 'admin@ua.pt', 40.6310031, -8.659642599999984, 1, 'Localizada no centro do Campus Universitário da UA, a Biblioteca da Universidade de Aveiro constitui-se como um agradável local para leitura, estudo e pesquisa acessível a toda a comunidade académica. Nela são disponibilizados os necessários recursos informativos que servem de suporte ao ensino, aprendizagem e à investigação na Universidade de Aveiro. Dispõe de 1000 lugares de leitura, repartidos por três pisos, incluindo gabinetes de estudo individuais, coletivos, audiovisuais e um espaço de leitura informal.', 'Biblioteca', 4.5)
    addConceito('deti', 'admin@ua.pt', 40.633175, -8.659496, 0.3, 'O Departamento de Eletrónica, Telecomunicações e Informática (DETI) foi fundado em 1974, com o nome de Departamento de Eletrónica e Telecomunicações, tendo sido um dos primeiros departamentos a iniciar atividade após a criação da Universidade de Aveiro em 1973. Em 2006 foi alterada a sua designação por forma a espelhar a atividade existente no Departamento na área da Informática.', 'DETI',4.9)
    addConceito('dmat', 'admin@ua.pt', 40.630349, -8.658214, 0.6, 'O Departamento de Matemática (DMat) foi criado em 1976 pouco tempo depois da fundação da Universidade de Aveiro. Este departamento encontra-se integrado num campus universitário harmonioso, verdadeira galeria da moderna arquitetura portuguesa. O DMat está sedeado desde 1993 num edifício com aproximadamente  4500 m2 projetado pelo arquiteto José Maria Lopo Prata, com linhas exteriores envolventes e elegantes, delimitando um espaço acolhedor que se deixa invadir pela luz proveniente de uma cúpula.', 'Departamento de Matemática', 4.3)
    addConceito('reitoria', 'admin@ua.pt', 40.63118, -8.657398, 0.5, 'A Universidade de Aveiro (UA) é uma fundação pública com regime de direito privado que tem como missão a intervenção e desenvolvimento da formação graduada e pós-graduada, a investigação e a cooperação com a sociedade. Criada em 1973, rapidamente se transformou numa das mais dinâmicas e inovadoras universidades do país. Frequentada por cerca de 15.000 alunos em programas de graduação e pós-graduação, a UA desde cedo assumiu um papel de relevância no panorama universitário do país, inserindo-se no grupo da frente no que diz respeito à qualidade das infraestruturas que oferece, à qualidade da sua investigação e à excelência do seu corpo docente.', 'Reitoria', 4.3)
    
    addConceito('bugas', 'admin@ua.pt', 40.642599, -8.649559, 1, 'Loja das Bugas', 'Loja das Bugas', 4.5)
    addConceito('cantina', 'admin@ua.pt', 40.630797222222, -8.6591805555556, 0.3, 'O edifício onde funciona o Refeitório de Santiago compreende um projecto da autoria do arquitecto Rebello de Andrade (constituído por uma área de 1200 metros quadrados). O Refeitório de Santiago, constituído por duas salas de 400 lugares cada, situa-se no edifício central dos Serviços de Acção Social, onde podem ser fornecidas cerca de 4.000 refeições por dia.', 'Cantina Santiago, Universidade de Aveiro',4.9)
    addConceito('centro_cultural_dos_congressos', 'admin@ua.pt', 40.639141, -8.644020, 0.6, 'O Centro Cultural e de Congressos (CCCA) é um espaço profundamente ligado à cidade. A sua localização, central, faz dele um edifício referência e um marco para quem visita Aveiro. A modernidade das funções inserida na beleza e tradição da Antiga Fábrica Jeronymo Pereira de Campos fazem deste edifício um local óptimo para a realização de todo o tipo de eventos.', 'Centro Cultural e de Congressos', 4.3)
    addConceito('complexo_pedagogico', 'admin@ua.pt', 40.630383, -8.655780, 0.5, 'Complexo Pedagógico, Tecnológico e Científico da Universidade de Aveiro', 'Complexo Pedagógica, Universidade de Aveiro', 4.3)
    addConceito('convento_museu_santa_joana', 'admin@ua.pt', 40.640305, -8.651056, 1, 'As notáveis coleções do Museu de Aveiro de temática ou função sacra integram núcleos de pintura, escultura, talha, azulejaria, ourivesaria, mobiliário e paramentaria. São na maioria provenientes do Convento de Jesus ou de outros conventos extintos da cidade e do País e documentam épocas diversas, desde o século XV ao século XX, com relevância para o período barroco.', 'Museu de Aveiro - Santa Joana', 4.5)
    addConceito('degeit', 'admin@ua.pt', 40.631779, -8.656965, 0.3, 'Departamento de Economia, Gestão e Engenharia Industrial da Universidade de Aveiro', 'Departamento de Economia, Gestão e Engenharia Industrial',4.9)
    addConceito('estacao_de_comboios', 'admin@ua.pt', 40.644323, -8.640550, 0.6, 'A estação da CP de Aveiro é um dos belos edifícios que compõem esta capital da arte nova e é uma autêntica homenagem ao azulejo Português, de confecção local tão abundante. Vale a pena ver com calma todos os painéis que compõem toda a construção pois cada um tem uma mensagem implícita que cada qual pode tirar as suas próprias conclusões.', 'Estação Ferroviária de Aveiro', 4.3)
    addConceito('estatua_manuel_firmino', 'admin@ua.pt', 40.642677, -8.648802, 0.5, 'Busto em homenagem a Manuel Firmino', 'Estátua de Manuel Firmino', 4.3)
    addConceito('estatua_princesa_santa_joana', 'admin@ua.pt', 40.6310031, -8.659642599999984, 1, 'Filha de D. Afonso V, rei de Portugal, a Princesa Santa Joana nasceu na cidade de Lisboa, em 6 de fevereiro de 1452. Órfã de mãe aos quatro anos, procurou desde menina praticar sempre a mais edificante virtude, tanto no desprendimento das grandezas da corte e das vaidades do mundo, como na profunda piedade e vida interior, na sincera devoção à paixão de Cristo e na desinteressada caridade a favor dos pobres. Como manifestação de tal género de sentir e viver, escolheu para seu distintivo a coroa de espinhos. Aos dezanove anos, recolheu-se no mosteiro de Odivelas; mas, em 4 de agosto de 1472, mudou para o mosteiro de Jesus da então vila de Aveiro, a que ela chamava «a sua Lisboa, a pequena». Aí viveu em austeridade e fervor religioso, sob o hábito dominicano, até ao seu falecimento, ocorrido em 12 de maio de 1490; tinha trinta e oito anos de idade. Foi sepultada no coro do convento. Logo após a sua morte, o povo de Aveiro começou a venerá-la por santa, considerando-a mesmo, mais tarde, como protetora da cidade; o seu culto foi confirmado pelo papa Inocêncio XII, em 4 de abril de 1693. O papa Paulo VI, em 5 de janeiro de 1965, constituiu-a padroeira principal da cidade e da diocese de Aveiro.', 'Estátua de Santa Joana Princesa', 4.5)
    addConceito('forum_aveiro', 'admin@ua.pt', 40.6409015, -8.6515224, 0.3, 'O Forum Aveiro, gerido pela CBRE Asset Services, possui uma forte característica diferenciadora, sendo o 1º centro comercial ao ar livre do país e representa um novo tipo de conceito de centros comerciais em Portugal, onde os jardins, as áreas verdes e os espaços públicos predominam.', 'Forum Aveiro',4.9)
    addConceito('ieeta', 'admin@ua.pt', 40.6331317, -8.6601875, 0.6, 'O Instituto de Engenharia Electrónica e Telemática de Aveiro - IEETA é uma associação científica e técnica sem fins lucrativos, tendo como missão a investigação multidisciplinar e desenvolvimento avançado em  Electrónica e Telemática, integrados na comunidade de investigação cientifica internacional e contribuindo para o desenvolvimento tecnológico e social nacionais. Integram-se neste Instituto os elementos do DETI-UA que pertenciam ao INESC - Unidade de Aveiro, bem como à Unidade de Investigação 127/94 INESC - Pólo da UA.', ' Instituto de Engenharia Electrónica e Telemática de Aveiro (IEETA)', 4.3)
    addConceito('jardim_galitos', 'admin@ua.pt', 40.6371452, -8.6412406, 0.5, 'O Jardim dos Galitos é um bom local para passeios em família. Tem um parque para as crianças, existe um bar ao lado, já para não falar nos campos de ténis e basquetebol ao ar livre.', 'Jardim dos Galitos', 4.3)
    addConceito('mercado_manuel_firmino', 'admin@ua.pt', 40.6416997, -8.6488876, 1, 'O Mercado Manuel Firmino é um mercado retalhista, instalado em recinto próprio e coberto, destinando-se os lugares de venda à comercialização de fruta, produtos hortícolas, sementes, flores, plantas, carnes, peixes e muitos outros... Está também vocacionado para as demais atividades autorizadas pela Câmara Municipal de Aveiro.', 'Mercado Manuel Firmino', 4.5)
    addConceito('parque_drinks', 'admin@ua.pt', 40.63770034, -8.65513497, 0.6, 'O parque do drinks é um local bastante frequentado por estudantes universitários. Possui máquinas para fazer exercício físico, campos para atividades ao ar livre... ', 'Parque do Drinks', 4.3)
    addConceito('parque_da_macaca', 'admin@ua.pt', 40.63654705, -8.65331663, 0.5, 'O parque Dom Pedro Infante, mais conhecido como parque da macaca é um ótimo local para descançar no meio da natureza. Construído na antiga propriedade de frades franciscanos, foi preparado a partir de 1862 na zona que pertencia ao Convento de Santo António. Aproveitou-se a ribeira que atravessava o parque para se desenvolver um espaço com lagos e fontes inserido no arvoredo envolvente. Possui coreto de Arte Nova tardia, escadaria com fonte, cascata e alguns painéis de azulejos, bem como Avenida das Tílias e Casa de Chá.', 'Parque Dom Pedro Infante', 4.3)
    addConceito('ponte_dos_lacos', 'admin@ua.pt', 40.64157707, -8.65003158, 1, 'A chamada Ponte dos Laços de Amizade é uma das travessias sobre a Ria de Aveiro de acesso ao centro comercial Fórum Aveiro. O significado da Ponte dos Laços de Amizade, criada em 2014 por dois estudantes da Universidade de Aveiro, é um hino às amizades que por cá nasceram ou passaram, bem como um símbolo do carinho que os habitantes nutrem pela cidade.', 'Ponte dos Laços de Amizade', 4.5)
    addConceito('ponte_pedonal_parque_macaca_drinks', 'admin@ua.pt', 40.63653796, -8.65410014, 0.3, 'Esta ponte pedonal liga dois dos parques mais importantes da cidade de Aveiro, o Parque Dom Pedro Infante (mais conhecido como parque da macaca) e o Parque do Drinks. Numa avenida tão movimentada como a avenida Artur Ravara, as pessoas têm uma forma mais segura de atravessar entre os dois parques.', 'Ponte pedonal parque da macaca e do drinks',4.9)
    addConceito('se_de_aveiro', 'admin@ua.pt', 40.63960346, -8.65026632, 0.5, 'A Sé de Aveiro situa-se no antigo convento dominicano, conhecido por ter sido a primeira comunidade religiosa a instalar-se na cidade. A igreja chama de imediato a atenção pela soberba fachada, com as suas imagens das Virtudes Humanas e a imponente torre sineira. No espaço interior domina o branco da pedra calcária, as várias capelas são decoradas por conjuntos de talha, pedra e azulejo de distintas épocas.', 'Sé de Aveiro', 4.3)
    addConceito('torre_de_agua', 'admin@ua.pt', 40.62902601, -8.65545995, 0.5, 'Com a altura de cerca de 30 metros, a torre do depósito de água perfila-se bem acima do conjunto, em torno da Galeria, de que é signo recortado no céu, mas também marcação do seu final frente aos húmidos que envolvem o braço da ria que separa a zona de Santiago da zona da Agra do Castro. A caixa apresenta-se na evidência de um paralelepípedo, mas o suporte foi concebido diferentemente, pois é constituído por uma lâmina complanar com o lado menor do paralelepípedo e por um cilindro. A primeira relaciona directamente a torre com o conjunto da Galeria, pois alinha-se com o plano das fachadas dos edifícios do seu lado nordeste e o segundo é simultaneamente uma coluna e a expressão de uma conduta. Álvaro Siza tornou complexo algo que, à primeira vista, seria um mero equipamento de um sistema de abastecimento de águas, mas sem o complicar com inúteis especulações formais. Partiu conceptualmente do óbvio, filtrou-o criticamente e exercitou a sua sensibilidade.', 'Torre de água', 4.3)

    addConceito('ria_de_aveiro', 'admin@ua.pt', None, None, 0.6, 'Aveiro é considerado a Veneza de PorTugal devido à ria. A ria de Aveiro, ou foz do Vouga, é como se chama o estuário do rio Vouga, o qual se estende pelo interior do território português, paralelamente ao mar, numa distância de 45 quilómetros e a uma largura máxima de 11 quilómetros, no sentido este–oeste de Ovar a Mira. Do seu entorno constam Aveiro, Ílhavo, Gafanha da Nazaré, Estarreja, Ovar, Murtosa, Vagos e Mira.', 'Ria de Aveiro', 4.3)
    addConceito('desconhecido', 'admin@ua.pt', None, None, 0.0, '', 'Desconhecido', 0.0)


def addPercursosExemplo():
    addPercurso('joana@ua.pt', 'Conhece a UA', 'Validado','Conhece a Universidade de Aveiro')
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
    addFotografia(None, 'biblioteca', 'joana@ua.pt',  40.6310031, -8.659642599999984, '../../../../treino/biblioteca/1.jpg', 1, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'biblioteca', 'joana@ua.pt',  40.6310031, -8.659642599999984, '../../../../treino/biblioteca/0002.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    
    addFotografia(None, 'bugas', 'joana@ua.pt',  40.642599, -8.649559, '../../../../treino/bugas/IMG_20180321_102018.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'bugas', 'joana@ua.pt',  40.642599, -8.649559, '../../../../treino/bugas/IMG_20180321_102057.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'cantina', 'joana@ua.pt',  40.630797222222, -8.6591805555556, '../../../../treino/cantina/46.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'cantina', 'joana@ua.pt',  40.630797222222, -8.6591805555556, '../../../../treino/cantina/47.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'centro_cultural_dos_congressos', 'joana@ua.pt',  40.639141, -8.644020, '../../../../treino/centro_cultural_dos_congressos/04.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'centro_cultural_dos_congressos', 'joana@ua.pt',  40.639141, -8.644020, '../../../../treino/centro_cultural_dos_congressos/IMG_20180321_095530.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'complexo_pedagogico', 'joana@ua.pt',  40.630383, -8.655780, '../../../../treino/complexo_pedagogico/Complexo_Pedagógico_016.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'complexo_pedagogico', 'joana@ua.pt',  40.630383, -8.655780, '../../../../treino/complexo_pedagogico/IMG_20180305_113705.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'convento_museu_santa_joana', 'joana@ua.pt',  40.640305, -8.651056, '../../../../treino/convento_museu_santa_joana/167010.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'convento_museu_santa_joana', 'joana@ua.pt',  40.640305, -8.651056, '../../../../treino/convento_museu_santa_joana/DSCN1656.JPG', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'degeit', 'joana@ua.pt',  40.631779, -8.656965, '../../../../treino/degeit/0001.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'degeit', 'joana@ua.pt',  40.631779, -8.656965, '../../../../treino/degeit/0002.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'deti', 'joana@ua.pt',  40.633175, -8.659496, '../../../../treino/deti/20.jpg', 1, '2018-03-22 13:30:30', 4.5, 'EmEspera', 0.79, 0.156)
    addFotografia(None, 'deti', 'joana@ua.pt',  40.633175, -8.659496, '../../../../treino/deti/0001.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'dmat', 'joana@ua.pt',  40.630349, -8.658214, '../../../../treino/dmat/IMG_20180308_090741.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'dmat', 'joana@ua.pt', 40.630349, -8.658214, '../../../../treino/dmat/01.jpg', 1, '2018-03-22 13:30:30', 4.5, 'EmEspera', 0.79, 0.156)

    addFotografia(None, 'estacao_de_comboios', 'joana@ua.pt',  40.644323, -8.640550, '../../../../treino/estacao_de_comboios/IMG_20180318_175535.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'estacao_de_comboios', 'joana@ua.pt',  40.644323, -8.640550, '../../../../treino/estacao_de_comboios/IMG_20180318_175844.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'estatua_manuel_firmino', 'joana@ua.pt', 40.642677, -8.648802, '../../../../treino/estatua_manuel_firmino/IMG_20180312_120445.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'estatua_manuel_firmino', 'joana@ua.pt', 40.642677, -8.648802, '../../../../treino/estatua_manuel_firmino/IMG_20180312_120516.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'estatua_princesa_santa_joana', 'joana@ua.pt', 40.6310031, -8.659642599999984, '../../../../treino/estatua_princesa_santa_joana/IMG_20180312_130637.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'estatua_princesa_santa_joana', 'joana@ua.pt', 40.6310031, -8.659642599999984, '../../../../treino/estatua_princesa_santa_joana/IMG_20180312_130727.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'forum_aveiro', 'joana@ua.pt', 40.6409015, -8.6515224, '../../../../treino/forum_aveiro/IMG_20180312_120208.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'forum_aveiro', 'joana@ua.pt', 40.6409015, -8.6515224, '../../../../treino/forum_aveiro/IMG_20180312_120312.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'ieeta', 'joana@ua.pt', 40.6331317, -8.6601875, '../../../../treino/ieeta/IMG_20180308_083320.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'ieeta', 'joana@ua.pt', 40.6331317, -8.6601875, '../../../../treino/ieeta/IMG_20180308_083337.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'jardim_galitos', 'joana@ua.pt', 40.6371452, -8.6412406, '../../../../treino/jardim_galitos/IMG_20180312_122341.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'jardim_galitos', 'joana@ua.pt', 40.6371452, -8.6412406, '../../../../treino/jardim_galitos/IMG_20180312_122343.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'mercado_manuel_firmino', 'joana@ua.pt', 40.6416997, -8.6488876, '../../../../treino/mercado_manuel_firmino/49.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'mercado_manuel_firmino', 'joana@ua.pt', 40.6416997, -8.6488876, '../../../../treino/mercado_manuel_firmino/IMG_20180312_120607.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'parque_da_macaca', 'joana@ua.pt', 40.63654705, -8.65331663, '../../../../treino/parque_da_macaca/5282885.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'parque_da_macaca', 'joana@ua.pt', 40.63654705, -8.65331663, '../../../../treino/parque_da_macaca/IMG_20180312_114447.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'parque_drinks', 'joana@ua.pt', 40.63770034, -8.65513497, '../../../../treino/parque_drinks/IMG_20180312_114632.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'parque_drinks', 'joana@ua.pt', 40.63770034, -8.65513497, '../../../../treino/parque_drinks/IMG_20180312_114653.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'ponte_dos_lacos', 'joana@ua.pt', 40.64157707, -8.65003158, '../../../../treino/ponte_dos_lacos/1313065243885_f.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'ponte_dos_lacos', 'joana@ua.pt', 40.64157707, -8.65003158, '../../../../treino/ponte_dos_lacos/207619_194010250636057_6797.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'ponte_pedonal_parque_macaca_drinks', 'joana@ua.pt', 40.63653796, -8.65410014, '../../../../treino/ponte_pedonal_parque_macaca_drinks/DSC89791.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'ponte_pedonal_parque_macaca_drinks', 'joana@ua.pt', 40.63653796, -8.65410014, '../../../../treino/ponte_pedonal_parque_macaca_drinks/IMG_20180312_114404.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'reitoria', 'joana@ua.pt', 40.63118, -8.657398, '../../../../treino/reitoria/0001.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'reitoria', 'joana@ua.pt', 40.63118, -8.657398, '../../../../treino/reitoria/0002.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'ria_de_aveiro', 'joana@ua.pt', 40.6409015, -8.6515224, '../../../../treino/ria_de_aveiro/15234391022_5d7e39497c_o.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'ria_de_aveiro', 'joana@ua.pt', 40.6409015, -8.6515224, '../../../../treino/ria_de_aveiro/36955390932_17b379639c_o.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'se_de_aveiro', 'joana@ua.pt', 40.63960346, -8.65026632, '../../../../treino/se_de_aveiro/13079449.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'se_de_aveiro', 'joana@ua.pt', 40.63960346, -8.65026632, '../../../../treino/se_de_aveiro/IMG_20180312_124133.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)

    addFotografia(None, 'torre_de_agua', 'joana@ua.pt', 40.62902601, -8.65545995, '../../../../treino/torre_de_agua/IMG_20180301_141628.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)
    addFotografia(None, 'torre_de_agua', 'joana@ua.pt', 40.62902601, -8.65545995, '../../../../treino/torre_de_agua/IMG_20180301_141820.jpg', None, '2018-03-22 13:30:00', 4.5, 'Aprovada', 0.98, 0.123)


def addInfoExemplo():
    addTiposExemplo()
    addUsers()
    #addUtilizadoresExemplo()
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
    #addInfoExemplo()
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
