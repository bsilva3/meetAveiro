'''
Contém métodos que usam web crawlers para pesquisar informação na internet

process_search(query) -> recebe uma query a pesquisar e recolhe informação da wikipedia
sobre a mesma (caso a wikipedia contenha uma entrada válida sobre ela)
'''

import bs4 as bs
import urllib.request

# urls para os edificios da universidade
urls = {
    "deti" : "http://www.ua.pt/deti/PageText.aspx?id=619",
    "ieeta" : "http://www.ua.pt/deti/pagetext.aspx?id=575",
    "biblioteca" : "https://www.ua.pt/sbidm/biblioteca/page/14470"
}

eventos = "https://www.viralagenda.com/pt/aveiro"

def process_search(query):
    '''
    Usa um web crawler para retirar informações do site da UA 
    (usado para os edificios da universidade)

    query - string com o nome do departamento
    '''
    if(query == 'biblioteca'):
        return search_bibs(query)
    else:
        return search_department(query)


def search_bibs(query):
    '''
    Processa a página web sobre as bibliotecas da UA

    query - biblioteca a especificar (ex: "biblioteca" -> Biblioteca de Santiago)
    '''
    link = urls[query]
    sauce = urllib.request.urlopen(link).read()
    soup = bs.BeautifulSoup(sauce, 'lxml')
    block = soup.find('blockquote')

    for child in block.find_all('p'):
        child.decompose()

    #print(block.text.strip())
    return block.text.strip()

def search_department(query):
    '''
    Extrai informação sobre o departamento especificado no site da UA

    query - nome do departamento (ex: deti, ieeta)
    '''
    link = urls[query]
    sauce = urllib.request.urlopen(link).read()
    soup = bs.BeautifulSoup(sauce, 'lxml')
    summary = soup.find('p')
    #print(summary.text)
    return summary.text.strip()


def search_eventos():
    '''
    Extrai informação sobre eventos anunciados no site viralagenda

    (em desenvolvimento)
    '''
    sauce = urllib.request.urlopen(eventos).read()
    soup = bs.BeautifulSoup(sauce, 'lxml')
    name = soup.findAll('div', {"class": "viral-event-title"})
    #hour = soup.findAll('div', {"class": "viral-event-hour"})
    place = soup.findAll('a', {"class": "viral-event-place"})
    eventType = soup.findAll('div', {"class": "viral-event-box viral-event-box-other viral-event-box-cat"})
    
    for i in range(0, len(name)):
        print(name[i].text.strip())
        #print(hour[i].text.strip())
        print(place[i].text.strip())
        print(eventType[i].text.strip())
        print()


def search_turismo():
    '''
    Extrai informação sobre eventos descritos no site do turismo de Aveiro
    e devolve-os numa lista de dicionarios
    '''
    turismo_url = 'https://turismoinaveiro.com/collections/experiencias-cidade-de-aveiro'
    sauce = urllib.request.urlopen(turismo_url).read()
    soup = bs.BeautifulSoup(sauce, 'lxml')
    events = soup.find_all('a', {'class': 'product-card'})

    result = []

    for e in events:
        res = {}
        temp_url = 'https://turismoinaveiro.com/' + e['href']
        temp_sauce = urllib.request.urlopen(temp_url).read()
        temp_soup = bs.BeautifulSoup(temp_sauce, 'lxml')
        title = temp_soup.find('h1')
        price = temp_soup.find('span', {'class': 'product-single__price'})
        table = temp_soup.find('table')
        if table is not None:
            tr = table.find('tr')
            td = tr.find('td')
            ps = td.find_all('p')
        #print(title.text.strip())
        #print(price.text.strip())
        res['title'] = title.text.strip()
        res['price'] = price.text.strip()

        if len(ps) == 3:
            #print(ps[1].text.strip())
            #print(ps[2].text.strip())
            location = ps[1].text.strip() + '\n' + ps[2].text.strip()
            res['location'] = location
        else:
            res['location'] = ps[1].text.strip()
        result.append(res)
    return result

#search_eventos()
search_turismo()
